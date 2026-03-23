package com.example.notesappcompose.data.news

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

class NewsImageCacheDataSource private constructor(
    context: Context,
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val appContext = context.applicationContext
    private val dbHelper = NewsImageCacheDbHelper(appContext)
    private val cacheDir = File(appContext.filesDir, "news_image_cache")
    private val mutex = Mutex()
    private var lastCleanupAtMs: Long = 0L

    suspend fun getOrFetchBitmap(url: String): Bitmap? {
        val safeUrl = url.trim()
        if (safeUrl.isEmpty()) return null

        return withContext(Dispatchers.IO) {
            mutex.withLock {
                val now = System.currentTimeMillis()
                runCleanupIfNeeded(now)

                val cachedEntry = dbHelper.findByUrl(safeUrl)
                val cachedFile = cachedEntry?.let { File(cacheDir, it.fileName) }
                val hasCachedFile = cachedFile?.exists() == true

                if (cachedEntry != null && hasCachedFile) {
                    val fresh = now - cachedEntry.updatedAtMs <= IMAGE_TTL_MS
                    if (fresh) {
                        dbHelper.touch(safeUrl, now)
                        return@withLock decodeBitmap(cachedFile, safeUrl)
                    }
                }

                val downloadedFile = downloadAndStore(safeUrl, now)
                if (downloadedFile != null) {
                    return@withLock decodeBitmap(downloadedFile, safeUrl)
                }

                if (cachedEntry != null && hasCachedFile) {
                    val withinRetention = now - cachedEntry.updatedAtMs <= IMAGE_RETENTION_MS
                    if (withinRetention) {
                        dbHelper.touch(safeUrl, now)
                        return@withLock decodeBitmap(cachedFile, safeUrl)
                    }
                }

                if (cachedEntry != null) {
                    dbHelper.deleteByUrl(safeUrl)
                    cachedFile?.delete()
                }

                null
            }
        }
    }

    private fun decodeBitmap(file: File, url: String): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (bitmap == null) {
            dbHelper.deleteByUrl(url)
            file.delete()
            return null
        }
        return bitmap
    }

    private fun downloadAndStore(url: String, nowMs: Long): File? {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "image/*")
            .get()
            .build()

        val bytes = runCatching {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful.not()) return@use null
                response.body?.bytes()
            }
        }.getOrNull() ?: return null

        if (bytes.isEmpty()) return null

        cacheDir.mkdirs()
        val fileName = hashUrl(url) + ".img"
        val outFile = File(cacheDir, fileName)
        runCatching {
            outFile.outputStream().use { stream ->
                stream.write(bytes)
                stream.flush()
            }
        }.onFailure {
            outFile.delete()
            return null
        }

        dbHelper.upsert(
            ImageCacheEntry(
                url = url,
                fileName = fileName,
                updatedAtMs = nowMs,
                lastAccessAtMs = nowMs,
                sizeBytes = bytes.size.toLong(),
            ),
        )
        return outFile
    }

    private fun runCleanupIfNeeded(nowMs: Long) {
        if (nowMs - lastCleanupAtMs < CLEANUP_INTERVAL_MS) return
        lastCleanupAtMs = nowMs

        val sortedEntries = dbHelper.allEntriesOrderByLastAccessAsc().toMutableList()
        if (sortedEntries.isEmpty()) return

        val staleEntries = sortedEntries.filter { nowMs - it.updatedAtMs > IMAGE_RETENTION_MS }
        staleEntries.forEach { entry ->
            deleteEntry(entry)
        }

        sortedEntries.removeAll(staleEntries.toSet())
        enforceCacheLimits(sortedEntries)
    }

    private fun enforceCacheLimits(entriesAscByAccess: MutableList<ImageCacheEntry>) {
        var totalBytes = entriesAscByAccess.sumOf { it.sizeBytes }
        var totalCount = entriesAscByAccess.size

        val iterator = entriesAscByAccess.iterator()
        while ((totalBytes > MAX_CACHE_BYTES || totalCount > MAX_CACHE_FILES) && iterator.hasNext()) {
            val candidate = iterator.next()
            deleteEntry(candidate)
            totalBytes -= candidate.sizeBytes
            totalCount -= 1
        }
    }

    private fun deleteEntry(entry: ImageCacheEntry) {
        dbHelper.deleteByUrl(entry.url)
        val file = File(cacheDir, entry.fileName)
        file.delete()
    }

    private fun hashUrl(url: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }

    companion object {
        private const val IMAGE_TTL_MS = 6 * 60 * 60 * 1000L
        private const val IMAGE_RETENTION_MS = 7 * 24 * 60 * 60 * 1000L
        private const val MAX_CACHE_BYTES = 40L * 1024L * 1024L
        private const val MAX_CACHE_FILES = 200
        private const val CLEANUP_INTERVAL_MS = 30L * 60L * 1000L

        @Volatile
        private var instance: NewsImageCacheDataSource? = null

        fun getInstance(context: Context): NewsImageCacheDataSource {
            val existing = instance
            if (existing != null) return existing

            val created = NewsImageCacheDataSource(context.applicationContext)
            instance = created
            return created
        }
    }
}

private data class ImageCacheEntry(
    val url: String,
    val fileName: String,
    val updatedAtMs: Long,
    val lastAccessAtMs: Long,
    val sizeBytes: Long,
)

private class NewsImageCacheDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_IMAGES (
                $COL_URL TEXT PRIMARY KEY,
                $COL_FILE_NAME TEXT NOT NULL,
                $COL_UPDATED_AT_MS INTEGER NOT NULL,
                $COL_LAST_ACCESS_MS INTEGER NOT NULL,
                $COL_SIZE_BYTES INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGES")
        onCreate(db)
    }

    fun findByUrl(url: String): ImageCacheEntry? {
        val db = readableDatabase
        db.query(
            TABLE_IMAGES,
            COLUMNS,
            "$COL_URL=?",
            arrayOf(url),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst().not()) return null
            return cursor.toEntry()
        }
    }

    fun upsert(entry: ImageCacheEntry) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_URL, entry.url)
            put(COL_FILE_NAME, entry.fileName)
            put(COL_UPDATED_AT_MS, entry.updatedAtMs)
            put(COL_LAST_ACCESS_MS, entry.lastAccessAtMs)
            put(COL_SIZE_BYTES, entry.sizeBytes)
        }
        db.insertWithOnConflict(
            TABLE_IMAGES,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun touch(url: String, accessMs: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LAST_ACCESS_MS, accessMs)
        }
        db.update(
            TABLE_IMAGES,
            values,
            "$COL_URL=?",
            arrayOf(url),
        )
    }

    fun deleteByUrl(url: String) {
        val db = writableDatabase
        db.delete(
            TABLE_IMAGES,
            "$COL_URL=?",
            arrayOf(url),
        )
    }

    fun allEntriesOrderByLastAccessAsc(): List<ImageCacheEntry> {
        val db = readableDatabase
        db.query(
            TABLE_IMAGES,
            COLUMNS,
            null,
            null,
            null,
            null,
            "$COL_LAST_ACCESS_MS ASC",
        ).use { cursor ->
            val result = mutableListOf<ImageCacheEntry>()
            while (cursor.moveToNext()) {
                result += cursor.toEntry()
            }
            return result
        }
    }

    private fun android.database.Cursor.toEntry(): ImageCacheEntry {
        val urlIndex = getColumnIndexOrThrow(COL_URL)
        val fileNameIndex = getColumnIndexOrThrow(COL_FILE_NAME)
        val updatedIndex = getColumnIndexOrThrow(COL_UPDATED_AT_MS)
        val accessIndex = getColumnIndexOrThrow(COL_LAST_ACCESS_MS)
        val sizeIndex = getColumnIndexOrThrow(COL_SIZE_BYTES)

        return ImageCacheEntry(
            url = getString(urlIndex),
            fileName = getString(fileNameIndex),
            updatedAtMs = getLong(updatedIndex),
            lastAccessAtMs = getLong(accessIndex),
            sizeBytes = getLong(sizeIndex),
        )
    }

    companion object {
        private const val DB_NAME = "news_image_cache.db"
        private const val DB_VERSION = 1

        private const val TABLE_IMAGES = "images"

        private const val COL_URL = "url"
        private const val COL_FILE_NAME = "file_name"
        private const val COL_UPDATED_AT_MS = "updated_at_ms"
        private const val COL_LAST_ACCESS_MS = "last_access_ms"
        private const val COL_SIZE_BYTES = "size_bytes"

        private val COLUMNS = arrayOf(
            COL_URL,
            COL_FILE_NAME,
            COL_UPDATED_AT_MS,
            COL_LAST_ACCESS_MS,
            COL_SIZE_BYTES,
        )
    }
}

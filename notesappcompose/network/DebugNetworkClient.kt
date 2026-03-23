package com.example.notesappcompose.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object DebugNetworkClient {
    private const val TAG = "DebugNetworkClient"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()

    suspend fun logSamplePostWithJsonBody() = withContext(Dispatchers.IO) {
        val payload = JSONObject()
        payload.put("client", "NotesAppCompose")
        payload.put("purpose", "homework-dz3-extra")
        val body = payload.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("https://httpbin.org/post")
            .header("X-App-Client", "NotesAppCompose-Android")
            .header("Accept", "application/json")
            .post(body)
            .build()
        runCatching {
            client.newCall(request).execute().use { response ->
                val snippet = response.body?.string().orEmpty().take(400)
                Log.d(TAG, "httpbin POST code=${response.code} bodySnippet=$snippet")
            }
        }.onFailure { e ->
            Log.w(TAG, "httpbin POST failed: ${e.message}")
        }
    }
}

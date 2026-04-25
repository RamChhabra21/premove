package com.example.premove.network

import android.util.Log
import com.example.premove.data.local.entity.EdgeEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

private data class LlmRequest(val prompt: String)
private data class LlmResponse(val response: String)

class LlmClient @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {
    companion object {
        private const val BASE_URL = "http://192.168.1.13:8001/api/llm"
    }

    data class EdgeResult(val edgeId: String, val inputData: String?)

    suspend fun evaluateEdges(
        outputData: String?,
        edges: List<EdgeEntity>
    ): List<EdgeResult> {
        val prompt = buildString {
            appendLine("Node output: ${outputData ?: "none"}")
            appendLine("For each edge that should fire, return its id and the minimal relevant data to pass forward.")
            appendLine("Return ONLY a JSON array. Example: [{\"edgeId\":\"id1\",\"inputData\":\"relevant info\"}]")
            appendLine("Edges:")
            edges.forEach { edge ->
                appendLine("- id: ${edge.id}, condition: ${edge.condition ?: "always proceed"}")
            }
        }

        Log.d("llm_prompt",prompt)

        val response = complete(prompt)

        Log.d("llm_response", response.content)

        // parse response into List<EdgeResult>
        return try {
            val array = org.json.JSONArray(response.content.trim())
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                EdgeResult(
                    edgeId    = obj.getString("edgeId"),
                    inputData = obj.optString("inputData").ifBlank { null }
                )
            }
        } catch (e: Exception) {
            // fallback — fire all with full output
            edges.map { EdgeResult(it.id, outputData) }
        }
    }

    private data class LlmMessage(val role: String, val content: String)
    private data class LlmRequest(
        val messages: List<LlmMessage>,
        val temperature: Double = 0.1
    )
    private data class LlmResponse(val content: String)
    private suspend fun complete(prompt: String): LlmResponse = withContext(Dispatchers.IO) {
        val body = gson.toJson(
            LlmRequest(
                messages = listOf(LlmMessage(role = "user", content = prompt))
            )
        ).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/complete")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        gson.fromJson(response.body?.string(), LlmResponse::class.java)
    }
}
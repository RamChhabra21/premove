package com.example.premove.network

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
        private const val BASE_URL = "http://10.0.2.2:8001/api/llm"
    }

    suspend fun evaluateEdges(
        outputData: String?,
        edges: List<EdgeEntity>
    ): List<String> {

        // no conditions on any edge — fire all, skip network call
        if (edges.all { it.condition == null }) {
            return edges.map { it.id }
        }

        val prompt = buildString {
            appendLine("A workflow node just completed with this output:")
            appendLine(outputData ?: "no output")
            appendLine()
            appendLine("Decide which edges should propagate.")
            appendLine("Return ONLY a JSON array of edge IDs, nothing else. Example: [\"id1\",\"id2\"]")
            appendLine()
            appendLine("Edges:")
            edges.forEach { edge ->
                appendLine("- id: ${edge.id}, condition: ${edge.condition ?: "always proceed"}")
            }
        }

        val response = complete(prompt)

        return response.response
            .trim()
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    }

    private suspend fun complete(prompt: String): LlmResponse = withContext(Dispatchers.IO) {
        val body = gson.toJson(LlmRequest(prompt))
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL/complete")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        gson.fromJson(response.body?.string(), LlmResponse::class.java)
    }
}
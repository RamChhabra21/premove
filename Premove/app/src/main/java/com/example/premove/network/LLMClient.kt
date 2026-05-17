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
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmClient @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) {
    private val baseUrl = "${NetworkConfig.BASE_URL}/llm"

    data class EdgeResult(val edgeId: String, val inputData: String?)

    data class LlmMessage(val role: String, val content: String)
    data class LlmRequest(
        val messages: List<LlmMessage>,
        val temperature: Double = 0.0
    )
    data class LlmResponse(val content: String?)

    suspend fun evaluateEdges(
        outputData: String?,
        edges: List<EdgeEntity>
    ): List<EdgeResult> {
        val prompt = buildString {
            appendLine("Context Data from previous node: ${outputData ?: "none"}")
            appendLine("")
            appendLine("Task: Evaluate the conditions for the outgoing edges and determine which ones should be triggered.")
            appendLine("")
            appendLine("Instructions:")
            appendLine("1. For each edge, analyze its 'condition' field against the Context Data provided above.")
            appendLine("2. If the condition is met (or if it says 'always proceed'), include that edge's ID in your response.")
            appendLine("3. ONLY include an 'inputData' field if you want to transform, filter, or modify the data specifically for that path. If you want to pass the data as-is, omit the 'inputData' field.")
            appendLine("4. Your decision MUST be based on the logic described in the 'condition' field of each edge.")
            appendLine("")
            appendLine("Return ONLY a JSON array of objects. Example: [{\"edgeId\":\"edge_1\"}, {\"edgeId\":\"edge_2\", \"inputData\":\"filtered result\"}]")
            appendLine("")
            appendLine("Edges to evaluate:")
            edges.forEach { edge ->
                appendLine("- ID: ${edge.id}, Condition: ${edge.condition ?: "always proceed"}")
            }
        }

        Log.d("LlmClient", "Evaluating ${edges.size} edges")
        val response = complete(prompt)
        val content = response?.content?.trim()
        Log.d("llm_response", content ?: "null response")

        return try {
            if (content.isNullOrBlank()) {
                return emptyList()
            }
            
            val array = org.json.JSONArray(content)
            val results = mutableListOf<EdgeResult>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val edgeId = obj.getString("edgeId")
                
                // If inputData is explicitly provided by the LLM, use it.
                // Otherwise, default to passing the original outputData forward.
                val finalInput = if (obj.has("inputData")) {
                    obj.getString("inputData")
                } else {
                    outputData
                }
                results.add(EdgeResult(edgeId, finalInput))
            }
            results
        } catch (e: Exception) {
            Log.e("LlmClient", "Error parsing LLM response: ${e.message}")
            // Fallback: fire none on evaluation failure to avoid incorrect branching
            emptyList()
        }
    }

    suspend fun complete(prompt: String): LlmResponse? = withContext(Dispatchers.IO) {
        try {
            val body = gson.toJson(
                LlmRequest(
                    messages = listOf(LlmMessage(role = "user", content = prompt))
                )
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/complete")
                .post(body)
                .build()
            
            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    Log.d("llm_response", responseBody)
                    gson.fromJson(responseBody, LlmResponse::class.java)
                } else {
                    Log.e("LlmClient", "LLM request failed: ${response.code} $responseBody")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LlmClient", "Exception in complete", e)
            null
        }
    }
}

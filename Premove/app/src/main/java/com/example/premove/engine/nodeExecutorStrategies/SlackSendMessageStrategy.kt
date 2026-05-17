package com.example.premove.engine.nodeExecutorStrategies

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.NodeExecutionResult
import com.example.premove.engine.NodeExecutionStrategy
import com.example.premove.network.NetworkConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlackSendMessageStrategy @Inject constructor(
    private val httpClient: OkHttpClient
) : NodeExecutionStrategy {
    override suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        return try {
            val config = node.configJson?.let { JSONObject(it) }
            val channel = config?.optString("channel") ?: return NodeExecutionResult.Failed("Missing channel")
            val message = config?.optString("message") ?: return NodeExecutionResult.Failed("Missing message")

            val body = JSONObject().apply {
                put("channel", channel)
                put("message", message)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${NetworkConfig.BASE_URL}/slack/send")
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    NodeExecutionResult.Completed(output = "Message sent to $channel")
                } else {
                    NodeExecutionResult.Failed("Slack API failed: ${response.code}")
                }
            }
        } catch (e: Exception) {
            NodeExecutionResult.Failed("Error sending Slack message: ${e.message}")
        }
    }
}

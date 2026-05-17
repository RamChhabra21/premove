package com.example.premove.engine.nodeExecutorStrategies

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.NodeExecutionResult
import com.example.premove.engine.TriggerResult
import com.example.premove.engine.TriggerStrategy
import com.example.premove.network.SlackApiService
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlackMessageReceivedStrategy @Inject constructor(
    private val slackApiService: SlackApiService
) : TriggerStrategy {
    override suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        // Once the node is READY, its inputData will contain the enriched message payload
        return NodeExecutionResult.Completed(output = nodeRunEntity.inputData ?: "")
    }

    override suspend fun evaluate(eventData: String, configJson: String?): TriggerResult? {
        return try {
            val eventJson = JSONObject(eventData)
            val messageText = eventJson.optString("text", "")
            val slackUserId = eventJson.optString("slack_user_id", "")
            val channelId = eventJson.optString("channel", "")
            
            val config = if (configJson != null) JSONObject(configJson) else JSONObject()
            
            // 1. Filter by specific user if configured in Node Editor
            val fromUser = config.optString("fromUser", "")
            if (fromUser.isNotEmpty() && fromUser != slackUserId) {
                return null
            }

            // 2. Filter by keyword if configured
            val keyword = config.optString("keyword", "")
            if (keyword.isNotEmpty() && !messageText.contains(keyword, ignoreCase = true)) {
                return null
            }

            // 3. Enrich data using Backend APIs
            val userProfile = if (slackUserId.isNotEmpty()) slackApiService.getUserProfile(slackUserId) else null
            val channelInfo = if (channelId.isNotEmpty()) slackApiService.getChannelInfo(channelId) else null

            // 4. Construct enriched payload for downstream nodes
            val enrichedData = JSONObject().apply {
                put("text", messageText)
                
                userProfile?.let {
                    put("sender_name", it.optString("real_name", it.optString("name", "Unknown")))
                    put("sender_username", it.optString("name", ""))
                    // sender_image (profile URL) removed to minimize payload size
                }
                
                channelInfo?.let {
                    put("channel_name", it.optString("name", "Unknown"))
                    put("is_dm", it.optBoolean("is_im", false))
                }
            }

            TriggerResult(extractedData = enrichedData.toString())
        } catch (e: Exception) {
            null
        }
    }
}

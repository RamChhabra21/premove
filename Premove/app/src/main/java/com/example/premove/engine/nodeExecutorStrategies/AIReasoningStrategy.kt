package com.example.premove.engine.nodeExecutorStrategies

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.NodeExecutionResult
import com.example.premove.engine.NodeExecutionStrategy
import com.example.premove.network.LlmClient
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class AIReasoningStrategy @Inject constructor(
    private val llmClient: LlmClient
) : NodeExecutionStrategy {
    override suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        return try {
            val config = node.configJson?.let { JSONObject(it) } ?: JSONObject()
            val instructions = config.optString("prompt", "Analyze the input and provide a response.")
            val inputContext = nodeRunEntity.inputData ?: "{}"

            val finalPrompt = """
                Instructions: $instructions
                
                Input Context (JSON):
                $inputContext
                
                Please perform the task described in the instructions using the provided context. 
                Return your response as a plain string.
            """.trimIndent()

            Log.d("AIReasoningStrategy", "Sending prompt to LLM: $instructions")
            val response = llmClient.complete(finalPrompt)
            
            if (response?.content != null) {
                NodeExecutionResult.Completed(output = response.content)
            } else {
                NodeExecutionResult.Failed("LLM returned an empty response")
            }
        } catch (e: Exception) {
            Log.e("AIReasoningStrategy", "Error in AI Reasoning", e)
            NodeExecutionResult.Failed("Reasoning error: ${e.message}")
        }
    }
}

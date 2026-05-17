package com.example.premove.engine

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity

interface NodeExecutionStrategy {
    suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult
}

data class TriggerResult(val extractedData: String)

interface TriggerStrategy : NodeExecutionStrategy {
    /**
     * Evaluates if an external event matches this node's configuration.
     * @param eventData The raw payload from the webhook (JSON string).
     * @param configJson The specific configuration saved for this node.
     * @return Result containing extracted data if match, or null if it shouldn't fire.
     */
    suspend fun evaluate(eventData: String, configJson: String?): TriggerResult?
}
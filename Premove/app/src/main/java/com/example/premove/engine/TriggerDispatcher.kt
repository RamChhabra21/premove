package com.example.premove.engine

import android.util.Log
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.ui.nodes.NodeStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriggerDispatcher @Inject constructor(
    private val nodeRepository: NodeRepository,
    private val nodeRunRepository: NodeRunRepository,
    private val nodeExecutor: NodeExecutor
) {
    /**
     * Entry point for external webhooks or events.
     * Finds all pending nodes of this type and evaluates them against the event.
     */
    suspend fun handleExternalEvent(nodeType: String, rawPayload: String) {
        Log.d("TRIGGER_DISPATCHER", "⚡ Handling event for type: $nodeType")
        Log.d("TRIGGER_DISPATCHER", "📝 Payload: $rawPayload")

        val strategy = nodeExecutor.getStrategy(nodeType)
        if (strategy !is TriggerStrategy) {
            Log.w("TRIGGER_DISPATCHER", "⚠️ No TriggerStrategy found for type: $nodeType")
            return
        }

        val pendingRuns = nodeRunRepository.getPendingNodeRunsByType(nodeType)
        Log.d("TRIGGER_DISPATCHER", "🔍 Found ${pendingRuns.size} PENDING node runs for this type")

        pendingRuns.forEach { nodeRun ->
            val node = nodeRepository.getNodeById(nodeRun.nodeId)
            
            // Evaluate condition (e.g. check Slack keyword)
            Log.d("TRIGGER_DISPATCHER", "🧐 Evaluating node ${nodeRun.nodeId} (Run: ${nodeRun.workflowRunId})")
            val result = strategy.evaluate(rawPayload, node?.configJson)
            
            if (result != null) {
                Log.i("TRIGGER_DISPATCHER", "✅ Condition MET for node ${nodeRun.nodeId}!")
                // Condition met! Mark node as READY so WorkflowEngine can pick it up
                val updated = nodeRunRepository.updateNodeInputAndStatus(
                    nodeId = nodeRun.nodeId,
                    workflowRunId = nodeRun.workflowRunId,
                    inputData = result.extractedData,
                    newStatus = NodeStatus.READY
                )
                Log.d("TRIGGER_DISPATCHER", "🔄 Database updated: $updated row(s)")
            } else {
                Log.d("TRIGGER_DISPATCHER", "❌ Condition NOT met for node ${nodeRun.nodeId}")
            }
        }
    }
}
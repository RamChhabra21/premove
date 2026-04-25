package com.example.premove.engine.nodeExecutorStrategies

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.JobRequest
import com.example.premove.engine.JobTracker
import com.example.premove.engine.NodeExecutionResult
import com.example.premove.engine.NodeExecutionStrategy
import org.json.JSONObject

class WebExecutionStrategy : NodeExecutionStrategy {

    private val jobTracker = JobTracker()

    override suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        val config = node.configJson?.let { JSONObject(it) }
        val goal = config?.getString("prompt") ?: return NodeExecutionResult.Failed("Missing goal in config")

        val jobId = jobTracker.startJob(
            JobRequest(
                workflowId = node.workflowId,
                goal = goal,
                nodeId = node.id.toString(),
                workflowType = "WEB"
            )
        ) ?: return NodeExecutionResult.Failed("Failed to start job")

        return NodeExecutionResult.Pending(jobId = jobId)
    }
}
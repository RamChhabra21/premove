package com.example.premove.engine
import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.nodeExecutorStrategies.AlarmStrategy
import com.example.premove.engine.nodeExecutorStrategies.WebExecutionStrategy
import com.example.premove.ui.nodes.NodeType

// --- Executor ---
class NodeExecutor {

    private val strategies: Map<NodeType, NodeExecutionStrategy> = mapOf(
        NodeType.WEB_AGENT to WebExecutionStrategy(),
        NodeType.ALARM to AlarmStrategy()
    )

    suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        val nodeType = NodeType.valueOf(node.type)
        val strategy = strategies[nodeType] ?: error("No strategy for ${node.type}")
        return strategy.execute(node, nodeRunEntity)
    }
}
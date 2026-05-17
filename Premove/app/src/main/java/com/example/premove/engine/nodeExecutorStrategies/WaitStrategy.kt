package com.example.premove.engine.nodeExecutorStrategies

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.NodeExecutionResult
import com.example.premove.engine.NodeExecutionStrategy
import kotlinx.coroutines.delay
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WaitStrategy @Inject constructor() : NodeExecutionStrategy {
    override suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        val config = node.configJson?.let { JSONObject(it) }
        val durationStr = config?.optString("duration") ?: "0s"
        
        // Simple parser for 10s, 5m etc.
        val millis = try {
            val num = durationStr.dropLast(1).toLong()
            when (durationStr.last()) {
                's', 'S' -> num * 1000
                'm', 'M' -> num * 60 * 1000
                'h', 'H' -> num * 60 * 60 * 1000
                else -> num
            }
        } catch (e: Exception) {
            1000L
        }

        delay(millis)
        return NodeExecutionResult.Completed(output = "Waited for $durationStr")
    }
}

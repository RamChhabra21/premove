package com.example.premove.engine

import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.nodeExecutorStrategies.AIReasoningStrategy
import com.example.premove.engine.nodeExecutorStrategies.AlarmStrategy
import com.example.premove.engine.nodeExecutorStrategies.SlackMessageReceivedStrategy
import com.example.premove.engine.nodeExecutorStrategies.SlackSendMessageStrategy
import com.example.premove.engine.nodeExecutorStrategies.VoiceOutputStrategy
import com.example.premove.engine.nodeExecutorStrategies.WaitStrategy
import com.example.premove.engine.nodeExecutorStrategies.WebExecutionStrategy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NodeExecutor @Inject constructor(
    private val webExecutionStrategy: WebExecutionStrategy,
    private val alarmStrategy: AlarmStrategy,
    private val slackMessageReceivedStrategy: SlackMessageReceivedStrategy,
    private val slackSendMessageStrategy: SlackSendMessageStrategy,
    private val aiReasoningStrategy: AIReasoningStrategy,
    private val waitStrategy: WaitStrategy,
    private val voiceOutputStrategy: VoiceOutputStrategy
) {

    private val strategies: Map<String, NodeExecutionStrategy> by lazy {
        mapOf(
            "WEB_AGENT" to webExecutionStrategy,
            "ALARM" to alarmStrategy,
            "ALARM_FIRED" to alarmStrategy,
            "SLACK_MESSAGE_RECEIVED" to slackMessageReceivedStrategy,
            "SLACK_SEND_MESSAGE" to slackSendMessageStrategy,
            "AI_REASONING" to aiReasoningStrategy,
            "WAIT_DELAY" to waitStrategy,
            "WAIT_UNTIL" to waitStrategy,
            "SPEAK_TEXT" to voiceOutputStrategy
        )
    }

    fun getStrategy(type: String): NodeExecutionStrategy? {
        return strategies[type.uppercase()]
    }

    suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        val strategy = getStrategy(node.type) ?: error("No strategy for ${node.type}")
        return strategy.execute(node, nodeRunEntity)
    }
}

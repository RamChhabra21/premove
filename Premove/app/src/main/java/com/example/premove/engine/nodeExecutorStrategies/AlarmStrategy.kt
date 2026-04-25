package com.example.premove.engine.nodeExecutorStrategies

import android.media.RingtoneManager
import com.example.premove.PremoveApplication
import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.JobRequest
import com.example.premove.engine.JobTracker
import com.example.premove.engine.NodeExecutionResult
import com.example.premove.engine.NodeExecutionStrategy
import kotlinx.coroutines.delay
import org.json.JSONObject

class AlarmStrategy : NodeExecutionStrategy {
    override suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        return try {
            val context = PremoveApplication.appContext
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
            ringtone.play()
            delay(2000)
            ringtone.stop()
            NodeExecutionResult.Completed(output = "Alarm played")
        } catch (e: Exception) {
            NodeExecutionResult.Failed("Failed to play alarm: ${e.message}")
        }
    }
}
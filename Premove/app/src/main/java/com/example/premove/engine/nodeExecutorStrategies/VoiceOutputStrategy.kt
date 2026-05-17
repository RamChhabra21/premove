package com.example.premove.engine.nodeExecutorStrategies

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.premove.data.local.entity.NodeEntity
import com.example.premove.data.local.entity.NodeRunEntity
import com.example.premove.engine.NodeExecutionResult
import com.example.premove.engine.NodeExecutionStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceOutputStrategy @Inject constructor(
    @ApplicationContext private val context: Context
) : NodeExecutionStrategy {

    private var tts: TextToSpeech? = null
    private val initDeferred = CompletableDeferred<Boolean>()

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                initDeferred.complete(true)
            } else {
                Log.e("VoiceOutputStrategy", "TTS Initialization failed")
                initDeferred.complete(false)
            }
        }
    }

    override suspend fun execute(node: NodeEntity, nodeRunEntity: NodeRunEntity): NodeExecutionResult {
        val isInitialized = initDeferred.await()
        if (!isInitialized) return NodeExecutionResult.Failed("Voice engine failed to initialize")

        val textToSpeak = if (!nodeRunEntity.inputData.isNullOrBlank()) {
            nodeRunEntity.inputData
        } else {
            // Fallback to static config if no input from upstream
            val config = node.configJson?.let { org.json.JSONObject(it) }
            config?.optString("text") ?: "Nothing to say"
        }

        return try {
            val speechDeferred = CompletableDeferred<Unit>()
            
            // Set listener to know when speaking finishes
            tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    speechDeferred.complete(Unit)
                }
                override fun onError(utteranceId: String?) {
                    speechDeferred.completeExceptionally(Exception("TTS Error"))
                }
            })

            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "node_${node.id}")
            
            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, params, "node_${node.id}")
            
            speechDeferred.await()
            NodeExecutionResult.Completed(output = "Spoke: $textToSpeak")
        } catch (e: Exception) {
            NodeExecutionResult.Failed("Voice error: ${e.message}")
        }
    }
}

package com.example.premove.network

import android.util.Log
import com.example.premove.engine.TriggerDispatcher
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

// App-level scope — never cancelled when service is destroyed
private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var triggerDispatcher: TriggerDispatcher

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM_RECEIVER", "🚀 Received FCM Data Message from: ${remoteMessage.from}")
        Log.d("FCM_RECEIVER", "📦 Raw Data Payload: ${remoteMessage.data}")

        val nodeType = remoteMessage.data["nodeType"] ?: "SLACK_MESSAGE_RECEIVED"

        // Convert map to proper JSON string so strategy.evaluate() can parse it
        val payload = JSONObject(remoteMessage.data as Map<*, *>).toString()

        Log.d("FCM_RECEIVER", "Payload: $payload")

        serviceScope.launch {
            try {
                triggerDispatcher.handleExternalEvent(nodeType, payload)
                Log.d("FCM_RECEIVER", "✅ Successfully handed off to TriggerDispatcher: $nodeType")
            } catch (e: Exception) {
                Log.e("FCM_RECEIVER", "❌ Error during handoff", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_RECEIVER", "🆕 New FCM token: $token")
    }
}
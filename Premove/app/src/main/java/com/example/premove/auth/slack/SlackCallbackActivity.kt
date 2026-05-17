package com.example.premove.auth.slack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.premove.MainActivity
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class SlackCallbackActivity : ComponentActivity() {

    @Inject
    lateinit var slackAuth: SlackAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleRedirect(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleRedirect(intent)
    }

    private fun handleRedirect(intent: Intent) {
        val uri = intent.data ?: run {
            Log.e("SlackAuth", "No URI in intent")
            finish()
            return
        }

        Log.d("SlackAuth", "URI received: $uri")

        val code = slackAuth.extractCode(uri) ?: run {
            Log.e("SlackAuth", "extractCode null — state mismatch or error")
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // Get the real FCM token so the backend can target this device
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                Log.d("SlackAuth", "Exchanging code with FCM token: $fcmToken")

                val success = slackAuth.exchangeCode(code, fcmToken)
                Log.d("SlackAuth", "Exchange success: $success")

                if (success) {
                    val mainIntent = Intent(this@SlackCallbackActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(mainIntent)
                }
            } catch (e: Exception) {
                Log.e("SlackAuth", "Error during Slack redirect handling", e)
            } finally {
                finish()
            }
        }
    }
}

package com.example.premove.auth.slack

import android.content.Context
import com.example.premove.R
import com.example.premove.network.NetworkConfig

// SlackConfig.kt
object SlackConfig {
    fun getClientId(context: Context): String {
        return context.getString(R.string.slack_client_id)
    }

    const val REDIRECT_URI  = "premove://slack/callback"
    const val USER_SCOPES   = "channels:read,channels:history,im:read,im:history,groups:read,groups:history"
    const val EXCHANGE_PATH = "/slack/exchange"
    
    val BACKEND_URL: String
        get() = "${NetworkConfig.BASE_URL}$EXCHANGE_PATH"
}

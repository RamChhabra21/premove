package com.example.premove.auth.slack

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SlackAuth @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {

    private val prefs = context.getSharedPreferences("slack_auth", Context.MODE_PRIVATE)

    // Step 1 — Launch Slack OAuth in Chrome Custom Tab with PKCE
    fun launch() {
        val state = generateState()
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)

        prefs.edit()
            .putString("state", state)
            .putString("code_verifier", codeVerifier)
            .apply()

        val url = "https://slack.com/oauth/v2/authorize" +
                "?client_id=${SlackConfig.getClientId(context)}" +
                "&user_scope=${SlackConfig.USER_SCOPES}" +
                "&redirect_uri=${SlackConfig.REDIRECT_URI}" +
                "&state=$state" +
                "&code_challenge=$codeChallenge" +
                "&code_challenge_method=S256"

        val customTabsIntent = CustomTabsIntent.Builder().build()
        // Adding FLAG_ACTIVITY_NEW_TASK because we are using ApplicationContext
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, Uri.parse(url))
    }

    // Step 2 — Extract code from redirect URI, verify state
    fun extractCode(uri: Uri): String? {
        if (uri.getQueryParameter("error") != null) return null
        val returnedState = uri.getQueryParameter("state")
        val savedState = prefs.getString("state", null)
        if (returnedState != savedState) return null  // CSRF check
        return uri.getQueryParameter("code")
    }

    // Step 3 — Send code + code_verifier + FCM token to your backend
    suspend fun exchangeCode(code: String, fcmToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val codeVerifier = prefs.getString("code_verifier", null)
                    ?: run {
                        Log.e("SlackAuth", "code_verifier is null")
                        return@withContext false
                    }

                Log.d("SlackAuth", "Starting exchangeCode call...")
                Log.d("SlackAuth", "code: $code")
                Log.d("SlackAuth", "code_verifier: $codeVerifier")
                Log.d("SlackAuth", "URL: ${SlackConfig.BACKEND_URL}")

                val bodyJson = JSONObject().apply {
                    put("code", code)
                    put("fcm_token", fcmToken)
                    put("code_verifier", codeVerifier)
                }
                
                Log.d("SlackAuth", "Request Body: $bodyJson")

                val body = bodyJson.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(SlackConfig.BACKEND_URL)
                    .post(body)
                    .build()

                Log.d("SlackAuth", "Executing request...")
                httpClient.newCall(request).execute().use { response ->
                    Log.d("SlackAuth", "Response received. Code: ${response.code}")
                    val responseBody = response.body?.string() ?: "no body"
                    Log.d("SlackAuth", "Response Body: $responseBody")

                    val success = response.isSuccessful
                    if (success) {
                        prefs.edit().putBoolean("connected", true).apply()
                    } else {
                        Log.e("SlackAuth", "Exchange failed with code ${response.code}: $responseBody")
                    }
                    success
                }

            } catch (e: Exception) {
                Log.e("SlackAuth", "Exception during exchangeCode", e)
                false
            }
        }
    }
    fun isConnected() = prefs.getBoolean("connected", false)

    fun disconnect() = prefs.edit().clear().apply()

    private fun generateState(): String {
        val state = UUID.randomUUID().toString()
        prefs.edit().putString("state", state).apply()
        return state
    }

    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    private fun generateCodeChallenge(verifier: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}

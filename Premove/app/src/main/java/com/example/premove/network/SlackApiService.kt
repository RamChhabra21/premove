package com.example.premove.network

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

@Singleton
class SlackApiService @Inject constructor(
    private val httpClient: OkHttpClient
) {
    // Simple in-memory cache to avoid redundant network calls for profiles and channels
    private val userCache = ConcurrentHashMap<String, JSONObject>()
    private val channelCache = ConcurrentHashMap<String, JSONObject>()

    suspend fun getUserProfile(slackUserId: String): JSONObject? = withContext(Dispatchers.IO) {
        userCache[slackUserId]?.let {
            Log.d("SlackApiService", "📦 Cache HIT for user: $slackUserId")
            return@withContext it
        }

        val url = "${NetworkConfig.BASE_URL}/slack/user?slack_user_id=$slackUserId"
        Log.d("SlackApiService", "Fetching user profile: $url")
        val request = Request.Builder().url(url).get().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val json = if (body != null) JSONObject(body) else null
                    if (json != null) {
                        userCache[slackUserId] = json
                    }
                    json
                } else {
                    Log.e("SlackApiService", "Failed to fetch user profile: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SlackApiService", "Error fetching user profile", e)
            null
        }
    }

    suspend fun getChannelInfo(channelId: String): JSONObject? = withContext(Dispatchers.IO) {
        channelCache[channelId]?.let {
            Log.d("SlackApiService", "📦 Cache HIT for channel: $channelId")
            return@withContext it
        }

        val url = "${NetworkConfig.BASE_URL}/slack/channel?channel_id=$channelId"
        Log.d("SlackApiService", "Fetching channel info: $url")
        val request = Request.Builder().url(url).get().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val json = if (body != null) JSONObject(body) else null
                    if (json != null) {
                        channelCache[channelId] = json
                    }
                    json
                } else {
                    Log.e("SlackApiService", "Failed to fetch channel info: ${response.code}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("SlackApiService", "Error fetching channel info", e)
            null
        }
    }

    suspend fun getUsers(): List<JSONObject> = withContext(Dispatchers.IO) {
        val url = "${NetworkConfig.BASE_URL}/slack/users"
        Log.d("SlackApiService", "Fetching all users: $url")
        val request = Request.Builder().url(url).get().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val jsonArray = JSONArray(body)
                        val list = mutableListOf<JSONObject>()
                        for (i in 0 until jsonArray.length()) {
                            val user = jsonArray.getJSONObject(i)
                            // Optionally warm up the cache
                            val userId = user.optString("id")
                            if (userId.isNotEmpty()) userCache[userId] = user
                            list.add(user)
                        }
                        list
                    } else emptyList()
                } else {
                    Log.e("SlackApiService", "Failed to fetch users: ${response.code}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("SlackApiService", "Error fetching users", e)
            emptyList()
        }
    }

    suspend fun getConversations(): List<JSONObject> = withContext(Dispatchers.IO) {
        val url = "${NetworkConfig.BASE_URL}/slack/conversations"
        Log.d("SlackApiService", "Fetching conversations: $url")
        val request = Request.Builder().url(url).get().build()
        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val jsonArray = JSONArray(body)
                        val list = mutableListOf<JSONObject>()
                        for (i in 0 until jsonArray.length()) {
                            val channel = jsonArray.getJSONObject(i)
                            val channelId = channel.optString("id")
                            if (channelId.isNotEmpty()) channelCache[channelId] = channel
                            list.add(channel)
                        }
                        list
                    } else emptyList()
                } else {
                    Log.e("SlackApiService", "Failed to fetch conversations: ${response.code}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("SlackApiService", "Error fetching conversations", e)
            emptyList()
        }
    }

    fun clearCache() {
        userCache.clear()
        channelCache.clear()
    }
}

package com.example.premove.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthApiService @Inject constructor(
    private val httpClient: OkHttpClient
) {
    suspend fun validateUser(token: String): Result<Unit> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}/auth/me")
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Backend validation failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

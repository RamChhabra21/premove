package com.example.premove.network

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

@Singleton
class AuthInterceptor @Inject constructor(
    private val auth: FirebaseAuth
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Skip adding token if it's an external request (not to our BASE_URL)
        if (!request.url.toString().startsWith(NetworkConfig.BASE_URL)) {
            return chain.proceed(request)
        }

        val user = auth.currentUser
        val token = user?.let {
            try {
                // Block and wait for the token with a timeout
                val task = it.getIdToken(false)
                val result = Tasks.await(task, 10, TimeUnit.SECONDS)
                result.token
            } catch (e: Exception) {
                Log.e("AuthInterceptor", "Failed to fetch ID token", e)
                null
            }
        }

        val authenticatedRequest = if (token != null) {
            Log.d("AuthInterceptor", "Injecting token into request: ${request.url}")
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w("AuthInterceptor", "Proceeding without token for URL: ${request.url}")
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}

package com.example.premove.engine

import android.util.Log
import com.example.premove.network.NetworkConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

// --- Job Result ---
sealed class JobResult {
    data class Done(val output: String) : JobResult()
    object InProgress : JobResult()
    object Failed : JobResult()
}

// --- Request ---
data class JobRequest(
    val workflowId: String,
    val goal: String,
    val nodeId: String,
    val workflowType: String = "WEB"
)

@Singleton
class JobTracker @Inject constructor(
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val firebaseAuth: FirebaseAuth
) {

    private val baseUrl = "${NetworkConfig.BASE_URL}/job"

    private suspend fun getAuthToken(): String? {
        return try {
            firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Log.e("JobTracker", "Error fetching auth token", e)
            null
        }
    }

    // --- start a job, returns jobId ---
    suspend fun startJob(request: JobRequest): String? = withContext(Dispatchers.IO) {
        try {
            val token = getAuthToken()
            val body = JSONObject().apply {
                put("workflow_id", request.workflowId)
                put("goal", request.goal)
                put("node_id", request.nodeId)
                put("workflow_type", request.workflowType)
            }.toString().toRequestBody("application/json".toMediaType())

            val httpRequest = Request.Builder()
                .url(baseUrl)
                .post(body)
                .apply {
                    token?.let { addHeader("Authorization", "Bearer $it") }
                }
                .build()

            httpClient.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    JSONObject(responseBody).getString("job_id")
                } else {
                    Log.e("JobTracker", "Start job failed: ${response.code} $responseBody")
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- get job data, returns JobResult ---
    suspend fun getJobData(jobId: String): JobResult = withContext(Dispatchers.IO) {
        try {
            val token = getAuthToken()
            val httpRequest = Request.Builder()
                .url("$baseUrl/$jobId")
                .get()
                .apply {
                    token?.let { addHeader("Authorization", "Bearer $it") }
                }
                .build()

            httpClient.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e("JobTracker", "Get job data failed: ${response.code} $responseBody")
                    return@withContext JobResult.Failed
                }

                val json = JSONObject(responseBody)
                Log.d("workflow_engine", "data coming in :  $json")
                Log.d("workflow_engine", "job status : ${json.getString("status")}")
                
                when (json.getString("status").uppercase()) {
                    "COMPLETED" -> JobResult.Done(json.optString("result", ""))
                    "FAILED"    -> JobResult.Failed
                    else        -> JobResult.InProgress
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            JobResult.Failed
        }
    }
}

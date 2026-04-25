package com.example.premove.engine

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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

class JobTracker {

    private val baseUrl = "http://192.168.1.13:8001/api/job"

    // --- start a job, returns jobId ---
    // request:  { workflow_id, goal, node_id, workflow_type }
    // response: { job_id: "019d1c79-...", status: "queued" }
    suspend fun startJob(request: JobRequest): String? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(baseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val body = JSONObject().apply {
                put("workflow_id", request.workflowId)
                put("goal", request.goal)
                put("node_id", request.nodeId)
                put("workflow_type", request.workflowType)
            }.toString()

            connection.outputStream.use { it.write(body.toByteArray()) }

            val response = connection.inputStream.bufferedReader().readText()
            JSONObject(response).getString("job_id")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- get job data, returns JobResult ---
    // response (in progress): { job_id: "...", status: "queued/running" }
    // response (done):        { job_id: "...", status: "completed", output: "..." }
    // response (failed):      { job_id: "...", status: "failed", error: "..." }
    suspend fun getJobData(jobId: String): JobResult = withContext(Dispatchers.IO) {
        try {
            val connection = (URL("$baseUrl/$jobId").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
            }

            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            Log.d("workflow_engine","data coming in :  $json")
            Log.d("workflow_engine","job status : ${json.getString("status")}")
            when (json.getString("status")) {
                "COMPLETED" -> JobResult.Done(json.optString("result", null))
                "FAILED"    -> JobResult.Failed
                else        -> JobResult.InProgress  // queued, running
            }
        } catch (e: Exception) {
            e.printStackTrace()
            JobResult.Failed
        }
    }
}
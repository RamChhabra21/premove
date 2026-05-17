package com.example.premove

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.premove.engine.WorkflowEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import dagger.hilt.android.EntryPointAccessors
import com.example.premove.di.ServiceEntryPoint

class AutomationService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val entryPoint by lazy {
        EntryPointAccessors.fromApplication<ServiceEntryPoint>(
            applicationContext
        )
    }

    private val workflowEngine by lazy {
        WorkflowEngine(
            workflowRepository = entryPoint.workflowRepository(),
            workflowRunRepository = entryPoint.workflowRunRepository(),
            nodeRepository = entryPoint.nodeRepository(),
            nodeRunRepository = entryPoint.nodeRunRepository(),
            edgeRepository = entryPoint.edgeRepository(),
            edgeRunRepository = entryPoint.edgeRunRepository(),
            llmClient = entryPoint.llmClient(),
            jobTracker = entryPoint.jobTracker(),
            nodeExecutor = entryPoint.nodeExecutor(),
            scope = scope
        )
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    private fun startForegroundService() {
        // Using NotificationCompat.PRIORITY_MIN and IMPORTANCE_MIN (in channel) 
        // to make the notification as unobtrusive as possible.
        val notification = NotificationCompat.Builder(this, PremoveApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // If you have a specific type in Manifest (like dataSync), use it here.
            // Using DATA_SYNC as a placeholder; ensure it matches your AndroidManifest.xml
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } catch (e: Exception) {
                // Fallback for cases where type might not match or other errors
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            while (isActive) {
                try {
                    workflowEngine.execute()
                } catch (e: Exception) {
                    Log.e("AutomationService", "Error in engine loop", e)
                }
                delay(5000)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Log.d("AutomationService", "❌ Service destroyed")
    }

    companion object {
        private const val NOTIFICATION_ID = 101
    }
}

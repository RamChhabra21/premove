package com.example.premove

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.premove.data.local.dao.WorkflowDao
import com.example.premove.data.repository.NodeRepository
import com.example.premove.data.repository.NodeRunRepository
import com.example.premove.data.repository.WorkflowRepository
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
            scope = scope
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification())

        scope.launch {
            while (isActive) {
                Log.d("AutomationService", "✅ Service is alive")
                // perform actions here
                // call workflow engine. execute here

                workflowEngine.execute()
                delay(30_000)
            }
        }

        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val channelId = "automation_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Automation Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Automation Running")
            .setContentText("Workflows are being monitored")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        Log.d("AutomationService", "❌ Service destroyed")
    }
}
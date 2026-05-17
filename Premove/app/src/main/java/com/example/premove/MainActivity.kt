package com.example.premove

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.premove.viewModel.AuthViewModel
import com.example.premove.ui.auth.LoginScreen
import com.example.premove.ui.navigation.AppNavHost
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Request Notification Permission (Android 13+)
        askNotificationPermission()

        // 2. Check and Request Battery Optimization Exemption
        requestIgnoreBatteryOptimizations()

        // Get and log FCM Token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Current FCM Token: $token")
        }

        // Start automation service
        val intent = Intent(this, AutomationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        setContent {
            val state by authViewModel.state.collectAsState()

            if (state.user == null) {
                LoginScreen(
                    viewModel = authViewModel,
                    onSignInSuccess = { }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 0.dp, y = 0.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    AppNavHost()
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        val intent = Intent()
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }

    override fun onResume() {
        Log.d("MainActivity", "onResume called")
        super.onResume()
        authViewModel.checkIfStillValid()
    }
}

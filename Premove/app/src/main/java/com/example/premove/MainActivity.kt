package com.example.premove

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.premove.ui.navigation.AppNavHost
import com.example.premove.ui.theme.PreMoverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Start automation service
        val intent = Intent(this, AutomationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        setContent {
            PreMoverTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize().offset(x=0.dp,y=0.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
package com.example.premove

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.premove.viewModel.AuthViewModel
import com.example.premove.ui.auth.LoginScreen
import com.example.premove.ui.navigation.AppNavHost
import com.example.premove.ui.theme.PreMoverTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

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
            // get the viewmodel here at the top level
            val state by authViewModel.state.collectAsState()

            if (state.user == null) {
                // not logged in → show login
                LoginScreen(
                    viewModel = authViewModel,
                    onSignInSuccess = { }  // state change handles navigation automatically
                )
            } else {
                // logged in → show your normal app
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

    override fun onResume() {  // ← only real addition
        Log.d("MainActivity", "onResume called")
        super.onResume()
        authViewModel.checkIfStillValid()
    }
}
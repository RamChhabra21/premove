package com.example.premove.viewModel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.premove.auth.GoogleAuthClient
import com.example.premove.network.AuthApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleAuthClient: GoogleAuthClient,
    private val authApiService: AuthApiService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // passive listener — catches token expiry, sign outs automatically
        googleAuthClient.observeAuthState { user ->
            _state.update { it.copy(user = user) }
        }
    }

    // called from onResume — catches bans quickly
    fun checkIfStillValid() {
        viewModelScope.launch {
            val isValid = googleAuthClient.refreshAndValidateUser()
            if (!isValid) {
                _state.update { it.copy(user = null) }
            }
        }
    }

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val signInResult = googleAuthClient.signIn(activity)
            
            signInResult.onSuccess { user ->
                try {
                    val token = user.getIdToken(true).await().token
                    if (token != null) {
                        authApiService.validateUser(token)
                            .onSuccess {
                                _state.update { it.copy(user = user, isLoading = false) }
                            }
                            .onFailure { e ->
                                googleAuthClient.signOut()
                                _state.update { it.copy(error = "Backend validation failed: ${e.message}", isLoading = false, user = null) }
                            }
                    } else {
                        googleAuthClient.signOut()
                        _state.update { it.copy(error = "Could not retrieve auth token", isLoading = false, user = null) }
                    }
                } catch (e: Exception) {
                    googleAuthClient.signOut()
                    _state.update { it.copy(error = "Validation error: ${e.message}", isLoading = false, user = null) }
                }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthClient.signOut()
            _state.update { it.copy(user = null) }
        }
    }
}

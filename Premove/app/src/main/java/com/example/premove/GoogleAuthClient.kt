package com.example.premove

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) {
    private val credentialManager = CredentialManager.create(context)

    fun getSignedInUser(): FirebaseUser? = auth.currentUser

    fun isSignedIn(): Boolean = auth.currentUser != null

    // passive — listens to Firebase auth state changes continuously
    fun observeAuthState(onAuthChanged: (FirebaseUser?) -> Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            onAuthChanged(firebaseAuth.currentUser)
        }
    }

    // active — force token refresh to catch bans immediately
    suspend fun refreshAndValidateUser(): Boolean {
        Log.d("GoogleAuthClient", "checking if user still valid...")
        Log.d("GoogleAuthClient", "current user: ${auth.currentUser?.uid}")
        return try {
            val result = auth.currentUser?.getIdToken(true)?.await()
            Log.d("GoogleAuthClient", "token refresh succeeded: ${result?.token?.take(20)}")
            true
        } catch (e: FirebaseAuthException) {
            Log.e("GoogleAuthClient", "FirebaseAuthException: ${e.errorCode} ${e.message}")
            auth.signOut()
            false
        } catch (e: Exception) {
            Log.e("GoogleAuthClient", "Exception type: ${e::class.java.name} message: ${e.message}")
            true
        }
    }

    suspend fun signIn(): Result<FirebaseUser> {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(
                    googleIdTokenCredential.idToken, null
                )
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                authResult.user?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("User is null after sign in"))
            } else {
                Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: GetCredentialException) {
            Log.e("GoogleAuthClient", "Sign-in failed", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("GoogleAuthClient", "Firebase auth failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            Log.e("GoogleAuthClient", "Couldn't clear credentials", e)
        }
    }
}
package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.ArishRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class ProfileSetupRequired(val user: FirebaseUser) : AuthUiState
    data class Authenticated(val user: FirebaseUser, val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(private val repository: ArishRepository) : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        val user = auth.currentUser
        if (user == null) {
            _uiState.value = AuthUiState.Unauthenticated
        } else {
            loadUserProfile(user)
        }
    }

    private fun loadUserProfile(user: FirebaseUser) {
        viewModelScope.launch {
            try {
                val profile = repository.observeUserProfile(user.uid).firstOrNull()
                if (profile != null && profile.username.isNotBlank()) {
                    _uiState.value = AuthUiState.Authenticated(user, profile)
                    repository.updatePresence(true)
                } else {
                    _uiState.value = AuthUiState.ProfileSetupRequired(user)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to load user profile", e)
                _uiState.value = AuthUiState.ProfileSetupRequired(user)
            }
        }
    }

    fun attemptAutoSignIn(context: Context) {
        val user = auth.currentUser
        if (user != null) {
            loadUserProfile(user)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Unauthenticated
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        val credentialManager = CredentialManager.create(context)

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    authResult.user?.let { loadUserProfile(it) } ?: run {
                        _uiState.value = AuthUiState.Unauthenticated
                    }
                } else {
                    _uiState.value = AuthUiState.Unauthenticated
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error("Google Sign-In configuration missing")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
        val credentialManager = CredentialManager.create(context)

        _isActionLoading.value = true

        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    _isActionLoading.value = false
                    authResult.user?.let { loadUserProfile(it) }
                } else {
                    _isActionLoading.value = false
                    _uiState.value = AuthUiState.Error("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                _isActionLoading.value = false
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                _isActionLoading.value = false
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun saveProfile(
        displayName: String,
        username: String,
        phoneNumber: String,
        about: String,
        photoUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onError("Not signed in")
            return
        }

        if (displayName.isBlank()) {
            onError("Please enter your display name")
            return
        }

        if (username.isBlank() || username.length < 3) {
            onError("Username must be at least 3 characters")
            return
        }

        _isActionLoading.value = true
        viewModelScope.launch {
            try {
                val profile = UserProfile(
                    userId = user.uid,
                    displayName = displayName.trim(),
                    username = username.trim().lowercase().replace(" ", "_"),
                    phoneNumber = phoneNumber.trim(),
                    about = if (about.isBlank()) "Hey there! I am using Arish Messenger." else about.trim(),
                    photoUrl = photoUrl,
                    isOnline = true
                )
                repository.saveUserProfile(profile)
                _isActionLoading.value = false
                _uiState.value = AuthUiState.Authenticated(user, profile)
                onSuccess()
            } catch (e: Exception) {
                _isActionLoading.value = false
                onError(e.localizedMessage ?: "Failed to save profile")
            }
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            repository.updatePresence(false)
            auth.signOut()
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("Auth", "Failed to clear credentials", e)
            } finally {
                _uiState.value = AuthUiState.Unauthenticated
            }
        }
    }
}

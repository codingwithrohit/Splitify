package com.example.splitify.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.TripRepository
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tripRepository: TripRepository,
    private val sessionManager: SessionManager,
    private val tripDao: TripDao
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState : StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String){
        _uiState.update { it.copy(
            email=email,
            emailError = null,
            errorMessage = null)
        }
    }

    fun onPasswordChange(password: String){
        _uiState.update { it.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        ) }
    }

    fun login() {
        val state = _uiState.value

        // Validation
        if (!isValidEmail(state.email)) {
            _uiState.update { it.copy(
                emailError = "Invalid email format"
            ) }
            return
        }

        if (state.password.length < 6) {
            _uiState.update { it.copy(
                passwordError = "Password must be at least 6 characters long"
            ) }
            return
        }

        _uiState.update { it.copy(
            isLoading = true,
            errorMessage = null
        ) }

        viewModelScope.launch {
            when (val result = authRepository.signIn(state.email, state.password)) {
                is Result.Success -> {
                    Log.d("LoginVM", "✅ Login successful")

                    // SMART DOWNLOAD LOGIC
                    handlePostLoginDataSync()

                    // ✅ Complete login (UI navigates away)
                    _uiState.update { it.copy(
                        isLoading = false,
                        isLoggedIn = true
                    ) }
                }

                is Result.Error -> {
                    Log.e("LoginVM", "❌ Login failed: ${result.message}")
                    val message = result.message.lowercase()

                    when {
                        // No internet / DNS issue
                        message.contains("unable to resolve host") ||
                                message.contains("no address associated with hostname") -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "No internet connection. Please check your network and try again."
                                )
                            }
                        }

                        // Email already registered
                        message.contains("invalid login credential") -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Incorrect login id or password"
                                )
                            }
                        }

                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Something went wrong. Please try again."
                                )
                            }
                        }
                    }
                }

                Result.Loading -> {
                    // Keep loading
                }
            }
        }
    }

    private suspend fun handlePostLoginDataSync() {
        try {
            // 1. Get current user ID
            val userId = sessionManager.getCurrentUserId()
            if (userId == null) {
                Log.w("LoginVM", "⚠️ No user ID after login (shouldn't happen)")
                return
            }

            Log.d("LoginVM", "👤 Checking local data for user: $userId")

            // 2. Check if we have local data
            val localTrips = tripDao.getTripsByUser(userId).first()

            if (localTrips.isNotEmpty()) {
                // ✅ Scenario 2: Returning user, same device
                Log.d("LoginVM", "✅ Found ${localTrips.size} trips locally - SKIP DOWNLOAD")
                Log.d("LoginVM", "📱 Returning user on same device")
                return
            }

            // ✅ Scenario 3: New user OR old user on new device
            Log.d("LoginVM", "📭 No local data found")
            Log.d("LoginVM", "📥 Downloading from server...")

            when (val result = tripRepository.downloadTripsFromSupabase()) {
                is Result.Success -> {
                    Log.d("LoginVM", "✅ Download completed")
                }
                is Result.Error -> {
                    Log.e("LoginVM", "❌ Download failed: ${result.message}")
                    // Don't block login
                }
                Result.Loading -> Unit
            }

        } catch (e: Exception) {
            Log.e("LoginVM", "❌ Post-login sync failed", e)
            // Don't block login
        }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is Result.Success -> {
                    // Sync trips from Supabase after successful login
                    handlePostLoginDataSync()
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                }
                is Result.Error -> {
                    val message = result.message.lowercase()

                    val errorMsg = when {
                        message.contains("network") ->
                            "No internet connection. Please check your network."
                        message.contains("invalid") ->
                            "Google sign-in failed. Please try again."
                        else -> result.message
                    }

                    _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address") }
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            passwordResetEmailSent = true,
                            successMessage = "Password reset link sent! Check your email."
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Failed to send reset email"
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }


    private fun isValidEmail(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}
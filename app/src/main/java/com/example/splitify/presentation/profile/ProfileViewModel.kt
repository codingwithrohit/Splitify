package com.example.splitify.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.data.local.SessionManager
import com.example.splitify.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val userId: String = ""
)

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val currency: String = "USD",
    val theme: String = "System"
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState.asStateFlow()

    init {
        loadUserProfile()
        loadAppSettings()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val name = sessionManager.getUserName() ?: "User"
                val email = sessionManager.getUserEmail() ?: ""
                val userId = sessionManager.getCurrentUserId() ?: ""

                _userProfile.value = UserProfile(
                    name = name,
                    email = email,
                    userId = userId
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadAppSettings() {
        // Load from SharedPreferences or DataStore
        // For now, using default values
    }

    fun updateNotifications(enabled: Boolean) {
        _appSettings.value = _appSettings.value.copy(notificationsEnabled = enabled)
        // Save to SharedPreferences
    }

    fun updateCurrency(currency: String) {
        _appSettings.value = _appSettings.value.copy(currency = currency)
        // Save to SharedPreferences
    }

    fun updateTheme(theme: String) {
        _appSettings.value = _appSettings.value.copy(theme = theme)
        // Save to SharedPreferences
    }

    fun logout(onLogOut: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                sessionManager.clearSession()
                onLogOut()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.Loading
            try {
                // Call your delete account API
                // authRepository.deleteAccount()
                sessionManager.clearSession()
                _deleteAccountState.value = DeleteAccountState.Success
                onSuccess()
            } catch (e: Exception) {
                _deleteAccountState.value = DeleteAccountState.Error(
                    e.message ?: "Failed to delete account"
                )
            }
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = DeleteAccountState.Idle
    }
}

sealed class DeleteAccountState {
    object Idle : DeleteAccountState()
    object Loading : DeleteAccountState()
    object Success : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}
package com.example.splitify.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.data.local.SessionManager
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.UserRepository
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val userId: String = "",
    val avatarUrl: String? = null
)

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val currency: String = "USD",
    val theme: String = "System"
)

sealed class ProfilePictureState {
    object Idle : ProfilePictureState()
    object Uploading : ProfilePictureState()
    object Success : ProfilePictureState()
    data class Error(val message: String) : ProfilePictureState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _deleteAccountState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountState> = _deleteAccountState.asStateFlow()

    private val _profilePictureState = MutableStateFlow<ProfilePictureState>(ProfilePictureState.Idle)
    val profilePictureState: StateFlow<ProfilePictureState> = _profilePictureState.asStateFlow()

    init {
        loadUserProfile()
        loadAppSettings()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val name = sessionManager.getFullName()?.takeIf { it.isNotBlank() }
                    ?: sessionManager.getUserName()?.takeIf { it.isNotBlank() }
                    ?: "User"
                val email = sessionManager.getUserEmail() ?: ""
                val userId = sessionManager.getUserId() ?: ""
                val avatarUrl = sessionManager.getAvatarUrl()

                _userProfile.value = UserProfile(
                    name = name,
                    email = email,
                    userId = userId,
                    avatarUrl = avatarUrl
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

    fun uploadProfilePicture(imageFile: File) {
        viewModelScope.launch {
            _profilePictureState.value = ProfilePictureState.Uploading

            val userId = _userProfile.value.userId
            if (userId.isBlank()) {
                _profilePictureState.value = ProfilePictureState.Error("User not found")
                return@launch
            }

            when (val result = userRepository.uploadProfilePicture(userId, imageFile)) {
                is Result.Success -> {
                    val avatarUrl = result.data

                    // Update user profile in database
                    when (val updateResult = userRepository.updateUserProfile(userId, avatarUrl)) {
                        is Result.Success -> {
                            _userProfile.value = _userProfile.value.copy(avatarUrl = avatarUrl)
                            _profilePictureState.value = ProfilePictureState.Success
                        }
                        is Result.Error -> {
                            _profilePictureState.value = ProfilePictureState.Error(
                                updateResult.exception.message ?: "Failed to update profile"
                            )
                        }
                        is Result.Loading -> {}
                    }
                }
                is Result.Error -> {
                    _profilePictureState.value = ProfilePictureState.Error(
                        result.exception.message ?: "Upload failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch {
            _profilePictureState.value = ProfilePictureState.Uploading

            val userId = _userProfile.value.userId
            when (val result = userRepository.deleteProfilePicture(userId)) {
                is Result.Success -> {
                    _userProfile.value = _userProfile.value.copy(avatarUrl = null)
                    _profilePictureState.value = ProfilePictureState.Success
                }
                is Result.Error -> {
                    _profilePictureState.value = ProfilePictureState.Error(
                        result.exception.message ?: "Delete failed"
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    fun resetProfilePictureState() {
        _profilePictureState.value = ProfilePictureState.Idle
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
                onLogOut()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.Loading
            when (val result = authRepository.changePassword(currentPassword, newPassword)) {
                is Result.Success -> {
                    _deleteAccountState.value = DeleteAccountState.Idle
                    onSuccess()
                }
                is Result.Error -> {
                    _deleteAccountState.value = DeleteAccountState.Error(
                        result.exception.message ?: "Failed to change password"
                    )
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteAccountState.value = DeleteAccountState.Loading
            when (val result = authRepository.deleteAccount()) {
                is Result.Success -> {
                    _deleteAccountState.value = DeleteAccountState.Success
                    onSuccess()
                }
                is Result.Error -> {
                    _deleteAccountState.value = DeleteAccountState.Error(
                        result.exception.message ?: "Failed to delete account"
                    )
                }
                is Result.Loading -> Unit
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
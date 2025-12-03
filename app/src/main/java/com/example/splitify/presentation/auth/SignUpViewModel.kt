package com.example.splitify.presentation.auth

import androidx.compose.runtime.saveable.autoSaver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(fullName: String){
        _uiState.update { it.copy(
            fullName = fullName,
            errorMessage = null
        ) }
    }

    fun onUserNameChange(userName: String){
        _uiState.update { it.copy(
            userName = userName,
            userNameError = null,
            errorMessage = null
        ) }
    }

    fun onEmailChange(email: String){
        _uiState.update { it.copy(
            email = email,
            emailError = null,
            errorMessage = null
        ) }
    }

    fun onPasswordChange(password: String){
        _uiState.update { it.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        ) }
    }

    fun onConfirmPasswordChange(confirmPassword: String){
        _uiState.update { it.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            errorMessage = null
        ) }
    }

    fun signUp() {
        val state = _uiState.value

        //Validations
        if (state.userName.length < 3) {
            _uiState.update { it.copy(userNameError = "Username must be at least 3 characters") }
            return
        }

        if (!isValidate(state.email)) {
            _uiState.update {
                it.copy(
                    emailError = "Invalid email format"
                )
            }
            return
        }

        if (state.password.length < 6) {
            _uiState.update {
                it.copy(
                    passwordError = "Password must be at least 6 characters long"
                )
            }
            return
        }

        if(state.confirmPassword != state.password){
            _uiState.update { it.copy(
                confirmPasswordError = "Passwords do no match"
            ) }
            return
        }

        _uiState.update { it.copy(
            isLoading = true,
            errorMessage = null
        ) }

        viewModelScope.launch {
            val result = authRepository.signUp(
                email = state.email,
                password = state.password,
                userName = state.userName,
                fullName = state.fullName)
            when(result){
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        isSignedUp = true
                    ) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    ) }
                }
            }
        }

    }


    private fun isValidate(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

package com.example.splitify.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
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

    fun login(){
        val state = _uiState.value

        //Validation
        if(!isValidate(state.email)){
            _uiState.update { it.copy(
                emailError = "Invalid email format"
            ) }
            return
        }
        if(state.password.length < 6){
            _uiState.update { it.copy(
                passwordError = "Password must be at least 6 characters long"
            ) }
        }

        _uiState.update { it.copy(
            isLoading = true,
            errorMessage = null
        ) }

        viewModelScope.launch {
            val result = authRepository.signIn(state.email, state.password)
            when(result){
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        isLoggedIn = true
                    ) }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = result.message
                    ) }
                }

                Result.Loading -> TODO()
            }
        }
    }

    private fun isValidate(email: String): Boolean{
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}
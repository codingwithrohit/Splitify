package com.example.splitify.presentation.auth

data class SignUpUiState(
    val fullName: String = "",
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",

    val userNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignedUp: Boolean = false
)
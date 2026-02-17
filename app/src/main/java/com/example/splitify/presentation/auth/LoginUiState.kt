package com.example.splitify.presentation.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",

    val emailError: String? = null,
    val passwordError: String? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val successMessage: String? = null,
    val passwordResetEmailSent: Boolean = false,
    val passwordUpdateSuccess: Boolean = false
)
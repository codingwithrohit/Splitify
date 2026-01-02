package com.example.splitify.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackbarController(
    private val snackbarHost: SnackbarHostState,
    private val scope: CoroutineScope
){
    fun showSuccess(message: String){
        scope.launch {
            snackbarHost.showSnackbar(
                message = "✓ $message",
                duration = SnackbarDuration.Short
            )
        }
    }

    fun showError(message: String){
        scope.launch {
            snackbarHost.showSnackbar(
                message = "⚠ $message",
                duration = SnackbarDuration.Long
            )
        }
    }

    fun showInfo(message: String){
        scope.launch {
            snackbarHost.showSnackbar(
                message = "ℹ️ $message",
                duration = SnackbarDuration.Short
            )
        }
    }

    fun showWithAction(
        message: String,
        actionLabel: String,
        onAction: () -> Unit
    ){
        scope.launch{
            val result = snackbarHost.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Long
            )
            if(result == SnackbarResult.ActionPerformed){
                onAction()
            }
        }
    }

}
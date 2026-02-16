package com.example.splitify.util

import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.SnackbarDuration
import com.example.splitify.domain.model.NotificationType

data class NotificationSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    override val withDismissAction: Boolean = true,
    val notificationType: NotificationType
) : SnackbarVisuals
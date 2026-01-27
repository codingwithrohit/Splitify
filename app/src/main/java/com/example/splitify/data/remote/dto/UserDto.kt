package com.example.splitify.data.remote.dto

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
)
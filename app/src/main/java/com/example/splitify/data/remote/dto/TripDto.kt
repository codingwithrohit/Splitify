package com.example.splitify.data.remote.dto

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class TripDto(
    val id: String,
    val name: String,
    val description: String?,

    @SerialName("created_by")  // Maps to 'created_by' column
    val createdBy: String,

    @SerialName("invite_code")  // Maps to 'invite_code' column
    val inviteCode: String,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("start_date")  // Maps to 'start_date' column
    val startDate: LocalDate,
    @Serializable(with = LocalDateSerializer::class)
    @SerialName("end_date")  // Maps to 'end_date' column
    val endDate: LocalDate?,
    @SerialName("created_at")
    val createdAt: String? = null, // Format: "2026-01-13T13:22:45.123Z"
    @SerialName("updated_at")
    val updatedAt: String? = null

)


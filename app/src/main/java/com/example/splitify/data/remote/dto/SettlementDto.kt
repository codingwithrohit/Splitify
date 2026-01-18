package com.example.splitify.data.remote.dto

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SettlementDto(
    val id: String,

    @SerialName("trip_id")
    val tripId: String,

    @SerialName("from_member_id")
    val fromMemberId: String,

    @SerialName("to_member_id")
    val toMemberId: String,

    val amount: Double,

    val status: String,

    val notes: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("settled_at")
    val settledAt: String? = null
)
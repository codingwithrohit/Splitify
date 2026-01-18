package com.example.splitify.data.remote.dto

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ExpenseSplitDto(
    val id: String,

     @SerialName("expense_id")
    val expenseId: String,

    @SerialName("member_id")
    val memberId: String,

    @SerialName("member_name")
    val memberName: String,

    @SerialName("amount_owed")
    val amountOwed: Double,

    @SerialName("created_at")
    val createdAt: String? = null
)
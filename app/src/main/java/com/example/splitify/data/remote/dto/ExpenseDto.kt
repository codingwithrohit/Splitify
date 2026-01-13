package com.example.splitify.data.remote.dto

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ExpenseDto(
    val id: String,

    @SerialName("trip_id")
    val tripId: String,

    val description: String,

    val amount: Double,  // Supabase DECIMAL → Kotlin Double

    val category: String,

    @Serializable(with = LocalDateSerializer::class)
    @SerialName("expense_date")
    val expenseDate: LocalDate,

    @SerialName("paid_by")
    val paidBy: String,  // trip_member.id (UUID)

    @SerialName("created_by")
    val createdBy: String,  // user.id (UUID)

    @SerialName("paid_by_name")
    val paidByName: String,

    @SerialName("is_group_expense")
    val isGroupExpense: Boolean,

    @SerialName("created_at")
    val createdAt: String? = null,  // ISO timestamp

    @SerialName("updated_at")
    val updatedAt: String? = null
)
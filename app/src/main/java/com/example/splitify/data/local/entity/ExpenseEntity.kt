package com.example.splitify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,

    val tripId: String,
    val description: String,
    val amount: Double,
    val category: String,
    val expenseDate: LocalDate,
    val paidBy: String,
    val paidByName: String,
    val isGroupExpense: Boolean,
    val createdAt: Long,

    //Sync fields
    val isLocal: Boolean = false,
    /* isLocal = true, it means the expense is stored locally only on the device and not synced to supabase
    isLocal = false it means the expense is synced to supabase and is stored locally
     */
    val isSynced: Boolean = true,
    /* isSynced = true, i.e. the local row matches with server row,
    isSynced = false, i.e. the local row has changes not yet uploaded to database
     */
    val lastModified: Long = System.currentTimeMillis()
)
package com.example.splitify.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["paid_by"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["paid_by"]),
        Index(value = ["created_by"]),
        Index(value = ["expense_date"])
    ])
data class ExpenseEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo("trip_id")
    val tripId: String,

    val description: String,
    val amount: Double,
    val category: String,

    @ColumnInfo("expense_date")
    val expenseDate: LocalDate,

    @ColumnInfo("paid_by")
    val paidBy: String,

    @ColumnInfo("created_by")
    val createdBy: String,

    @ColumnInfo("paid_by_name")
    val paidByName: String,

    @ColumnInfo("is_group_expense")
    val isGroupExpense: Boolean,

    @ColumnInfo("created_at")
    val createdAt: Long,

    @ColumnInfo("updated_at")
    val updatedAt: Long? = null,

    //Sync fields
    @ColumnInfo("is_local")
    val isLocal: Boolean = true,
    /* isLocal = true, it means the expense is stored locally only on the device and not synced to supabase
    isLocal = false it means the expense is synced to supabase and is stored locally
     */
    @ColumnInfo("is_synced")
    val isSynced: Boolean = false,
    /* isSynced = true, i.e. the local row matches with server row,
    isSynced = false, i.e. the local row has changes not yet uploaded to database
     */
    @ColumnInfo("last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
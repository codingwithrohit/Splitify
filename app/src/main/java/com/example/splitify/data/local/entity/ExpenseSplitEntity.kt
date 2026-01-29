package com.example.splitify.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "expense_splits",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expense_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["member_id"],
            onDelete = ForeignKey.CASCADE,
            deferred = true
        )
    ],
    indices = [
        Index(value = ["expense_id"]),
        Index(value = ["member_id"])
    ]
)
data class ExpenseSplitEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("expense_id")
    val expenseId: String,
    @ColumnInfo("member_id")
    val memberId: String,
    @ColumnInfo("member_name")
    val memberName: String,
    @ColumnInfo("amount_owed")
    val amountOwed: Double,
    @ColumnInfo("created_at")
    val createdAt: Long = System.currentTimeMillis()
)

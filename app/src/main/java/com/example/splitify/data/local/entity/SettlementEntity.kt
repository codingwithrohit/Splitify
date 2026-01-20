package com.example.splitify.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.splitify.domain.model.SettlementStatus

@Entity(
    tableName = "settlements",
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
            childColumns = ["from_member_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TripMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["to_member_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["from_member_id"]),
        Index(value = ["to_member_id"]),
        Index(value = ["status"])
    ])
data class SettlementEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("trip_id")
    val tripId: String,
    @ColumnInfo("from_member_id")
    val fromMemberId: String,
    @ColumnInfo("to_member_id")
    val toMemberId: String,
    val amount: Double,
    val status: String,
    val notes: String? = null,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("settled_at")
    val settledAt: Long? = null

)

package com.example.splitify.data.local.entity

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
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromMemberId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TripMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["toMemberId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["tripId"]),
        Index(value = ["fromMemberId"]),
        Index(value = ["toMemberId"]),
        Index(value = ["status"])
    ])
data class SettlementEntity(
    @PrimaryKey
    val id: String,
    val tripId: String,
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Double,
    val status: String,
    val notes: String? = null,
    val createdAt: Long,
    val settledAt: Long? = null
)

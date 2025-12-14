package com.example.splitify.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "trip_members",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["trip_id"]),
        Index(value = ["user_id"])
    ]
)
data class TripMemberEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("trip_id")
    val tripId: String,
    @ColumnInfo("user_id")
    val userId: String?,
    @ColumnInfo("display_name")
    val displayName: String,
    @ColumnInfo("role")
    val role: String,
    @ColumnInfo("joined_at")
    val joinedAt: Long,
    @ColumnInfo("avatar_url")
    val avatarUrl: String? = null,
    @ColumnInfo("is_synced")
    val isSynced: Boolean = false,
    @ColumnInfo("last_modified")
    val lastModified: Long = System.currentTimeMillis()
)

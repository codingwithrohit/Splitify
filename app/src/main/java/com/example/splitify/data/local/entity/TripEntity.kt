package com.example.splitify.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String,

    val name: String,
    val description: String? = null,
    @ColumnInfo("start_date")
    val startDate: LocalDate,
    @ColumnInfo("end_date")
    val endDate: LocalDate? = null,
    @ColumnInfo("invite_code")
    val inviteCode: String,
    @ColumnInfo("created_by")
    val createdBy: String,
    @ColumnInfo("created_at")
    val createdAt: Long,

    //Sync related fields
    @ColumnInfo("is_local")
    val isLocal: Boolean = false,  // Not yet synced to Supabase
    @ColumnInfo("is_synced")
    val isSynced: Boolean = true,
    @ColumnInfo("last_modified")
    val lastModified: Long = System.currentTimeMillis()
)
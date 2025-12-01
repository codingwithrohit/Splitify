package com.example.splitify.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String,

    val name: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val inviteCode: String,
    val createdBy: String,
    val createdAt: Long,

    //Sync related fields
    val isLocal: Boolean = false,  // Not yet synced to Supabase
    val isSynced: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
)
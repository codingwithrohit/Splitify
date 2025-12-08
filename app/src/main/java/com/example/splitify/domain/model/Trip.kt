package com.example.splitify.domain.model

import java.time.LocalDate


data class Trip(
    val id: String,
    val name: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? ,
    val inviteCode: String,
    val createdBy: String, //User id
    val createdAt: Long = System.currentTimeMillis(),
    val isLocal: Boolean = false
)
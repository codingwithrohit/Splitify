package com.example.splitify.presentation.trips

import java.time.LocalDate

data class CreateTripUiState (
    val name: String = "",
    val description: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val inviteCode: String = "",

    val nameError: String? = null,
    val dateError: String? = null,

    val isLoading: Boolean = false,
    val isSaved: Boolean = false,

    val createdTripId: String? = null

)
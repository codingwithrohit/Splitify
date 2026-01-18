package com.example.splitify.presentation.trips

sealed class CreateTripFormMode {

    data object CreateTrip: CreateTripFormMode()

    data class EditTrip(val tripId: String?): CreateTripFormMode()
}
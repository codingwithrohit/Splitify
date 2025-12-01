package com.example.splitify.domain.repository

import com.example.splitify.domain.model.Trip
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow

interface TripRepository {

    fun getAllTrips(): Flow<List<Trip>>

    suspend fun getTripById(tripId: String): Trip?

    suspend fun createTrip(trip: Trip): Result<Trip>

    suspend fun deleteTrip(tripId: String): Result<Unit>

    suspend fun updateTrip(trip: Trip): Result<Unit>

    suspend fun syncTrips(): Result<Unit>


}
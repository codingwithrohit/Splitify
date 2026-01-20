package com.example.splitify.domain.repository

import com.example.splitify.domain.model.Trip
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.Flow

interface TripRepository {

    fun getAllTrips(): Flow<List<Trip>>

    suspend fun getUserTripIds(userId: String): List<String>

    fun getTripsByUser(userId: String): Flow<List<Trip>>

    suspend fun getTripById(tripId: String): Trip?

    fun observeTripById(tripId: String):  Flow<Result<Trip>>

    suspend fun createTrip(trip: Trip): Result<Trip>

    suspend fun deleteTrip(tripId: String): Result<Unit>

    suspend fun updateTrip(trip: Trip): Result<Unit>

    suspend fun syncTrips(): Result<Unit>

    suspend fun clearLocalTrips(): Result<Unit>

    suspend fun downloadTripsFromSupabase(): Result<Unit>


}
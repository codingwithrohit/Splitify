package com.example.splitify.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.splitify.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow


/**
 * Data Access Object for Trip operations
 * Contains all database queries for trips
 */
@Dao
interface TripDao {

     /* Get all trips ordered by start date (newest first)
        Returns Flow - updates automatically when data changes */
    @Query("Select * from trips ORDER BY startDate DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    // Select a single trip by Id, returns null if not found
    @Query("Select * from trips WHERE id = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?

    // Get all trips created by a specific user
    @Query("Select * From trips WHERE createdBy = :userId ORDER BY startDate DESC")
    fun getTripsByUser(userId: String): Flow<List<TripEntity>>

    // Get trips that haven't been synced to Supabase yet
    @Query("Select * From trips WHERE isLocal = 1")
    suspend fun getUnsyncedTrips(): List<TripEntity>

    // Insert a new trip, If trip with same Id exists, replace it
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    // Insert multiple trips at once, useful for syncing with database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    // Update an existing trip
    @Update
    suspend fun updateTrip(trip: TripEntity)

    // Delete a trip
    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    // Delete trip by Id
    @Query("Delete From trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)

    // Delete all trips - for testing and logout
    @Query("Delete From trips")
    suspend fun deleteAllTrips()

}
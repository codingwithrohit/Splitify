package com.example.splitify.data.sync

import android.util.Log
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.example.splitify.util.Result

@Singleton
class TripSyncUseCase @Inject constructor(
    private val tripMemberRepository: TripMemberRepository,
    private val expenseRepository: ExpenseRepository
) {

    suspend operator fun invoke(tripId: String): Result<Unit> {
        return try {
            Log.d("TripSyncUseCase", "🔄 Syncing trip: $tripId")

            // Pull members first (expenses reference them)
            tripMemberRepository.fetchAndSaveRemoteMembers(tripId)

            // Pull expenses and splits
            expenseRepository.fetchAndSaveRemoteExpenses(tripId)

            Log.d("TripSyncUseCase", "✅ Trip synced successfully")
            Result.Success(Unit)

        } catch (e: Exception) {
            Log.e("TripSyncUseCase", "❌ Sync failed", e)
            Result.Error(e)
        }
    }

}
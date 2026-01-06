package com.example.splitify.domain.usecase.balance

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.splitify.domain.model.Balance
import javax.inject.Inject
import com.example.splitify.util.Result
import kotlinx.coroutines.flow.first

class GetBalanceForMemberUseCase @Inject constructor(
    private val calculateTripBalancesUseCase: CalculateTripBalancesUseCase
) {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    suspend operator fun invoke(tripId: String, memberId: String): Result<Balance?> {
        return when (
            val result = calculateTripBalancesUseCase(tripId).first()
        ) {
            is Result.Success -> {
                val balance = result.data.getBalanceForMember(memberId)
                Result.Success(balance)
            }

            is Result.Error ->
                Result.Error(Exception(result.message))

            else ->
                Result.Error(Exception("Failed to calculate balance"))
        }
    }
}
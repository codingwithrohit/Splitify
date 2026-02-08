package com.example.splitify.di

import com.example.splitify.data.sync.TripSyncUseCase
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideTripSyncUseCase(
        tripMemberRepository: TripMemberRepository,
        expenseRepository: ExpenseRepository
    ): TripSyncUseCase {
        return TripSyncUseCase(tripMemberRepository, expenseRepository)
    }


}
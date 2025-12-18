package com.example.splitify.di

//import com.example.splitify.data.repository.AuthRepositoryImpl
import com.example.splitify.data.repository.AuthRepositoryImpl
import com.example.splitify.data.repository.ExpenseRepositoryImpl
import com.example.splitify.data.repository.TripMemberRepositoryImpl
import com.example.splitify.data.repository.TripRepositoryImpl
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.ExpenseRepository
import com.example.splitify.domain.repository.TripMemberRepository
//import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.domain.repository.TripRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    //Binds TripRepositoryImpl to TripRepository Interface
    //When someone needs TripRepository, Hilt provides TripRepositoryImpl
    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindTripMemberRepository(
        tripMemberRepositoryImpl: TripMemberRepositoryImpl
    ): TripMemberRepository




}
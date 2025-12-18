package com.example.splitify.di

import android.content.Context
import androidx.room.Room
import com.example.splitify.data.local.AppDatabase
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.dao.TripMemberDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /* Provides the Room database instance
    @Singleton means only one instance exists in the entire app */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration() // Destroys old database on version change
            .build()
    }

    // Provide the TripDao from database
    @Provides
    @Singleton
    fun provideTripDao(database: AppDatabase): TripDao{
        return database.tripDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: AppDatabase): ExpenseDao{
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideTripMemberDao(database: AppDatabase): TripMemberDao{
        return database.tripMemberDao()
    }
    @Provides
    @Singleton
    fun provideExpenseSplitDao(database: AppDatabase): ExpenseSplitDao{
        return database.expenseSplitDao()
    }
}
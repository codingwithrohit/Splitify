package com.example.splitify.di

import android.content.Context
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.repository.UserRepositoryImpl
import com.example.splitify.domain.repository.UserRepository
import com.example.splitify.util.NotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager{
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        supabase: SupabaseClient,
        sessionManager: SessionManager
    ): UserRepository {
        return UserRepositoryImpl(supabase, sessionManager)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(): NotificationManager {
        return NotificationManager()
    }
}
package com.example.splitify.di

import android.content.Context
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.repository.UserRepositoryImpl
import com.example.splitify.domain.repository.UserRepository
import com.example.splitify.util.AppForegroundObserver
import com.example.splitify.util.NotificationManager
import com.example.splitify.util.SystemNotificationManager
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
    fun provideAppForegroundObserver(): AppForegroundObserver {
        return AppForegroundObserver()
    }


    @Provides
    @Singleton
    fun provideSystemNotificationManager(
        @ApplicationContext context: Context
    ): SystemNotificationManager {
        return SystemNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        systemNotificationManager: SystemNotificationManager,
        appForegroundObserver: AppForegroundObserver
    ): NotificationManager {
        return NotificationManager(
            systemNotificationManager,
            appForegroundObserver
        )
    }
}
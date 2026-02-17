package com.example.splitify.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by
preferencesDataStore(name = "session_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val FULL_NAME = stringPreferencesKey("full_name")
        val AVATAR_URL = stringPreferencesKey("avatar_url")
        val TOKEN_EXPIRY = longPreferencesKey("token_expiry")
    }


    suspend fun hasValidSession(): Boolean {
        val prefs = context.sessionDataStore.data.first()
        return !prefs[Keys.ACCESS_TOKEN].isNullOrBlank()
                && !prefs[Keys.USER_ID].isNullOrBlank()
    }


    suspend fun getUserName(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.USER_NAME]
    }

    suspend fun getUserEmail(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.USER_EMAIL]
    }

    suspend fun getFullName(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.FULL_NAME]
    }

    suspend fun getAvatarUrl(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.AVATAR_URL]
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long,
        userId: String,
        userName: String,
        userEmail: String,
        fullName: String?,
        avatarUrl: String?
    ) {
        val expiryTimeMillis = System.currentTimeMillis() + (expiresIn * 1000)

        context.sessionDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            prefs[Keys.TOKEN_EXPIRY] = expiryTimeMillis

            prefs[Keys.USER_ID] = userId
            prefs[Keys.USER_NAME] = userName
            prefs[Keys.USER_EMAIL] = userEmail
            prefs[Keys.FULL_NAME] = fullName ?: ""
            prefs[Keys.AVATAR_URL] = avatarUrl ?: ""
        }
    }

    suspend fun updateTokens(
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        val expiryTimeMillis = System.currentTimeMillis() + (expiresIn * 1000)

        context.sessionDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            prefs[Keys.TOKEN_EXPIRY] = expiryTimeMillis
        }
    }

    suspend fun needsTokenRefresh(): Boolean {
        val prefs = context.sessionDataStore.data.first()
        val expiryTime = prefs[Keys.TOKEN_EXPIRY] ?: return true

        val now = System.currentTimeMillis()
        val refreshWindowMillis = 5 * 60 * 1000

        return (expiryTime - now) <= refreshWindowMillis
    }

    suspend fun getAccessToken(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.ACCESS_TOKEN]
    }

    suspend fun getCurrentUserId(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.USER_ID]
    }

    suspend fun getRefreshToken(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.REFRESH_TOKEN]
    }

    suspend fun updateAvatarUrl(avatarUrl: String?) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.AVATAR_URL] = avatarUrl ?: ""
        }
    }

    suspend fun getUserId(): String? {
        val prefs = context.sessionDataStore.data.first()
        return prefs[Keys.USER_ID]
    }


    fun getCurrentUserFlow(): Flow<UserSession?> {
        return context.sessionDataStore.data.map { prefs ->
            val userId = prefs[Keys.USER_ID] ?: return@map null

            UserSession(
                userId = userId,
                email = prefs[Keys.USER_EMAIL] ?: "",
                userName = prefs[Keys.USER_NAME] ?: "",
                fullName = prefs[Keys.FULL_NAME],
                avatarUrl = prefs[Keys.AVATAR_URL]
            )
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }

    data class UserSession(
        val userId: String,
        val email: String,
        val userName: String,
        val fullName: String?,
        val avatarUrl: String?
    )
}

package com.example.splitify.data.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.room.withTransaction
import com.example.splitify.data.local.AppDatabase
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.dao.SettlementDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.dao.TripMemberDao
import com.example.splitify.data.remote.dto.UserDto
import com.example.splitify.data.remote.toDomainModel
import com.example.splitify.domain.model.User
import com.example.splitify.domain.repository.AuthRepository
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import javax.inject.Inject
import com.example.splitify.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@SuppressLint("UnsafeOptInUsageError")
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager,
    private val database: AppDatabase,
    private val tripDao: TripDao,
    private val tripMemberDao: TripMemberDao,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao,
    private val settlementDao: SettlementDao
): AuthRepository {

    override suspend fun initializeSession(): Boolean {
        val hasLocalSession = sessionManager.hasValidSession()
        if (!hasLocalSession) return false

        // Token refresh is best-effort and must never affect UI state
        if (sessionManager.needsTokenRefresh()) {
            try {
                refreshTokens()
            } catch (_: Exception) {
                // Ignore refresh failures (offline / timeout)
            }
        }

        return true
    }


    private suspend fun refreshTokens(): Boolean {
        val refreshToken = sessionManager.getRefreshToken() ?: return false

        return try {
            supabase.auth.refreshSession(refreshToken)

            val session = supabase.auth.currentSessionOrNull() ?: return false

            sessionManager.updateTokens(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken ?: "",
                expiresIn = session.expiresIn
            )

            true
        } catch (e: Exception) {
            // Do not clear session here
            false
        }
    }


    override fun getCurrentUser(): Flow<User?> = flow {
        val session = sessionManager.getCurrentUserFlow()
        session.collect { userSession ->
            if (userSession != null) {
                emit(User(
                    id = userSession.userId,
                    email = userSession.email,
                    userName = userSession.userName,
                    fullName = userSession.fullName,
                    avatarUrl = userSession.avatarUrl,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = null
                ))
            } else {
                emit(null)
            }
        }
    }


    override suspend fun isLoggedIn(): Boolean {
        return sessionManager.hasValidSession()
    }

    override suspend fun signUp(
        email: String,
        password: String,
        userName: String,
        fullName: String
    ): Result<User> {
        return try {
            //1. sign up with supabase auth
            supabase.auth.signUpWith(Email){
                this.email = email
                this.password = password
            }
            //2. Get current session (contain tokens)
            val session = supabase.auth.currentSessionOrNull() ?:
            return Exception("Failed to get session").asError()

            val userId = session.user?.id
                ?: return Exception("Failed to get user id").asError()

            //3. Create user profile in database
            val userDto = UserDto(
                id = userId,
                email = email,
                username = userName,
                full_name = fullName
            )
            supabase.from("users").insert(userDto)

            //4. Save session token
            sessionManager.saveSession(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                expiresIn = session.expiresIn,
                userId = userId,
                userName = userName,
                userEmail = email,
                fullName = fullName,
                avatarUrl = null
            )

            Log.d("AuthRepo", "✅ Sign up successful, session saved")

            userDto.toDomainModel().asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }

    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<User> {
        return try {
            supabase.auth.signInWith(Email){
                this.email = email
                this.password = password
            }
            val session = supabase.auth.currentSessionOrNull()
                ?: return Exception("Failed to get session").asError()

            val userId = session.user?.id
                ?: return Exception("Failed to get user ID").asError()

            val userProfile = supabase.from("users")
                .select()
                .decodeList<UserDto>()
                .firstOrNull{it.id == userId}
                ?: return Exception("User profile not found").asError()

            sessionManager.saveSession(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                userId = userId,
                userEmail = userProfile.email,
                userName = userProfile.username,
                fullName = userProfile.full_name,
                avatarUrl = userProfile.avatar_url,
                expiresIn = session.expiresIn
            )
            Log.d("AuthRepo:", "Saved Session detail: " +
                    "accessToken = ${session.accessToken} " +
                    "refreshToken = ${session.refreshToken} " +
                    "userId = ${session.user?.id}" +
                    "userName = ${userProfile.username}" +
                    "userEmail = ${userProfile.email}" +
                    "fullName = ${userProfile.full_name}"
            )
            Log.d("AuthRepo", "✅ Sign in successful, session saved")
            userProfile.toDomainModel().asSuccess()
        }
        catch (e: Exception){
            e.asError()
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return try {
            val userProfile = supabase.from("users")
                .select()
                .decodeList<UserDto>()
                .firstOrNull() {it.id == userId}

            userProfile?.toDomainModel().asSuccess()
        }
        catch (e: Exception) {
            e.asError()
        }
    }


    override suspend fun signOut(): Result<Unit> {
        return try {
            supabase.auth.signOut()

            val userId = sessionManager.getCurrentUserId() ?: ""

            database.withTransaction {
                expenseSplitDao.deleteAllSplits()
                expenseDao.deleteAllExpense()
                settlementDao.deleteAllSettlements()
                tripMemberDao.deleteAllMembers()
                tripDao.deleteAllTrips()
            }

            val remainingTrips = tripDao.getTripIdsByUser(userId).size
            if (remainingTrips == 0) {
                Log.d("AuthRepo", "Database cleared successfully")
            } else {
                Log.d("AuthRepo", "Warning: $remainingTrips trips still remain in DB")
            }

            sessionManager.clearSession()

            Unit.asSuccess()
        } catch (e: Exception) {
            e.asError()
        }
    }

    override fun searchUsersByUsername(query: String): Flow<Result<List<User>>> = flow {
        try {
            emit(Result.Loading)

            val users = supabase.from("users")
                .select {
                    filter {
                        or {
                            ilike("username", "%$query%")
                            ilike("full_name", "%$query%")
                        }
                    }
                }
                .decodeList<UserDto>()
                .take(20)  // Limit results

            emit(Result.Success(users.map { it.toDomainModel() }))
        } catch (e: Exception) {
            emit(Result.Error(e, "Search failed: ${e.message}"))
        }
    }

}
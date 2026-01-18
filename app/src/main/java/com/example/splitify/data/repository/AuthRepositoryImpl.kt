package com.example.splitify.data.repository

import android.annotation.SuppressLint
import android.util.Log
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.local.dao.ExpenseDao
import com.example.splitify.data.local.dao.ExpenseSplitDao
import com.example.splitify.data.local.dao.TripDao
import com.example.splitify.data.local.dao.TripMemberDao
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

@SuppressLint("UnsafeOptInUsageError")
class AuthRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager,
    private val tripDao: TripDao,
    private val tripMemberDao: TripMemberDao,
    private val expenseDao: ExpenseDao,
    private val expenseSplitDao: ExpenseSplitDao
): AuthRepository {

    override suspend fun initializeSession(): Boolean {
        return try {
            Log.d("AuthRepo", "Initializing session...")
            val accessToken = sessionManager.getAccessToken()
            val refreshToken = sessionManager.getRefreshToken()
            if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
                Log.d("AuthRepo", "No stored tokens")
                return false
            }
            try {
                supabase.auth.retrieveUser(accessToken)
                Log.d("AuthRepo", "✅ Session restored successfully")

                // Check if token needs refresh
                if (sessionManager.needsTokenRefresh()) {
                    Log.d("AuthRepo", "🔄 Token expiring soon, refreshing...")
                    refreshTokens()
                }

                true
            } catch (e: Exception) {
                Log.e("AuthRepo", " Session restore failed, trying refresh...", e)
                // Try to refresh with refresh token
                refreshTokens()
            }

        }catch (e: Exception) {
            Log.e("AuthRepo", " Session initialization failed", e)
            false
        }
    }

    private suspend fun refreshTokens(): Boolean {
        return try {
            Log.d("AuthRepo", "🔄 Refreshing tokens...")

            val refreshToken = sessionManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                Log.e("AuthRepo", "❌ No refresh token available")
                return false
            }

            supabase.auth.refreshSession(refreshToken)
            val session = supabase.auth.currentSessionOrNull()
                ?: return false

            if (session != null) {
                sessionManager.updateTokens(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken ?: ""
                )
                Log.d("AuthRepo", "✅ Tokens refreshed successfully")
                true
            } else {
                Log.e("AuthRepo", "❌ Refresh returned null session")
                false
            }

        } catch (e: Exception) {
            Log.e("AuthRepo", "❌ Token refresh failed", e)
            // Clear invalid session
            sessionManager.clearSession()
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
                avatarUrl = userProfile.avatar_url
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
            val userId = sessionManager.getCurrentUserId()?: ""

            // 1️. Remote logout (optional but correct)
            supabase.auth.signOut()
            val tripsBefore = tripDao.getTripIdsByUser(userId).size
            Log.d("Logout", "📊 Trips before: $tripsBefore")
            // 2.  Clear local DB data
            if (userId != null) {
                expenseSplitDao.deleteAllSplitsForUser(userId)
                expenseDao.deleteAllExpensesForUser(userId)
                tripMemberDao.deleteAllMembersForUser(userId)
                tripDao.deleteAllTripsForUser(userId)
                val tripsAfter = tripDao.getTripIdsByUser(userId).size
                Log.d("Logout", "📊 Trips after: $tripsAfter (should be 0)")
            }

            // 3.  Clear session LAST
            sessionManager.clearSession()

            Log.d("AuthRepo", "✅ Signed out & local data cleared")
            Unit.asSuccess()

        } catch (e: Exception) {
            Log.e("AuthRepo", " Sign out failed", e)
            e.asError()
        }
    }


    //Data Transfer Object for User (matches Supabase schema)
    @Serializable
    data class UserDto(
        val id: String,
        val email: String,
        val username: String,
        val full_name: String? = null,
        val avatar_url: String? = null,
        val created_at: String? = null,
        val updated_at: String? = null,
    )

    //Convert UserDto to Domain Model()
    fun UserDto.toDomainModel(): User{
        return User(
            id = id,
            email = email,
            userName = username,
            fullName = full_name,
            avatarUrl = avatar_url,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
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
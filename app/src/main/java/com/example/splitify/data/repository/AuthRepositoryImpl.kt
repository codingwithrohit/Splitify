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
import com.example.splitify.util.Result
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

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

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

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
            //1. Check if username is already taken
            val existingUser = supabase.from("users")
                .select {
                    filter { eq("username", userName) }
                }
                .decodeList<UserDto>()

            if (existingUser.isNotEmpty()) {
                return Exception("Username already taken").asError()
            }

            //2. sign up with supabase auth
            supabase.auth.signUpWith(Email){
                this.email = email
                this.password = password
            }
            //3. Get current session (contain tokens)
            val session = supabase.auth.currentSessionOrNull() ?:
            return Exception("Failed to get session").asError()

            val userId = session.user?.id
                ?: return Exception("Failed to get user id").asError()

            //4. Create user profile in database
            try {
                val userDto = UserDto(
                    id = userId,
                    email = email,
                    username = userName,
                    full_name = fullName
                )
                supabase.from("users").insert(userDto)
            } catch (e: Exception) {
                // Rollback: Delete auth user if profile creation fails
                try {
                    supabase.auth.admin.deleteUser(userId)
                } catch (_: Exception) { }
                throw e
            }

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

            UserDto(userId, email, userName, fullName, null, "", "").toDomainModel().asSuccess()
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

    override suspend fun checkUsernameAvailable(username: String): Result<Boolean> {
        return try {
            val users = supabase.from("users")
                .select {
                    filter { eq("username", username) }
                }
                .decodeList<UserDto>()

            (users.isEmpty()).asSuccess()
        } catch (e: Exception) {
            e.asError()
        }
    }

//    override suspend fun signInWithGoogle(idToken: String): Result<User> {
//        return try {
//            // 1. Sign in to Supabase using the Google ID Token
//            supabase.auth.signInWith(IDToken) {
//                this.idToken = idToken
//                provider = Google
//            }
//
//            val session = supabase.auth.currentSessionOrNull()
//                ?: return Exception("Failed to get session from Supabase").asError()
//
//            val userId = session.user?.id
//                ?: return Exception("Failed to get user ID").asError()
//
//            // 2. Sync profile: Check if user exists in your 'users' table
//            var userProfile = supabase.from("users")
//                .select()
//                .decodeList<UserDto>()
//                .firstOrNull { it.id == userId }
//
//            // 3. If new user (Social Login), create their profile automatically
//            if (userProfile == null) {
//                val email = session.user?.email ?: ""
//                // Generate a username from email if not provided by Google metadata
//                val baseUsername = email.split("@")[0]
//
//                userProfile = UserDto(
//                    id = userId,
//                    email = email,
//                    username = baseUsername,
//                    full_name = session.user?.userMetadata?.get("full_name")?.toString() ?: baseUsername
//                )
//                supabase.from("users").insert(userProfile)
//            }
//
//            // 4. Save to SessionManager (Consistent with your Email login)
//            sessionManager.saveSession(
//                accessToken = session.accessToken,
//                refreshToken = session.refreshToken ?: "",
//                userId = userId,
//                userEmail = userProfile.email,
//                userName = userProfile.username,
//                fullName = userProfile.full_name,
//                avatarUrl = userProfile.avatar_url,
//                expiresIn = session.expiresIn
//            )
//
//            userProfile.toDomainModel().asSuccess()
//
//        } catch (e: Exception) {
//            e.asError()
//        }
//    }


    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting Google Sign-In with ID Token")

                // 1. Sign in to Supabase using the Google ID Token
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    provider = Google
                }

                val session = supabase.auth.currentSessionOrNull()
                if (session == null) {
                    Log.e(TAG, "Failed to get session from Supabase")
                    return@withContext Exception("Failed to authenticate with Google").asError()
                }

                val userId = session.user?.id
                if (userId == null) {
                    Log.e(TAG, "Failed to get user ID from session")
                    return@withContext Exception("Failed to get user information").asError()
                }

                Log.d(TAG, "Google Sign-In successful, User ID: $userId")

                // 2. Fetch or create user profile in 'users' table
                var userProfile = try {
                    supabase.from("users")
                        .select()
                        .decodeList<UserDto>()
                        .firstOrNull { it.id == userId }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user profile: ${e.message}")
                    null
                }

                // 3. Create profile if new user
                if (userProfile == null) {
                    Log.d(TAG, "New user detected, creating profile")

                    val email = session.user?.email ?: ""
                    val fullName = session.user?.userMetadata?.get("full_name")?.toString()
                        ?: session.user?.userMetadata?.get("name")?.toString()
                        ?: email.split("@")[0]

                    // Generate unique username
                    val baseUsername = email.split("@")[0].lowercase().replace(Regex("[^a-z0-9]"), "")
                    val username = generateUniqueUsername(baseUsername)

                    val avatarUrl = session.user?.userMetadata?.get("avatar_url")?.toString()
                        ?: session.user?.userMetadata?.get("picture")?.toString()

                    userProfile = UserDto(
                        id = userId,
                        email = email,
                        username = username,
                        full_name = fullName,
                        avatar_url = avatarUrl
                    )

                    try {
                        supabase.from("users").insert(userProfile)
                        Log.d(TAG, "User profile created successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating user profile: ${e.message}")
                        return@withContext Exception("Failed to create user profile").asError()
                    }
                }

                // 4. Save session to SessionManager
                sessionManager.saveSession(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken ?: "",
                    userId = userId,
                    userEmail = userProfile.email,
                    userName = userProfile.username,
                    fullName = userProfile.full_name,
                    avatarUrl = userProfile.avatar_url,
                    expiresIn = session.expiresIn
                )

                Log.d(TAG, "Session saved successfully")

                // 5. Clear local database for fresh sync
                clearLocalDatabase()

                userProfile.toDomainModel().asSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
                e.asError()
            }
        }
    }

    /**
     * 🔥 NEW: Password Reset Implementation
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Sending password reset email to: $email")

                supabase.auth.resetPasswordForEmail(email)

                Log.d(TAG, "Password reset email sent successfully")
                Unit.asSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to send password reset email: ${e.message}", e)
                e.asError()
            }
        }
    }

    /**
     * 🔥 NEW: Update Password (after reset link clicked)
     */
    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Updating password")

                supabase.auth.modifyUser {
                    password = newPassword
                }

                Log.d(TAG, "Password updated successfully")
                Unit.asSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to update password: ${e.message}", e)
                e.asError()
            }
        }
    }

    /**
     * Helper: Generate unique username if collision occurs
     */
    private suspend fun generateUniqueUsername(baseUsername: String): String {
        var username = baseUsername
        var counter = 1

        while (isUsernameTaken(username)) {
            username = "${baseUsername}${counter}"
            counter++
        }

        return username
    }

    private suspend fun isUsernameTaken(username: String): Boolean {
        return try {
            val existing = supabase.from("users")
                .select()
                .decodeList<UserDto>()
                .any { it.username == username }
            existing
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun clearLocalDatabase() {
        try {
            expenseSplitDao.deleteAllSplits()
            settlementDao.deleteAllSettlements()
            expenseDao.deleteAllExpense()
            tripMemberDao.deleteAllMembers()
            tripDao.deleteAllTrips()
            Log.d(TAG, "Local database cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local database: ${e.message}")
        }
    }



}
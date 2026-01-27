package com.example.splitify.domain.repository

import com.example.splitify.domain.model.User
import kotlinx.coroutines.flow.Flow
import com.example.splitify.util.Result

interface AuthRepository {

    suspend fun initializeSession(): Boolean
    fun getCurrentUser(): Flow<User?>

    suspend fun isLoggedIn(): Boolean

    suspend fun signUp(
        email: String,
        password: String,
        userName: String,
        fullName: String,
    ): Result<User>

    suspend fun signIn(
        email: String,
        password: String
    ): Result<User>

    suspend fun getUserById(userId: String): Result<User?>

    fun searchUsersByUsername(query: String): Flow<Result<List<User>>>

    suspend fun signOut(): Result<Unit>

    suspend fun checkUsernameAvailable(username: String): Result<Boolean>

}
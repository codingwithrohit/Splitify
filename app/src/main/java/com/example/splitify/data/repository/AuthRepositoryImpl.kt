package com.example.splitify.data.repository

import android.annotation.SuppressLint
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
    private val supabase: SupabaseClient
): AuthRepository {

    override fun getCurrentUser(): Flow<User?> =
        flow {
            emit(
                runCatching {
                    val session = supabase.auth.currentSessionOrNull()
                        ?: return@runCatching null

                    val userId = session.user?.id ?: return@runCatching null

                    supabase.from("users")
                        .select()
                        .decodeList<UserDto>()
                        .firstOrNull { it.id == userId }
                        ?.toDomainModel()
                }.getOrNull()
            )
        }


    override suspend fun isLoggedIn(): Boolean {
        return try {
            supabase.auth.currentSessionOrNull() != null
        }
        catch (e: Exception){
            false
        }
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
            //2.  Get current user (session is created immediately if email confirmation is OFF)
            val userId = supabase.auth.currentUserOrNull()?.id ?:
            return Exception("Failed to get user id").asError()

            //3. Create user profile in database
            val userDto = UserDto(
                id = userId,
                email = email,
                username = userName,
                full_name = fullName
            )
            supabase.from("users").insert(userDto)
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
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return Exception("Failed to get user ID").asError()

            val userProfile = supabase.from("users")
                .select()
                .decodeList<UserDto>()
                .firstOrNull{it.id == userId}
                ?: return Exception("User profile not found").asError()
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
            Unit.asSuccess()
        }
        catch (e: Exception){
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

}
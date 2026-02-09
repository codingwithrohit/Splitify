package com.example.splitify.data.repository

import android.util.Log
import com.example.splitify.data.local.SessionManager
import com.example.splitify.data.remote.dto.UserDto
import com.example.splitify.data.remote.toDomainModel
import com.example.splitify.domain.model.User
import com.example.splitify.domain.repository.UserRepository
import com.example.splitify.util.Result
import com.example.splitify.util.asError
import com.example.splitify.util.asSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionManager: SessionManager
) : UserRepository {

    private val BUCKET_NAME = "avatars" // Your Supabase bucket name

    override suspend fun uploadProfilePicture(
        userId: String,
        imageFile: File
    ): Result<String> = withContext(Dispatchers.IO) {

        try {
            val bucket = supabase.storage.from(BUCKET_NAME)

            val path = "$userId/avatar.jpg"

            // No need to manually delete — upsert handles replacement
            bucket.upload(
                path = path,
                data = imageFile.readBytes(),
                upsert = true
            )

            val publicUrl = bucket.publicUrl(path)

            Log.d("UserRepo", "✅ Profile picture uploaded: $publicUrl")

            publicUrl.asSuccess()

        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Upload failed", e)
            e.asError()
        }
    }


//    override suspend fun uploadProfilePicture(userId: String, imageFile: File): Result<String> {
//        return try {
//            val bucket = supabase.storage.from(BUCKET_NAME)
//
//            // Delete old avatar if exists
//            try {
//                bucket.delete("$userId/avatar.jpg")
//            } catch (e: Exception) {
//                // Ignore if file doesn't exist
//            }
//
//            // Upload new avatar
//            val path = "$userId/avatar.jpg"
//            bucket.upload(
//                path = path,
//                data = imageFile.readBytes(),
//                upsert = true
//            )
//
//            // Get public URL
//            val publicUrl = bucket.publicUrl(path)
//
//            Log.d("UserRepo", "✅ Profile picture uploaded: $publicUrl")
//
//            publicUrl.asSuccess()
//        } catch (e: Exception) {
//            Log.e("UserRepo", "❌ Upload failed", e)
//            e.asError()
//        }
//    }

    override suspend fun getProfilePictureUrl(userId: String): Result<String?> {
        return try {
            val bucket = supabase.storage.from(BUCKET_NAME)
            val path = "$userId/avatar.jpg"

            // Check if file exists
            val files = bucket.list(userId)
            val avatarExists = files.any { it.name == "avatar.jpg" }

            if (avatarExists) {
                val publicUrl = bucket.publicUrl(path)
                publicUrl.asSuccess()
            } else {
                null.asSuccess()
            }
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Get avatar URL failed", e)
            e.asError()
        }
    }

    override suspend fun updateUserProfile(userId: String, avatarUrl: String): Result<User> {
        return try {
            // Update in Supabase database
            supabase.from("users")
                .update(mapOf("avatar_url" to avatarUrl)) {
                    filter {
                        eq("id", userId)
                    }
                }

            // Update SessionManager
            sessionManager.updateAvatarUrl(avatarUrl)

            // Get updated user profile
            val userProfile = supabase.from("users")
                .select()
                .decodeList<UserDto>()
                .firstOrNull { it.id == userId }
                ?: return Exception("User not found").asError()

            Log.d("UserRepo", "✅ Profile updated with avatar URL")

            userProfile.toDomainModel().asSuccess()
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Update profile failed", e)
            e.asError()
        }
    }

    override suspend fun deleteProfilePicture(userId: String): Result<Unit> {
        return try {
            val bucket = supabase.storage.from(BUCKET_NAME)

            // Delete from storage
            bucket.delete("$userId/avatar.jpg")

            // Update database to remove avatar_url
            supabase.from("users")
                .update(mapOf("avatar_url" to null)) {
                    filter {
                        eq("id", userId)
                    }
                }

            // Update SessionManager
            sessionManager.updateAvatarUrl(null)

            Log.d("UserRepo", "✅ Profile picture deleted")

            Unit.asSuccess()
        } catch (e: Exception) {
            Log.e("UserRepo", "❌ Delete failed", e)
            e.asError()
        }
    }
}
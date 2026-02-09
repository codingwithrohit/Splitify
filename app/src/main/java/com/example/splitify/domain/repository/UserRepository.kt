package com.example.splitify.domain.repository

import com.example.splitify.domain.model.User
import java.io.File
import com.example.splitify.util.Result

interface UserRepository {

    suspend fun uploadProfilePicture(userId: String, imageFile: File): Result<String>

    suspend fun getProfilePictureUrl(userId: String): Result<String?>

    suspend fun updateUserProfile(userId: String, avatarUrl: String): Result<User>

    suspend fun deleteProfilePicture(userId: String): Result<Unit>
}
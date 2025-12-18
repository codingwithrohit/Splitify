package com.example.splitify.domain.model

data class TripMember(
    var id: String,
    val tripId: String,
    val userId: String?,
    val displayName: String,
    val role: MemberRole,
    val joinedAt: Long,
    val avatarUrl: String? = null
){
    val isGuest: Boolean get() = userId == null
    val isAdmin: Boolean get() = role == MemberRole.ADMIN
}


enum class MemberRole{
    ADMIN, MEMBER
}
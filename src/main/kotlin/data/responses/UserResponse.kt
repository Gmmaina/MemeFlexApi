package com.example.data.responses

import com.example.data.models.User
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val profileImage: String?,
    val totalLikes: Int,
    val createdAt: Long
)

// Extension functions
fun User.toResponse() = UserResponse(
    id = stringId,
    username = username,
    email = email,
    profileImage = profileImage,
    totalLikes = totalLikes,
    createdAt = createdAt
)
package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String, // Can be username or email
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)


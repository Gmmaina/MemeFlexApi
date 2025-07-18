package com.example.data.responses

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

@Serializable
data class SuccessResponse(
    val success: Boolean,
    val message: String
)


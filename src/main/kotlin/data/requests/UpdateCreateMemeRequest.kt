package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMemeRequest(
    val caption: String? = null,
    val tags: List<String> = emptyList()
)

@Serializable
data class CreateMemeRequest(
    val imageUrl: String,
    val caption: String? = null,
    val tags: List<String> = emptyList()
)


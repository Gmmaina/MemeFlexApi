package com.example.data.responses

import com.example.data.models.Meme
import kotlinx.serialization.Serializable

@Serializable
data class MemeResponse(
    val id: String,
    val userId: String,
    val username: String,
    val imageUrl: String,
    val caption: String?,
    val tags: List<String>,
    val likesCount: Int,
    val isLiked: Boolean = false, // For current user
    val downloadCount: Int,
    val createdAt: Long
)

@Serializable
data class FeedResponse(
    val memes: List<MemeResponse>,
    val hasMore: Boolean,
    val nextCursor: String?
)

fun Meme.toResponse(currentUserId: String? = null) = MemeResponse(
    id = stringId,
    userId = userId,
    username = username,
    imageUrl = imageUrl,
    caption = caption,
    tags = tags,
    likesCount = likes.size,
    isLiked = currentUserId?.let { likes.contains(it) } ?: false,
    downloadCount = downloadCount,
    createdAt = createdAt
)
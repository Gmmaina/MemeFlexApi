package com.example.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class Meme(
    @BsonId val id: ObjectId? = null,
    val userId: String,
    val username: String, // Denormalized for faster queries
    val imageUrl: String,
    val caption: String? = null,
    val tags: List<String> = emptyList(),
    val likes: List<String> = emptyList(), // List of user IDs who liked
    val downloadCount: Int = 0,
    val createdAt: Long = Instant.now().epochSecond,
    val isActive: Boolean = true
) {
    // Helper to get string ID
    val stringId: String get() = id?.toHexString() ?: ""
}
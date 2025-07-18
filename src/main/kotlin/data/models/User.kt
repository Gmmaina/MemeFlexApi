package com.example.data.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class User(
    @BsonId val id: ObjectId? = null,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = Instant.now().epochSecond,
    val profileImage: String? = null,
    val totalLikes: Int = 0,
    val isActive: Boolean = true
) {
    // Helper to get string ID
    val stringId: String get() = id?.toHexString() ?: ""
}
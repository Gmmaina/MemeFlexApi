package com.example.repositories

import com.example.data.models.Meme
import com.example.database.DatabaseFactory
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.bson.types.ObjectId


class MemeRepository {
    private val collection: MongoCollection<Meme> = DatabaseFactory.database.getCollection("memes")

    suspend fun createMeme(meme: Meme): Meme {
        try {
            collection.insertOne(meme)
            println("✅ Meme created successfully: ${meme.stringId}")
        } catch (e: Exception) {
            println("❌ Error creating meme: ${e.message}")
            e.printStackTrace()
        }
        return meme
    }

    suspend fun getMemeById(id: String): Meme? {
        return try {
            val objectId = ObjectId(id)
            collection.find(
                Filters.and(
                    Filters.eq("_id", objectId),
                    Filters.eq("isActive", true)
                )
            ).firstOrNull()
        } catch (e: Exception) {
            println("❌ Error getting meme by ID: ${e.message}")
            null
        }
    }

    suspend fun getMemesByUserId(userId: String, limit: Int, skip: Int): List<Meme> {
        return try {
            collection.find(
                Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("isActive", true)
                )
            )
                .sort(Sorts.descending("createdAt"))
                .limit(limit)
                .skip(skip)
                .toList()
        } catch (e: Exception) {
            println("❌ Error getting memes by user ID: ${e.message}")
            emptyList()
        }
    }

    suspend fun getFeed(limit: Int, skip: Int): List<Meme> {
        return try {
            collection.find(Filters.eq("isActive", true))
                .sort(Sorts.descending("createdAt"))
                .limit(limit)
                .skip(skip)
                .toList()
        } catch (e: Exception) {
            println("❌ Error getting feed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getFeedByPopularity(limit: Int, skip: Int): List<Meme> {
        return try {
            collection.find(Filters.eq("isActive", true))
                .sort(Sorts.descending("createdAt"))
                .limit(limit)
                .skip(skip)
                .toList()
        } catch (e: Exception) {
            println("❌ Error getting popular feed: ${e.message}")
            emptyList()
        }
    }

    suspend fun searchMemes(query: String, limit: Int, skip: Int): List<Meme> {
        return try {
            collection.find(
                Filters.and(
                    Filters.text(query),
                    Filters.eq("isActive", true)
                )
            )
                .sort(Sorts.descending("createdAt"))
                .limit(limit)
                .skip(skip)
                .toList()
        } catch (e: Exception) {
            println("❌ Error searching memes: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMemesByTags(tags: List<String>, limit: Int, skip: Int): List<Meme> {
        return try {
            collection.find(
                Filters.and(
                    Filters.`in`("tags", tags),
                    Filters.eq("isActive", true)
                )
            )
                .sort(Sorts.descending("createdAt"))
                .limit(limit)
                .skip(skip)
                .toList()
        } catch (e: Exception) {
            println("❌ Error getting memes by tags: ${e.message}")
            emptyList()
        }
    }

    suspend fun likeMeme(memeId: String, userId: String): Boolean {
        return try {
            val objectId = ObjectId(memeId)
            val result = collection.updateOne(
                Filters.and(
                    Filters.eq("_id", objectId),
                    Filters.not(Filters.eq("likes", userId))
                ),
                Updates.addToSet("likes", userId)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("❌ Error liking meme: ${e.message}")
            false
        }
    }

    suspend fun unlikeMeme(memeId: String, userId: String): Boolean {
        return try {
            val objectId = ObjectId(memeId)
            val result = collection.updateOne(
                Filters.eq("_id", objectId),
                Updates.pull("likes", userId)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("❌ Error unliking meme: ${e.message}")
            false
        }
    }

    suspend fun incrementDownloadCount(memeId: String): Boolean {
        return try {
            val objectId = ObjectId(memeId)
            val result = collection.updateOne(
                Filters.eq("_id", objectId),
                Updates.inc("downloadCount", 1)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("❌ Error incrementing download count: ${e.message}")
            false
        }
    }

    suspend fun updateMeme(memeId: String, userId: String, caption: String?, tags: List<String>): Boolean {
        return try {
            val objectId = ObjectId(memeId)
            val updates = mutableListOf<Bson>()
            caption?.let { updates.add(Updates.set("caption", it)) }
            updates.add(Updates.set("tags", tags))

            val result = collection.updateOne(
                Filters.and(
                    Filters.eq("_id", objectId),
                    Filters.eq("userId", userId)
                ),
                Updates.combine(updates)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("❌ Error updating meme: ${e.message}")
            false
        }
    }

    suspend fun deleteMeme(memeId: String, userId: String): Boolean {
        return try {
            val objectId = ObjectId(memeId)
            val result = collection.updateOne(
                Filters.and(
                    Filters.eq("_id", objectId),
                    Filters.eq("userId", userId)
                ),
                Updates.set("isActive", false)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("❌ Error deleting meme: ${e.message}")
            false
        }
    }

    suspend fun getUserMemeCount(userId: String): Long {
        return try {
            collection.countDocuments(
                Filters.and(
                    Filters.eq("userId", userId),
                    Filters.eq("isActive", true)
                )
            )
        } catch (e: Exception) {
            println("❌ Error getting user meme count: ${e.message}")
            0
        }
    }
}
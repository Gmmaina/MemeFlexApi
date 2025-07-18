package com.example.repositories

import com.example.data.models.User
import com.example.database.DatabaseFactory
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId


class UserRepository {
    private val collection: MongoCollection<User> = DatabaseFactory.database.getCollection("users")

    suspend fun createUser(user: User): User? {
        return try {
            println("🔄 Attempting to create user: ${user.username}")
            collection.insertOne(user)
            println("✅ User created successfully: ${user.stringId}")
            user
        } catch (e: Exception) {
            println("❌ Error creating user: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun getUserById(id: String): User? {
        return try {
            val objectId = ObjectId(id)
            collection.find(
                Filters.and(
                    Filters.eq("_id", objectId),
                    Filters.eq("isActive", true)
                )
            ).firstOrNull()
        } catch (e: Exception) {
            println("❌ Error getting user by ID: ${e.message}")
            null
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        return try {
            collection.find(
                Filters.and(
                    Filters.eq("username", username),
                    Filters.eq("isActive", true)
                )
            ).firstOrNull()
        } catch (e: Exception) {
            println("❌ Error getting user by username: ${e.message}")
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            collection.find(
                Filters.and(
                    Filters.eq("email", email),
                    Filters.eq("isActive", true)
                )
            ).firstOrNull()
        } catch (e: Exception) {
            println("❌ Error getting user by email: ${e.message}")
            null
        }
    }

    suspend fun getUserByUsernameOrEmail(usernameOrEmail: String): User? {
        return try {
            collection.find(
                Filters.and(
                    Filters.or(
                        Filters.eq("username", usernameOrEmail),
                        Filters.eq("email", usernameOrEmail)
                    ),
                    Filters.eq("isActive", true)
                )
            ).firstOrNull()
        } catch (e: Exception) {
            println("❌ Error getting user by username or email: ${e.message}")
            null
        }
    }

    suspend fun updateUserLikes(userId: String, increment: Int): Boolean {
        return try {
            val objectId = ObjectId(userId)
            val result = collection.updateOne(
                Filters.eq("_id", objectId),
                Updates.inc("totalLikes", increment)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("❌ Error updating user likes: ${e.message}")
            false
        }
    }

    suspend fun updateUserProfile(userId: String, profileImage: String?): Boolean {
        return try {
            val objectId = ObjectId(userId)
            val result = collection.updateOne(
                Filters.eq("_id", objectId),
                Updates.set("profileImage", profileImage)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            println("❌ Error updating user profile: ${e.message}")
            false
        }
    }
}

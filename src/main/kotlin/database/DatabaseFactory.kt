package com.example.database

import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider


object DatabaseFactory {
    private lateinit var client: MongoClient
    lateinit var database: MongoDatabase
    private var indexesCreated = false

    fun init(connectionString: String, databaseName: String) {
        println("🔄 Connecting to MongoDB: $connectionString")
        println("🔄 Database: $databaseName")

        // Configure codecs for POJO mapping
        val pojoCodecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
        )

        client = MongoClient.create(connectionString)
        database = client.getDatabase(databaseName)
            .withCodecRegistry(pojoCodecRegistry)

        println("✅ MongoDB connection established")
    }

    suspend fun ensureIndexes() {
        if (!indexesCreated) {
            createIndexes()
            indexesCreated = true
        }
    }

    private suspend fun createIndexes() {
        try {
            val usersCollection = database.getCollection<org.bson.Document>("users")
            val memesCollection = database.getCollection<org.bson.Document>("memes")

            // User indexes
            usersCollection.createIndex(org.bson.Document("username", 1))
            usersCollection.createIndex(org.bson.Document("email", 1))

            // Meme indexes
            memesCollection.createIndex(org.bson.Document("createdAt", -1))
            memesCollection.createIndex(org.bson.Document("userId", 1))
            memesCollection.createIndex(org.bson.Document("tags", 1))
            memesCollection.createIndex(org.bson.Document("caption", "text"))
            memesCollection.createIndex(
                org.bson.Document("isActive", 1).append("createdAt", -1)
            )

            println("✅ Database indexes created successfully")
        } catch (e: Exception) {
            println("❌ Warning: Could not create indexes - ${e.message}")
        }
    }

    fun close() {
        client.close()
    }
}
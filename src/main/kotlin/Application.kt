package com.example

import com.example.auth.JwtConfig
import com.example.data.responses.ErrorResponse
import com.example.database.DatabaseFactory
import com.example.routes.configureRoutes
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.event.Level


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Get configuration from environment variables (for production)
    val connectionString = environment.config.propertyOrNull("database.connectionString")?.getString()
        ?: System.getenv("MONGODB_URI")
        ?: "mongodb://localhost:27017"

    val databaseName = environment.config.propertyOrNull("database.name")?.getString()
        ?: System.getenv("DATABASE_NAME")
        ?: "memeapp"

    // Initialize database
    DatabaseFactory.init(connectionString, databaseName)

    // Configure plugins
    configureContentNegotiation()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureLogging()

    // Configure routes
    configureRoutes()

    // Create indexes when the application starts (in a coroutine)
    launch {
        try {
            DatabaseFactory.ensureIndexes()
            println("✅ Database indexes created successfully")
        } catch (e: Exception) {
            println("❌ Error creating indexes: ${e.message}")
            e.printStackTrace()
        }
    }
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        // For development - in production, specify your Android app's domain
        anyHost()

        // For production, use specific origins:
        // allowHost("your-android-app-domain.com", schemes = listOf("https"))
    }
}

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId")?.asString()
                val username = credential.payload.getClaim("username")?.asString()

                if (userId != null && username != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("unauthorized", "Token is not valid or has expired")
                )
            }
        }
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // Log the actual error for debugging
            call.application.log.error("Unhandled exception: ${cause.message}", cause)
            cause.printStackTrace() // This will show the full error in console

            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("internal_error", "An unexpected error occurred: ${cause.message}")
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("not_found", "The requested resource was not found")
            )
        }

        status(HttpStatusCode.MethodNotAllowed) { call, _ ->
            call.respond(
                HttpStatusCode.MethodNotAllowed,
                ErrorResponse("method_not_allowed", "HTTP method not allowed for this endpoint")
            )
        }
    }
}

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}
package com.example.routes


import com.example.auth.JwtConfig
import com.example.auth.PasswordUtils
import com.example.auth.ValidationUtils
import com.example.data.requests.*
import com.example.data.responses.*
import com.example.data.models.*
import com.example.data.requests.LoginRequest
import com.example.data.requests.RegisterRequest
import com.example.repositories.MemeRepository
import com.example.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRoutes() {
    val userRepository = UserRepository()
    val memeRepository = MemeRepository()

    routing {
        // Auth routes
        route("/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()

                // Validation
                if (!ValidationUtils.isValidUsername(request.username)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("validation_error", "Invalid username format")
                    )
                    return@post
                }

                if (!ValidationUtils.isValidEmail(request.email)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("validation_error", "Invalid email format")
                    )
                    return@post
                }

                if (!ValidationUtils.isValidPassword(request.password)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("validation_error", "Password must be at least 6 characters")
                    )
                    return@post
                }

                // Check if user already exists
                if (userRepository.getUserByUsername(request.username) != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("user_exists", "Username already taken")
                    )
                    return@post
                }

                if (userRepository.getUserByEmail(request.email) != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("user_exists", "Email already registered")
                    )
                    return@post
                }

                // Create user
                val user = User(
                    username = request.username.trim(),
                    email = request.email.trim().lowercase(),
                    passwordHash = PasswordUtils.hashPassword(request.password)
                )

                val createdUser = userRepository.createUser(user)
                if (createdUser != null) {
                    val token = JwtConfig.generateToken(createdUser.stringId, createdUser.username)
                    call.respond(
                        HttpStatusCode.Created,
                        AuthResponse(token, createdUser.toResponse())
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("registration_failed", "Failed to create user")
                    )
                }
            }

            post("/login") {
                val request = call.receive<LoginRequest>()

                val user = userRepository.getUserByUsernameOrEmail(request.username)
                if (user == null || !PasswordUtils.verifyPassword(request.password, user.passwordHash)) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("invalid_credentials", "Invalid username/email or password")
                    )
                    return@post
                }

                val token = JwtConfig.generateToken(user.stringId, user.username)
                call.respond(AuthResponse(token, user.toResponse()))
            }
        }

        // Protected routes
        authenticate("auth-jwt") {
            // User routes
            route("/users") {
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = JwtConfig.extractUserId(principal!!)!!

                    val user = userRepository.getUserById(userId)
                    if (user != null) {
                        call.respond(user.toResponse())
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("user_not_found", "User not found")
                        )
                    }
                }

                get("/{id}") {
                    val userId = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "User ID is required")
                    )

                    val user = userRepository.getUserById(userId)
                    if (user != null) {
                        call.respond(user.toResponse())
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("user_not_found", "User not found")
                        )
                    }
                }

                get("/{id}/memes") {
                    val userId = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "User ID is required")
                    )

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
                    val skip = (page - 1) * limit

                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = JwtConfig.extractUserId(principal!!)!!

                    val memes = memeRepository.getMemesByUserId(userId, limit + 1, skip)
                    val hasMore = memes.size > limit
                    val memesToReturn = if (hasMore) memes.dropLast(1) else memes

                    call.respond(
                        FeedResponse(
                            memes = memesToReturn.map { it.toResponse(currentUserId) },
                            hasMore = hasMore,
                            nextCursor = if (hasMore) (page + 1).toString() else null
                        )
                    )
                }
            }

            // Meme routes
            route("/memes") {
                post {
                    val request = call.receive<CreateMemeRequest>()
                    val principal = call.principal<JWTPrincipal>()
                    val userId = JwtConfig.extractUserId(principal!!)!!
                    val username = JwtConfig.extractUsername(principal)!!

                    // Validate image URL
                    if (!ValidationUtils.isValidImageUrl(request.imageUrl)) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("invalid_image", "Invalid image URL")
                        )
                        return@post
                    }

                    val meme = Meme(
                        userId = userId,
                        username = username,
                        imageUrl = request.imageUrl,
                        caption = request.caption?.trim(),
                        tags = request.tags.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                    )

                    val createdMeme = memeRepository.createMeme(meme)
                    call.respond(HttpStatusCode.Created, createdMeme.toResponse(userId))
                }

                get("/{id}") {
                    val memeId = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "Meme ID is required")
                    )

                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = JwtConfig.extractUserId(principal!!)!!

                    val meme = memeRepository.getMemeById(memeId)
                    if (meme != null) {
                        call.respond(meme.toResponse(currentUserId))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("meme_not_found", "Meme not found")
                        )
                    }
                }

                put("/{id}") {
                    val memeId = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "Meme ID is required")
                    )

                    val request = call.receive<UpdateMemeRequest>()
                    val principal = call.principal<JWTPrincipal>()
                    val userId = JwtConfig.extractUserId(principal!!)!!

                    val updated = memeRepository.updateMeme(
                        memeId,
                        userId,
                        request.caption?.trim(),
                        request.tags.map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                    )

                    if (updated) {
                        call.respond(SuccessResponse(true, "Meme updated successfully"))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("meme_not_found", "Meme not found or you don't have permission")
                        )
                    }
                }

                delete("/{id}") {
                    val memeId = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "Meme ID is required")
                    )

                    val principal = call.principal<JWTPrincipal>()
                    val userId = JwtConfig.extractUserId(principal!!)!!

                    val deleted = memeRepository.deleteMeme(memeId, userId)
                    if (deleted) {
                        call.respond(SuccessResponse(true, "Meme deleted successfully"))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("meme_not_found", "Meme not found or you don't have permission")
                        )
                    }
                }

                post("/{id}/like") {
                    val memeId = call.parameters["id"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "Meme ID is required")
                    )

                    val principal = call.principal<JWTPrincipal>()
                    val userId = JwtConfig.extractUserId(principal!!)!!

                    val liked = memeRepository.likeMeme(memeId, userId)
                    if (liked) {
                        // Update user's total likes
                        val meme = memeRepository.getMemeById(memeId)
                        meme?.let { userRepository.updateUserLikes(it.userId, 1) }

                        call.respond(SuccessResponse(true, "Meme liked"))
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("already_liked", "Meme already liked or not found")
                        )
                    }
                }

                delete("/{id}/like") {
                    val memeId = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "Meme ID is required")
                    )

                    val principal = call.principal<JWTPrincipal>()
                    val userId = JwtConfig.extractUserId(principal!!)!!

                    val unliked = memeRepository.unlikeMeme(memeId, userId)
                    if (unliked) {
                        // Update user's total likes
                        val meme = memeRepository.getMemeById(memeId)
                        meme?.let { userRepository.updateUserLikes(it.userId, -1) }

                        call.respond(SuccessResponse(true, "Meme unliked"))
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("not_liked", "Meme not liked or not found")
                        )
                    }
                }

                post("/{id}/download") {
                    val memeId = call.parameters["id"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("missing_parameter", "Meme ID is required")
                    )

                    val updated = memeRepository.incrementDownloadCount(memeId)
                    if (updated) {
                        call.respond(SuccessResponse(true, "Download counted"))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("meme_not_found", "Meme not found")
                        )
                    }
                }
            }
        }

        // Public routes (no authentication required)
        route("/feed") {
            get {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
                val sortBy = call.request.queryParameters["sort"] ?: "recent" // recent, popular
                val skip = (page - 1) * limit

                val memes = when (sortBy) {
                    "popular" -> memeRepository.getFeedByPopularity(limit + 1, skip)
                    else -> memeRepository.getFeed(limit + 1, skip)
                }

                val hasMore = memes.size > limit
                val memesToReturn = if (hasMore) memes.dropLast(1) else memes

                call.respond(
                    FeedResponse(
                        memes = memesToReturn.map { it.toResponse() },
                        hasMore = hasMore,
                        nextCursor = if (hasMore) (page + 1).toString() else null
                    )
                )
            }

            get("/search") {
                val query = call.request.queryParameters["q"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("missing_parameter", "Search query is required")
                )

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
                val skip = (page - 1) * limit

                val memes = memeRepository.searchMemes(query, limit + 1, skip)
                val hasMore = memes.size > limit
                val memesToReturn = if (hasMore) memes.dropLast(1) else memes

                call.respond(
                    FeedResponse(
                        memes = memesToReturn.map { it.toResponse() },
                        hasMore = hasMore,
                        nextCursor = if (hasMore) (page + 1).toString() else null
                    )
                )
            }

            get("/tags/{tag}") {
                val tag = call.parameters["tag"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("missing_parameter", "Tag is required")
                )

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20
                val skip = (page - 1) * limit

                val memes = memeRepository.getMemesByTags(listOf(tag.lowercase()), limit + 1, skip)
                val hasMore = memes.size > limit
                val memesToReturn = if (hasMore) memes.dropLast(1) else memes

                call.respond(
                    FeedResponse(
                        memes = memesToReturn.map { it.toResponse() },
                        hasMore = hasMore,
                        nextCursor = if (hasMore) (page + 1).toString() else null
                    )
                )
            }
        }
    }
}
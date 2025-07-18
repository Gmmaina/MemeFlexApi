package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.auth.jwt.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*


object JwtConfig {
    private val SECRET = System.getenv("JWT_SECRET") ?: "your-secret-key-change-in-production"
    private const val ISSUER = "meme-app"
    private const val AUDIENCE = "meme-app-users"
    private const val VALIDITY_MS = 1000L * 60 * 60 * 24 * 7 // 7 days

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(userId: String, username: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(algorithm)
    }

    fun extractUserId(principal: JWTPrincipal): String? {
        return principal.payload.getClaim("userId")?.asString()
    }

    fun extractUsername(principal: JWTPrincipal): String? {
        return principal.payload.getClaim("username")?.asString()
    }
}

object PasswordUtils {
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
}

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 &&
                username.length <= 20 &&
                username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidImageUrl(url: String): Boolean {
        return url.startsWith("https://res.cloudinary.com/") &&
                (url.contains(".jpg") || url.contains(".png") || url.contains(".gif") || url.contains(".jpeg"))
    }
}
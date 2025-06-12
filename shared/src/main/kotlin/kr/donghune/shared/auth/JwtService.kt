package kr.donghune.shared.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class JwtService {
    private val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-key-change-in-production"
    private val jwtIssuer = "mogako-auth"
    private val algorithm = Algorithm.HMAC256(jwtSecret)
    
    private val accessTokenExpiryMinutes = 15L
    private val refreshTokenExpiryDays = 30L
    
    fun generateAccessToken(userId: Int): String {
        val expiresAt = LocalDateTime.now().plusMinutes(accessTokenExpiryMinutes)
        
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(userId.toString())
            .withClaim("type", "access")
            .withExpiresAt(Date.from(expiresAt.toInstant(ZoneOffset.UTC)))
            .withIssuedAt(Date())
            .sign(algorithm)
    }
    
    fun generateRefreshToken(userId: Int): String {
        val expiresAt = LocalDateTime.now().plusDays(refreshTokenExpiryDays)
        
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(userId.toString())
            .withClaim("type", "refresh")
            .withExpiresAt(Date.from(expiresAt.toInstant(ZoneOffset.UTC)))
            .withIssuedAt(Date())
            .sign(algorithm)
    }
    
    fun verifyToken(token: String): JwtPayload? {
        return try {
            val decodedJWT = JWT.require(algorithm)
                .withIssuer(jwtIssuer)
                .build()
                .verify(token)
            
            JwtPayload(
                userId = decodedJWT.subject.toInt(),
                type = decodedJWT.getClaim("type").asString(),
                expiresAt = decodedJWT.expiresAt.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime()
            )
        } catch (e: JWTVerificationException) {
            null
        }
    }
    
    fun isTokenExpired(payload: JwtPayload): Boolean {
        return LocalDateTime.now().isAfter(payload.expiresAt)
    }
    
    data class JwtPayload(
        val userId: Int,
        val type: String,
        val expiresAt: LocalDateTime
    )
}
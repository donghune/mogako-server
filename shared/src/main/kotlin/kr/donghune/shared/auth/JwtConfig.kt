package kr.donghune.shared.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureJwtAuthentication() {
    authentication {
        jwt("auth-jwt") {
            val jwtSecret = System.getenv("JWT_SECRET") ?: "default-secret-key-change-in-production"
            val jwtIssuer = "mogako-auth"
            
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            
            validate { credential ->
                if (credential.payload.issuer.contains(jwtIssuer)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
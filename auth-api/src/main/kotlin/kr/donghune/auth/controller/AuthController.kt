package kr.donghune.auth.controller

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kr.donghune.auth.dto.EmailLoginRequest
import kr.donghune.auth.dto.GoogleLoginRequest
import kr.donghune.auth.dto.RefreshTokenRequest
import kr.donghune.auth.dto.RegisterRequest
import kr.donghune.auth.service.AuthService

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            try {
                val registerRequest = call.receive<RegisterRequest>()
                val response = authService.registerWithEmail(registerRequest)

                if (response != null) {
                    call.respond(HttpStatusCode.Created, response)
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Email already exists or invalid password")
                    )
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
            }
        }

        post("/login/google") {
            try {
                val loginRequest = call.receive<GoogleLoginRequest>()
                val response = authService.authenticateWithGoogle(loginRequest.googleIdToken)

                if (response != null) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid Google ID token"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
            }
        }

        post("/login/email") {
            try {
                val loginRequest = call.receive<EmailLoginRequest>()
                val response = authService.authenticateWithEmail(loginRequest)

                if (response != null) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid email or password"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
            }
        }

        post("/refresh") {
            try {
                val refreshRequest = call.receive<RefreshTokenRequest>()
                val response = authService.refreshAccessToken(refreshRequest.refreshToken)

                if (response != null) {
                    call.respond(HttpStatusCode.OK, response)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid refresh token"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
            }
        }

        authenticate("auth-jwt") {
            post("/logout") {
                try {
                    val refreshRequest = call.receive<RefreshTokenRequest>()
                    val success = authService.logout(refreshRequest.refreshToken)

                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Successfully logged out"))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid refresh token"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                }
            }

            get("/me") {
                val accessToken = call.request.headers["Authorization"]?.removePrefix("Bearer ")

                if (accessToken != null) {
                    val user = authService.validateAccessToken(accessToken)
                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user.toResponse())
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid access token"))
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing access token"))
                }
            }
        }
    }
}
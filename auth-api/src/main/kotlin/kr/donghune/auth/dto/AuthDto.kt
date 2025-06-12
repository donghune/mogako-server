package kr.donghune.auth.dto

import kr.donghune.auth.domain.AuthProvider
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?,
    val authProvider: AuthProvider,
    val emailVerified: Boolean
)

@Serializable
data class GoogleLoginRequest(
    val googleIdToken: String
)

@Serializable
data class EmailLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)
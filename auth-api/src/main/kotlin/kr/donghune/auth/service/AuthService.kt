package kr.donghune.auth.service

import kr.donghune.auth.domain.AuthProvider
import kr.donghune.auth.domain.User
import kr.donghune.auth.domain.Users
import kr.donghune.auth.dto.EmailLoginRequest
import kr.donghune.auth.dto.LoginResponse
import kr.donghune.auth.dto.RefreshTokenResponse
import kr.donghune.auth.dto.RegisterRequest
import kr.donghune.auth.infrastructure.GoogleOAuthService
import kr.donghune.shared.auth.JwtService
import kr.donghune.shared.auth.PasswordService
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime

class AuthService(
    private val jwtService: JwtService,
    private val googleOAuthService: GoogleOAuthService,
    private val passwordService: PasswordService
) {
    
    suspend fun authenticateWithGoogle(googleIdToken: String): LoginResponse? {
        val googleUserInfo = googleOAuthService.verifyGoogleIdToken(googleIdToken) ?: return null
        
        val user = newSuspendedTransaction {
            val existingUser = User.find { Users.googleId eq googleUserInfo.id }.firstOrNull()
            
            if (existingUser != null) {
                existingUser.updatedAt = LocalDateTime.now()
                existingUser
            } else {
                User.new {
                    googleId = googleUserInfo.id
                    email = googleUserInfo.email
                    authProvider = AuthProvider.GOOGLE
                    nickname = googleUserInfo.name
                    profileImageUrl = googleUserInfo.picture
                    emailVerified = true
                }
            }
        }
        
        val accessToken = jwtService.generateAccessToken(user.id.value)
        val refreshToken = jwtService.generateRefreshToken(user.id.value)
        
        newSuspendedTransaction {
            user.refreshToken = refreshToken
        }
        
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = user.toResponse()
        )
    }
    
    suspend fun refreshAccessToken(refreshTokenString: String): RefreshTokenResponse? {
        val payload = jwtService.verifyToken(refreshTokenString) ?: return null
        
        if (payload.type != "refresh" || jwtService.isTokenExpired(payload)) {
            return null
        }
        
        val user = newSuspendedTransaction {
            User.findById(payload.userId)
        } ?: return null
        
        val isValidRefreshToken = newSuspendedTransaction {
            user.refreshToken == refreshTokenString
        }
        
        if (!isValidRefreshToken) {
            return null
        }
        
        val newAccessToken = jwtService.generateAccessToken(user.id.value)
        val newRefreshToken = jwtService.generateRefreshToken(user.id.value)
        
        newSuspendedTransaction {
            user.refreshToken = newRefreshToken
            user.updatedAt = LocalDateTime.now()
        }
        
        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }
    
    suspend fun logout(refreshTokenString: String): Boolean {
        val payload = jwtService.verifyToken(refreshTokenString) ?: return false
        
        if (payload.type != "refresh") {
            return false
        }
        
        return newSuspendedTransaction {
            val user = User.findById(payload.userId)
            if (user != null && user.refreshToken == refreshTokenString) {
                user.refreshToken = null
                user.updatedAt = LocalDateTime.now()
                true
            } else {
                false
            }
        }
    }
    
    suspend fun validateAccessToken(accessTokenString: String): User? {
        val payload = jwtService.verifyToken(accessTokenString) ?: return null
        
        if (payload.type != "access" || jwtService.isTokenExpired(payload)) {
            return null
        }
        
        return newSuspendedTransaction {
            User.findById(payload.userId)
        }
    }
    
    suspend fun registerWithEmail(request: RegisterRequest): LoginResponse? {
        if (!passwordService.isValidPassword(request.password)) {
            return null
        }
        
        val existingUser = newSuspendedTransaction {
            User.find { Users.email eq request.email }.firstOrNull()
        }
        
        if (existingUser != null) {
            return null
        }
        
        val hashedPassword = passwordService.hashPassword(request.password)
        
        val user = newSuspendedTransaction {
            User.new {
                email = request.email
                passwordHash = hashedPassword
                authProvider = AuthProvider.EMAIL
                nickname = request.nickname
                emailVerified = false
            }
        }
        
        val accessToken = jwtService.generateAccessToken(user.id.value)
        val refreshToken = jwtService.generateRefreshToken(user.id.value)
        
        newSuspendedTransaction {
            user.refreshToken = refreshToken
        }
        
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = user.toResponse()
        )
    }
    
    suspend fun authenticateWithEmail(request: EmailLoginRequest): LoginResponse? {
        val user = newSuspendedTransaction {
            User.find { 
                (Users.email eq request.email) and (Users.authProvider eq AuthProvider.EMAIL) 
            }.firstOrNull()
        } ?: return null
        
        val passwordHash = newSuspendedTransaction { user.passwordHash }
        if (passwordHash == null || !passwordService.verifyPassword(request.password, passwordHash)) {
            return null
        }
        
        val accessToken = jwtService.generateAccessToken(user.id.value)
        val refreshToken = jwtService.generateRefreshToken(user.id.value)
        
        newSuspendedTransaction {
            user.refreshToken = refreshToken
            user.updatedAt = LocalDateTime.now()
        }
        
        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = user.toResponse()
        )
    }
}
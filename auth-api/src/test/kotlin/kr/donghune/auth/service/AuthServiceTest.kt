package kr.donghune.auth.service

import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kr.donghune.auth.domain.AuthProvider
import kr.donghune.auth.domain.User
import kr.donghune.auth.domain.Users
import kr.donghune.auth.dto.EmailLoginRequest
import kr.donghune.auth.dto.RegisterRequest
import kr.donghune.auth.dto.RefreshTokenRequest
import kr.donghune.auth.infrastructure.GoogleOAuthService
import kr.donghune.shared.auth.JwtService
import kr.donghune.shared.auth.PasswordService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthServiceTest {
    private lateinit var authService: AuthService
    private lateinit var jwtService: JwtService
    private lateinit var googleOAuthService: GoogleOAuthService
    private lateinit var passwordService: PasswordService
    
    @Before
    fun setUp() {
        // Initialize in-memory database for testing
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        
        transaction {
            SchemaUtils.create(Users)
        }
        
        jwtService = JwtService()
        googleOAuthService = GoogleOAuthService()
        passwordService = PasswordService()
        
        authService = AuthService(jwtService, googleOAuthService, passwordService)
    }
    
    @After
    fun tearDown() {
        transaction {
            SchemaUtils.drop(Users)
        }
    }
    
    @Test
    fun `test register with email - success`() = runBlocking {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "Password123",
            nickname = "TestUser"
        )
        
        // When
        val response = authService.registerWithEmail(request)
        
        // Then
        assertNotNull(response)
        assertEquals(request.email, response.user.email)
        assertEquals(request.nickname, response.user.nickname)
        assertEquals(AuthProvider.EMAIL, response.user.authProvider)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
        
        // Verify user was created in database
        transaction {
            val user = User.find { Users.email eq request.email }.firstOrNull()
            assertNotNull(user)
            assertEquals(request.email, user.email)
            assertEquals(request.nickname, user.nickname)
            assertEquals(AuthProvider.EMAIL, user.authProvider)
            assertNotNull(user.passwordHash)
            assertEquals(response.refreshToken, user.refreshToken)
        }
    }
    
    @Test
    fun `test register with email - invalid password`() = runBlocking {
        // Given
        val request = RegisterRequest(
            email = "test@example.com",
            password = "weak",  // Too short, no uppercase, no digit
            nickname = "TestUser"
        )
        
        // When
        val response = authService.registerWithEmail(request)
        
        // Then
        assertNull(response)
        
        // Verify user was not created in database
        transaction {
            val user = User.find { Users.email eq request.email }.firstOrNull()
            assertNull(user)
        }
    }
    
    @Test
    fun `test register with email - duplicate email`() = runBlocking {
        // Given
        val email = "test@example.com"
        
        // Create a user with the same email
        transaction {
            User.new {
                this.email = email
                this.passwordHash = "some-hash"
                this.authProvider = AuthProvider.EMAIL
                this.nickname = "ExistingUser"
                this.emailVerified = false
            }
        }
        
        val request = RegisterRequest(
            email = email,
            password = "Password123",
            nickname = "TestUser"
        )
        
        // When
        val response = authService.registerWithEmail(request)
        
        // Then
        assertNull(response)
    }
    
    @Test
    fun `test authenticate with email - success`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "Password123"
        val hashedPassword = passwordService.hashPassword(password)
        
        transaction {
            User.new {
                this.email = email
                this.passwordHash = hashedPassword
                this.authProvider = AuthProvider.EMAIL
                this.nickname = "TestUser"
                this.emailVerified = false
            }
        }
        
        val request = EmailLoginRequest(
            email = email,
            password = password
        )
        
        // When
        val response = authService.authenticateWithEmail(request)
        
        // Then
        assertNotNull(response)
        assertEquals(email, response.user.email)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
        
        // Verify refresh token was updated in database
        transaction {
            val user = User.find { Users.email eq email }.first()
            assertEquals(response.refreshToken, user.refreshToken)
        }
    }
    
    @Test
    fun `test authenticate with email - invalid password`() = runBlocking {
        // Given
        val email = "test@example.com"
        val correctPassword = "Password123"
        val wrongPassword = "WrongPassword123"
        val hashedPassword = passwordService.hashPassword(correctPassword)
        
        transaction {
            User.new {
                this.email = email
                this.passwordHash = hashedPassword
                this.authProvider = AuthProvider.EMAIL
                this.nickname = "TestUser"
                this.emailVerified = false
            }
        }
        
        val request = EmailLoginRequest(
            email = email,
            password = wrongPassword
        )
        
        // When
        val response = authService.authenticateWithEmail(request)
        
        // Then
        assertNull(response)
    }
    
    @Test
    fun `test authenticate with email - user not found`() = runBlocking {
        // Given
        val request = EmailLoginRequest(
            email = "nonexistent@example.com",
            password = "Password123"
        )
        
        // When
        val response = authService.authenticateWithEmail(request)
        
        // Then
        assertNull(response)
    }
    
    @Test
    fun `test refresh access token - success`() = runBlocking {
        // Given
        val userId = transaction {
            User.new {
                this.email = "test@example.com"
                this.passwordHash = "some-hash"
                this.authProvider = AuthProvider.EMAIL
                this.nickname = "TestUser"
                this.emailVerified = false
            }.id.value
        }
        
        val refreshToken = jwtService.generateRefreshToken(userId)
        
        transaction {
            val user = User.findById(userId)!!
            user.refreshToken = refreshToken
        }
        
        // When
        val response = authService.refreshAccessToken(refreshToken)
        
        // Then
        assertNotNull(response)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
        
        // Verify refresh token was updated in database
        transaction {
            val user = User.findById(userId)!!
            assertEquals(response.refreshToken, user.refreshToken)
        }
    }
    
    @Test
    fun `test refresh access token - invalid token`() = runBlocking {
        // Given
        val invalidToken = "invalid-token"
        
        // When
        val response = authService.refreshAccessToken(invalidToken)
        
        // Then
        assertNull(response)
    }
    
    @Test
    fun `test refresh access token - token not in database`() = runBlocking {
        // Given
        val userId = transaction {
            User.new {
                this.email = "test@example.com"
                this.passwordHash = "some-hash"
                this.authProvider = AuthProvider.EMAIL
                this.nickname = "TestUser"
                this.emailVerified = false
            }.id.value
        }
        
        val refreshToken = jwtService.generateRefreshToken(userId)
        
        // When
        val response = authService.refreshAccessToken(refreshToken)
        
        // Then
        assertNull(response)
    }
    
    @Test
    fun `test logout - success`() = runBlocking {
        // Given
        val userId = transaction {
            User.new {
                this.email = "test@example.com"
                this.passwordHash = "some-hash"
                this.authProvider = AuthProvider.EMAIL
                this.nickname = "TestUser"
                this.emailVerified = false
            }.id.value
        }
        
        val refreshToken = jwtService.generateRefreshToken(userId)
        
        transaction {
            val user = User.findById(userId)!!
            user.refreshToken = refreshToken
        }
        
        // When
        val success = authService.logout(refreshToken)
        
        // Then
        assertTrue(success)
        
        // Verify refresh token was removed from database
        transaction {
            val user = User.findById(userId)!!
            assertNull(user.refreshToken)
        }
    }
    
    @Test
    fun `test logout - invalid token`() = runBlocking {
        // Given
        val invalidToken = "invalid-token"
        
        // When
        val success = authService.logout(invalidToken)
        
        // Then
        assertEquals(false, success)
    }
    
    @Test
    fun `test validate access token - success`() = runBlocking {
        // Given
        val userId = transaction {
            User.new {
                this.email = "test@example.com"
                this.passwordHash = "some-hash"
                this.authProvider = AuthProvider.EMAIL
                this.nickname = "TestUser"
                this.emailVerified = false
            }.id.value
        }
        
        val accessToken = jwtService.generateAccessToken(userId)
        
        // When
        val user = authService.validateAccessToken(accessToken)
        
        // Then
        assertNotNull(user)
        assertEquals(userId, user.id.value)
    }
    
    @Test
    fun `test validate access token - invalid token`() = runBlocking {
        // Given
        val invalidToken = "invalid-token"
        
        // When
        val user = authService.validateAccessToken(invalidToken)
        
        // Then
        assertNull(user)
    }
}
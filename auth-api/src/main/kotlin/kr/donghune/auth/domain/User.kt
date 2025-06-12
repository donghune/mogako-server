package kr.donghune.auth.domain

import kr.donghune.auth.dto.UserResponse
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

enum class AuthProvider {
    GOOGLE, EMAIL
}

object Users : IntIdTable() {
    val googleId = varchar("google_id", 255).nullable().uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()
    val authProvider = enumeration("auth_provider", AuthProvider::class)
    val nickname = varchar("nickname", 100)
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val refreshToken = varchar("refresh_token", 500).nullable()
    val emailVerified = bool("email_verified").default(false)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    
    var googleId by Users.googleId
    var email by Users.email
    var passwordHash by Users.passwordHash
    var authProvider by Users.authProvider
    var nickname by Users.nickname
    var profileImageUrl by Users.profileImageUrl
    var refreshToken by Users.refreshToken
    var emailVerified by Users.emailVerified
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt
    
    fun toResponse() = UserResponse(
        id = id.value,
        email = email,
        nickname = nickname,
        profileImageUrl = profileImageUrl,
        authProvider = authProvider,
        emailVerified = emailVerified
    )
}
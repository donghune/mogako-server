package kr.donghune.shared.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class PasswordService {
    
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hashedPassword = hashWithSalt(password, salt)
        return "$salt:$hashedPassword"
    }
    
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        val parts = hashedPassword.split(":")
        if (parts.size != 2) return false
        
        val salt = parts[0]
        val hash = parts[1]
        val hashedInput = hashWithSalt(password, salt)
        
        return hash == hashedInput
    }
    
    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }
    
    private fun hashWithSalt(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray())
        val hashedBytes = md.digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }
    
    fun isValidPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }
}
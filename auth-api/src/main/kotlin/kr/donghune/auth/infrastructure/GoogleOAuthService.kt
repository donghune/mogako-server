package kr.donghune.auth.infrastructure

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GoogleOAuthService {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    
    suspend fun verifyGoogleIdToken(idToken: String): GoogleUserInfo? {
        return try {
            val response = httpClient.get("https://oauth2.googleapis.com/tokeninfo") {
                parameter("id_token", idToken)
            }
            
            if (response.status.value == 200) {
                response.body<GoogleTokenInfo>().let { tokenInfo ->
                    GoogleUserInfo(
                        id = tokenInfo.sub,
                        email = tokenInfo.email,
                        name = tokenInfo.name,
                        picture = tokenInfo.picture
                    )
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    @Serializable
    private data class GoogleTokenInfo(
        val sub: String,
        val email: String,
        val name: String,
        val picture: String?
    )
    
    data class GoogleUserInfo(
        val id: String,
        val email: String,
        val name: String,
        val picture: String?
    )
}
package kr.donghune.shared.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader("X-Requested-With")
        allowCredentials = true
        
        // Allow all origins in development
        anyHost()
        
        // For production, specify allowed origins:
        // allowHost("localhost:3000")
        // allowHost("yourdomain.com", schemes = listOf("https"))
    }
}
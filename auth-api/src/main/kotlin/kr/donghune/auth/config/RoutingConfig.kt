package kr.donghune.auth.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kr.donghune.auth.controller.authRoutes
import kr.donghune.auth.service.AuthService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService by inject<AuthService>()

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        authRoutes(authService)
    }
}
package kr.donghune.auth.config

import io.ktor.server.application.*
import kr.donghune.shared.auth.configureJwtAuthentication

fun Application.configureSecurity() {
    configureJwtAuthentication()
}
package kr.donghune.auth.config

import io.ktor.server.application.*
import kr.donghune.shared.config.configureHTTP

fun Application.configureHTTP() {
    configureHTTP()
}
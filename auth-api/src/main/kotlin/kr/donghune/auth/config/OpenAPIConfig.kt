package kr.donghune.auth.config

import io.ktor.server.application.*
import kr.donghune.shared.config.configureOpenAPI

fun Application.configureOpenAPI() {
    configureOpenAPI()
}
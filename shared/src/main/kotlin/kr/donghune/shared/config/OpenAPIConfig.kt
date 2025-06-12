package kr.donghune.shared.config

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureOpenAPI() {
    routing {
        swaggerUI(path = "docs", swaggerFile = "openapi/documentation.yaml") {
            version = "5.17.14"
        }
    }
}
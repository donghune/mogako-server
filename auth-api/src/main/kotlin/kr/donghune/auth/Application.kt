package kr.donghune.auth

import io.ktor.server.application.*
import kr.donghune.auth.config.*
import kr.donghune.shared.config.configureCORS

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureMonitoring()
    configureHTTP()
    configureCORS()
    configureAdministration()
    configureOpenAPI()
    configureRouting()
}
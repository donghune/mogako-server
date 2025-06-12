package kr.donghune.calendar

import io.ktor.server.application.*
import kr.donghune.calendar.config.*
import kr.donghune.shared.config.configureCORS

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureHTTP()
    configureCORS()
    configureOpenAPI()
    configureRouting()
}
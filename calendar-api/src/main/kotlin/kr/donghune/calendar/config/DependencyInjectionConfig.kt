package kr.donghune.calendar.config

import io.ktor.server.application.*
import kr.donghune.calendar.service.CalendarService
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single { CalendarService() }
        })
    }
}
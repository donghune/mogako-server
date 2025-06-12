package kr.donghune.calendar.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kr.donghune.calendar.controller.calendarRoutes
import kr.donghune.calendar.service.CalendarService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val calendarService by inject<CalendarService>()

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        calendarRoutes(calendarService)
    }
}
package kr.donghune.calendar.config

import io.ktor.server.application.*
import kr.donghune.calendar.domain.CalendarEntries
import kr.donghune.shared.config.configureDatabases

fun Application.configureDatabases() {
    configureDatabases(CalendarEntries)
}
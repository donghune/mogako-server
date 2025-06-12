package kr.donghune.calendar.config

import io.ktor.server.application.*
import kr.donghune.shared.config.configureSerialization

fun Application.configureSerialization() {
    configureSerialization()
}
package kr.donghune.auth.config

import io.ktor.server.application.*
import kr.donghune.auth.domain.Users
import kr.donghune.shared.config.configureDatabases

fun Application.configureDatabases() {
    configureDatabases(Users)
}
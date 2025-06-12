package kr.donghune.auth.config

import io.ktor.server.application.*
import kr.donghune.auth.infrastructure.GoogleOAuthService
import kr.donghune.auth.service.AuthService
import kr.donghune.shared.auth.JwtService
import kr.donghune.shared.auth.PasswordService
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(module {
            single { JwtService() }
            single { PasswordService() }
            single { GoogleOAuthService() }
            single { AuthService(get(), get(), get()) }
        })
    }
}
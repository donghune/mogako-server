package kr.donghune.auth.config

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
    }
    install(CallId) {
        header("X-Request-ID")
        generate { "req-${System.currentTimeMillis()}" }
    }
    install(MicrometerMetrics) {
        registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }
}

fun Application.configureAdministration() {
    // 필요시 관리 기능 추가
}
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    `java-library`
}

group = "kr.donghune"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.koin.ktor)
    api(libs.koin.logger.slf4j)
    api(libs.ktor.server.core)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.ktor.server.content.negotiation)
    api(libs.postgresql)
    api(libs.exposed.core)
    api(libs.exposed.jdbc)
    api(libs.exposed.dao)
    api(libs.exposed.java.time)
    api(libs.ktor.server.auth)
    api(libs.ktor.server.auth.jwt)
    api(libs.ktor.client.core)
    api(libs.ktor.client.apache)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.server.metrics.micrometer)
    api(libs.micrometer.registry.prometheus)
    api(libs.ktor.server.host.common)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.default.headers)
    api(libs.ktor.server.call.logging)
    api(libs.ktor.server.call.id)
    api(libs.ktor.server.cors)
    api(libs.ktor.server.openapi)
    api(libs.ktor.server.swagger)
    api(libs.ktor.server.netty)
    api(libs.logback.classic)
    api(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
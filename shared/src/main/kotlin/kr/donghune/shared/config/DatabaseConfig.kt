package kr.donghune.shared.config

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases(vararg tables: Table) {
    val url = System.getenv("DATABASE_URL") ?: environment.config.property("postgres.url").getString()
    val user = System.getenv("DATABASE_USER") ?: environment.config.property("postgres.user").getString()
    val password = System.getenv("DATABASE_PASSWORD") ?: environment.config.property("postgres.password").getString()

    val database = Database.connect(
        url = System.getenv("DATABASE_URL") ?: environment.config.property("postgres.url").getString(),
        driver = "org.postgresql.Driver",
        user = System.getenv("DATABASE_USER") ?: environment.config.property("postgres.user").getString(),
        password = System.getenv("DATABASE_PASSWORD") ?: environment.config.property("postgres.password").getString()
    )

    println("Connecting to database at $url with user $user, password ${if (password.isNotEmpty()) "******" else "not set"}")

    transaction(database) {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(*tables)
    }
}
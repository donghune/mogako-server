package kr.donghune.calendar.controller

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kr.donghune.calendar.dto.CreateCalendarEntryRequest
import kr.donghune.calendar.dto.UpdateCalendarEntryRequest
import kr.donghune.calendar.service.CalendarService
import java.time.LocalDate

fun Route.calendarRoutes(calendarService: CalendarService) {
    authenticate("auth-jwt") {
        route("/calendar") {
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.subject.toInt()

                val dateParam = call.request.queryParameters["date"]

                val entries = if (dateParam != null) {
                    try {
                        val date = LocalDate.parse(dateParam)
                        calendarService.getCalendarEntriesByDate(userId, date)
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid date format. Use YYYY-MM-DD"))
                        return@get
                    }
                } else {
                    calendarService.getUserCalendarEntries(userId)
                }

                call.respond(HttpStatusCode.OK, entries.map { it.toResponse() })
            }

            get("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.subject.toInt()

                val entryId = call.parameters["id"]?.toIntOrNull()
                if (entryId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid entry ID"))
                    return@get
                }

                val entry = calendarService.getCalendarEntry(userId, entryId)
                if (entry != null) {
                    call.respond(HttpStatusCode.OK, entry.toResponse())
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calendar entry not found"))
                }
            }

            post {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.subject.toInt()

                try {
                    val request = call.receive<CreateCalendarEntryRequest>()
                    val entry = calendarService.createCalendarEntry(userId, request)
                    call.respond(HttpStatusCode.Created, entry.toResponse())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                }
            }

            put("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.subject.toInt()

                val entryId = call.parameters["id"]?.toIntOrNull()
                if (entryId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid entry ID"))
                    return@put
                }

                try {
                    val request = call.receive<UpdateCalendarEntryRequest>()
                    val updatedEntry = calendarService.updateCalendarEntry(userId, entryId, request)

                    if (updatedEntry != null) {
                        call.respond(HttpStatusCode.OK, updatedEntry.toResponse())
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calendar entry not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request format"))
                }
            }

            delete("/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.subject.toInt()

                val entryId = call.parameters["id"]?.toIntOrNull()
                if (entryId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid entry ID"))
                    return@delete
                }

                val success = calendarService.deleteCalendarEntry(userId, entryId)
                if (success) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Calendar entry deleted successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Calendar entry not found"))
                }
            }
        }
    }
}
package kr.donghune.calendar.domain

import kr.donghune.calendar.dto.CalendarEntryResponse
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDate
import java.time.LocalDateTime

enum class Mood {
    ANGRY, SAD, HAPPY
}

object CalendarEntries : IntIdTable() {
    val userId = integer("user_id")
    val date = date("date")
    val summary = varchar("summary", 200)
    val content = text("content")
    val mood = enumeration("mood", Mood::class)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

class CalendarEntry(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CalendarEntry>(CalendarEntries)
    
    var userId by CalendarEntries.userId
    var date by CalendarEntries.date
    var summary by CalendarEntries.summary
    var content by CalendarEntries.content
    var mood by CalendarEntries.mood
    var createdAt by CalendarEntries.createdAt
    var updatedAt by CalendarEntries.updatedAt
    
    fun toResponse() = CalendarEntryResponse(
        id = id.value,
        date = date,
        summary = summary,
        content = content,
        mood = mood,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
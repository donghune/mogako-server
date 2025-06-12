package kr.donghune.calendar.dto

import kr.donghune.calendar.domain.Mood
import kr.donghune.shared.dto.LocalDateSerializer
import kr.donghune.shared.dto.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

@Serializable
data class CalendarEntryResponse(
    val id: Int,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val summary: String,
    val content: String,
    val mood: Mood,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)

@Serializable
data class CreateCalendarEntryRequest(
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val summary: String,
    val content: String,
    val mood: Mood
)

@Serializable
data class UpdateCalendarEntryRequest(
    val summary: String?,
    val content: String?,
    val mood: Mood?
)
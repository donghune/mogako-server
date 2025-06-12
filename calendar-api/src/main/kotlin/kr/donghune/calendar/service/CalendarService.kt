package kr.donghune.calendar.service

import kr.donghune.calendar.domain.CalendarEntry
import kr.donghune.calendar.domain.CalendarEntries
import kr.donghune.calendar.domain.Mood
import kr.donghune.calendar.dto.CreateCalendarEntryRequest
import kr.donghune.calendar.dto.UpdateCalendarEntryRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate
import java.time.LocalDateTime

class CalendarService {
    
    suspend fun getUserCalendarEntries(userId: Int): List<CalendarEntry> {
        return newSuspendedTransaction {
            CalendarEntry.find { CalendarEntries.userId eq userId }.toList()
        }
    }
    
    suspend fun getCalendarEntry(userId: Int, entryId: Int): CalendarEntry? {
        return newSuspendedTransaction {
            CalendarEntry.find { 
                (CalendarEntries.userId eq userId) and (CalendarEntries.id eq entryId) 
            }.firstOrNull()
        }
    }
    
    suspend fun getCalendarEntriesByDate(userId: Int, date: LocalDate): List<CalendarEntry> {
        return newSuspendedTransaction {
            CalendarEntry.find { 
                (CalendarEntries.userId eq userId) and (CalendarEntries.date eq date) 
            }.toList()
        }
    }
    
    suspend fun createCalendarEntry(userId: Int, request: CreateCalendarEntryRequest): CalendarEntry {
        return newSuspendedTransaction {
            CalendarEntry.new {
                this.userId = userId
                this.date = request.date
                this.summary = request.summary
                this.content = request.content
                this.mood = request.mood
            }
        }
    }
    
    suspend fun updateCalendarEntry(
        userId: Int, 
        entryId: Int, 
        request: UpdateCalendarEntryRequest
    ): CalendarEntry? {
        return newSuspendedTransaction {
            val entry = CalendarEntry.find { 
                (CalendarEntries.userId eq userId) and (CalendarEntries.id eq entryId) 
            }.firstOrNull()
            
            if (entry != null) {
                request.summary?.let { entry.summary = it }
                request.content?.let { entry.content = it }
                request.mood?.let { entry.mood = it }
                entry.updatedAt = LocalDateTime.now()
                entry
            } else {
                null
            }
        }
    }
    
    suspend fun deleteCalendarEntry(userId: Int, entryId: Int): Boolean {
        return newSuspendedTransaction {
            val entry = CalendarEntry.find { 
                (CalendarEntries.userId eq userId) and (CalendarEntries.id eq entryId) 
            }.firstOrNull()
            
            if (entry != null) {
                entry.delete()
                true
            } else {
                false
            }
        }
    }
}
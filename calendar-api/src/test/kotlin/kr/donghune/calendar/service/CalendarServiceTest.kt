package kr.donghune.calendar.service

import kotlinx.coroutines.runBlocking
import kr.donghune.calendar.domain.CalendarEntries
import kr.donghune.calendar.domain.CalendarEntry
import kr.donghune.calendar.domain.Mood
import kr.donghune.calendar.dto.CreateCalendarEntryRequest
import kr.donghune.calendar.dto.UpdateCalendarEntryRequest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.*

class CalendarServiceTest {
    private lateinit var calendarService: CalendarService
    
    @Before
    fun setUp() {
        // Initialize in-memory database for testing
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        
        transaction {
            SchemaUtils.create(CalendarEntries)
        }
        
        calendarService = CalendarService()
    }
    
    @After
    fun tearDown() {
        transaction {
            SchemaUtils.drop(CalendarEntries)
        }
    }
    
    @Test
    fun `test getUserCalendarEntries - returns entries for user`() {
        runBlocking {
            // Given
            val userId = 1
            val otherUserId = 2
            
            transaction {
                // Create entries for the user
                CalendarEntry.new {
                    this.userId = userId
                    this.date = LocalDate.now()
                    this.summary = "Test Entry 1"
                    this.content = "Test Content 1"
                    this.mood = Mood.HAPPY
                }
                
                CalendarEntry.new {
                    this.userId = userId
                    this.date = LocalDate.now().minusDays(1)
                    this.summary = "Test Entry 2"
                    this.content = "Test Content 2"
                    this.mood = Mood.SAD
                }
                
                // Create an entry for another user
                CalendarEntry.new {
                    this.userId = otherUserId
                    this.date = LocalDate.now()
                    this.summary = "Other User Entry"
                    this.content = "Other User Content"
                    this.mood = Mood.ANGRY
                }
            }
            
            // When
            val entries = calendarService.getUserCalendarEntries(userId)
            
            // Then
            assertEquals(2, entries.size)
            entries.forEach { entry ->
                assertEquals(userId, entry.userId)
            }
        }
    }
    
    @Test
    fun `test getUserCalendarEntries - returns empty list when no entries`() {
        runBlocking {
            // Given
            val userId = 1
            
            // When
            val entries = calendarService.getUserCalendarEntries(userId)
            
            // Then
            assertTrue(entries.isEmpty())
        }
    }
    
    @Test
    fun `test getCalendarEntry - returns entry when found`() {
        runBlocking {
            // Given
            val userId = 1
            val entryId = transaction {
                CalendarEntry.new {
                    this.userId = userId
                    this.date = LocalDate.now()
                    this.summary = "Test Entry"
                    this.content = "Test Content"
                    this.mood = Mood.HAPPY
                }.id.value
            }
            
            // When
            val entry = calendarService.getCalendarEntry(userId, entryId)
            
            // Then
            assertNotNull(entry)
            assertEquals(userId, entry.userId)
            assertEquals("Test Entry", entry.summary)
            assertEquals("Test Content", entry.content)
            assertEquals(Mood.HAPPY, entry.mood)
        }
    }
    
    @Test
    fun `test getCalendarEntry - returns null when not found`() {
        runBlocking {
            // Given
            val userId = 1
            val nonExistentEntryId = 999
            
            // When
            val entry = calendarService.getCalendarEntry(userId, nonExistentEntryId)
            
            // Then
            assertNull(entry)
        }
    }
    
    @Test
    fun `test getCalendarEntry - returns null when entry belongs to another user`() {
        runBlocking {
            // Given
            val userId = 1
            val otherUserId = 2
            val entryId = transaction {
                CalendarEntry.new {
                    this.userId = otherUserId
                    this.date = LocalDate.now()
                    this.summary = "Other User Entry"
                    this.content = "Other User Content"
                    this.mood = Mood.ANGRY
                }.id.value
            }
            
            // When
            val entry = calendarService.getCalendarEntry(userId, entryId)
            
            // Then
            assertNull(entry)
        }
    }
    
    @Test
    fun `test getCalendarEntriesByDate - returns entries for date`() {
        runBlocking {
            // Given
            val userId = 1
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            
            transaction {
                // Create entries for today
                CalendarEntry.new {
                    this.userId = userId
                    this.date = today
                    this.summary = "Today Entry 1"
                    this.content = "Today Content 1"
                    this.mood = Mood.HAPPY
                }
                
                CalendarEntry.new {
                    this.userId = userId
                    this.date = today
                    this.summary = "Today Entry 2"
                    this.content = "Today Content 2"
                    this.mood = Mood.SAD
                }
                
                // Create an entry for yesterday
                CalendarEntry.new {
                    this.userId = userId
                    this.date = yesterday
                    this.summary = "Yesterday Entry"
                    this.content = "Yesterday Content"
                    this.mood = Mood.ANGRY
                }
            }
            
            // When
            val entries = calendarService.getCalendarEntriesByDate(userId, today)
            
            // Then
            assertEquals(2, entries.size)
            entries.forEach { entry ->
                assertEquals(userId, entry.userId)
                assertEquals(today, entry.date)
            }
        }
    }
    
    @Test
    fun `test getCalendarEntriesByDate - returns empty list when no entries for date`() {
        runBlocking {
            // Given
            val userId = 1
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            
            transaction {
                CalendarEntry.new {
                    this.userId = userId
                    this.date = today
                    this.summary = "Today Entry"
                    this.content = "Today Content"
                    this.mood = Mood.HAPPY
                }
            }
            
            // When
            val entries = calendarService.getCalendarEntriesByDate(userId, tomorrow)
            
            // Then
            assertTrue(entries.isEmpty())
        }
    }
    
    @Test
    fun `test createCalendarEntry - creates new entry`() {
        runBlocking {
            // Given
            val userId = 1
            val date = LocalDate.now()
            val request = CreateCalendarEntryRequest(
                date = date,
                summary = "New Entry",
                content = "New Content",
                mood = Mood.HAPPY
            )
            
            // When
            val entry = calendarService.createCalendarEntry(userId, request)
            
            // Then
            assertEquals(userId, entry.userId)
            assertEquals(date, entry.date)
            assertEquals("New Entry", entry.summary)
            assertEquals("New Content", entry.content)
            assertEquals(Mood.HAPPY, entry.mood)
            
            // Verify entry was created in database
            transaction {
                val dbEntry = CalendarEntry.findById(entry.id)
                assertNotNull(dbEntry)
                assertEquals(userId, dbEntry.userId)
                assertEquals(date, dbEntry.date)
                assertEquals("New Entry", dbEntry.summary)
                assertEquals("New Content", dbEntry.content)
                assertEquals(Mood.HAPPY, dbEntry.mood)
            }
        }
    }
    
    @Test
    fun `test updateCalendarEntry - updates existing entry`() {
        runBlocking {
            // Given
            val userId = 1
            val entryId = transaction {
                CalendarEntry.new {
                    this.userId = userId
                    this.date = LocalDate.now()
                    this.summary = "Original Summary"
                    this.content = "Original Content"
                    this.mood = Mood.SAD
                }.id.value
            }
            
            val request = UpdateCalendarEntryRequest(
                summary = "Updated Summary",
                content = "Updated Content",
                mood = Mood.HAPPY
            )
            
            // When
            val updatedEntry = calendarService.updateCalendarEntry(userId, entryId, request)
            
            // Then
            assertNotNull(updatedEntry)
            assertEquals("Updated Summary", updatedEntry.summary)
            assertEquals("Updated Content", updatedEntry.content)
            assertEquals(Mood.HAPPY, updatedEntry.mood)
            
            // Verify entry was updated in database
            transaction {
                val dbEntry = CalendarEntry.findById(entryId)
                assertNotNull(dbEntry)
                assertEquals("Updated Summary", dbEntry.summary)
                assertEquals("Updated Content", dbEntry.content)
                assertEquals(Mood.HAPPY, dbEntry.mood)
            }
        }
    }
    
    @Test
    fun `test updateCalendarEntry - returns null when entry not found`() {
        runBlocking {
            // Given
            val userId = 1
            val nonExistentEntryId = 999
            
            val request = UpdateCalendarEntryRequest(
                summary = "Updated Summary",
                content = "Updated Content",
                mood = Mood.HAPPY
            )
            
            // When
            val updatedEntry = calendarService.updateCalendarEntry(userId, nonExistentEntryId, request)
            
            // Then
            assertNull(updatedEntry)
        }
    }
    
    @Test
    fun `test updateCalendarEntry - returns null when entry belongs to another user`() {
        runBlocking {
            // Given
            val userId = 1
            val otherUserId = 2
            val entryId = transaction {
                CalendarEntry.new {
                    this.userId = otherUserId
                    this.date = LocalDate.now()
                    this.summary = "Other User Entry"
                    this.content = "Other User Content"
                    this.mood = Mood.ANGRY
                }.id.value
            }
            
            val request = UpdateCalendarEntryRequest(
                summary = "Updated Summary",
                content = "Updated Content",
                mood = Mood.HAPPY
            )
            
            // When
            val updatedEntry = calendarService.updateCalendarEntry(userId, entryId, request)
            
            // Then
            assertNull(updatedEntry)
            
            // Verify entry was not updated in database
            transaction {
                val dbEntry = CalendarEntry.findById(entryId)
                assertNotNull(dbEntry)
                assertEquals("Other User Entry", dbEntry.summary)
                assertEquals("Other User Content", dbEntry.content)
                assertEquals(Mood.ANGRY, dbEntry.mood)
            }
        }
    }
    
    @Test
    fun `test updateCalendarEntry - partial update with null fields`() {
        runBlocking {
            // Given
            val userId = 1
            val entryId = transaction {
                CalendarEntry.new {
                    this.userId = userId
                    this.date = LocalDate.now()
                    this.summary = "Original Summary"
                    this.content = "Original Content"
                    this.mood = Mood.SAD
                }.id.value
            }
            
            val request = UpdateCalendarEntryRequest(
                summary = "Updated Summary",
                content = null,
                mood = null
            )
            
            // When
            val updatedEntry = calendarService.updateCalendarEntry(userId, entryId, request)
            
            // Then
            assertNotNull(updatedEntry)
            assertEquals("Updated Summary", updatedEntry.summary)
            assertEquals("Original Content", updatedEntry.content)
            assertEquals(Mood.SAD, updatedEntry.mood)
            
            // Verify entry was updated in database
            transaction {
                val dbEntry = CalendarEntry.findById(entryId)
                assertNotNull(dbEntry)
                assertEquals("Updated Summary", dbEntry.summary)
                assertEquals("Original Content", dbEntry.content)
                assertEquals(Mood.SAD, dbEntry.mood)
            }
        }
    }
    
    @Test
    fun `test deleteCalendarEntry - deletes existing entry`() {
        runBlocking {
            // Given
            val userId = 1
            val entryId = transaction {
                CalendarEntry.new {
                    this.userId = userId
                    this.date = LocalDate.now()
                    this.summary = "Test Entry"
                    this.content = "Test Content"
                    this.mood = Mood.HAPPY
                }.id.value
            }
            
            // When
            val success = calendarService.deleteCalendarEntry(userId, entryId)
            
            // Then
            assertTrue(success)
            
            // Verify entry was deleted from database
            transaction {
                val dbEntry = CalendarEntry.findById(entryId)
                assertNull(dbEntry)
            }
        }
    }
    
    @Test
    fun `test deleteCalendarEntry - returns false when entry not found`() {
        runBlocking {
            // Given
            val userId = 1
            val nonExistentEntryId = 999
            
            // When
            val success = calendarService.deleteCalendarEntry(userId, nonExistentEntryId)
            
            // Then
            assertFalse(success)
        }
    }
    
    @Test
    fun `test deleteCalendarEntry - returns false when entry belongs to another user`() {
        runBlocking {
            // Given
            val userId = 1
            val otherUserId = 2
            val entryId = transaction {
                CalendarEntry.new {
                    this.userId = otherUserId
                    this.date = LocalDate.now()
                    this.summary = "Other User Entry"
                    this.content = "Other User Content"
                    this.mood = Mood.ANGRY
                }.id.value
            }
            
            // When
            val success = calendarService.deleteCalendarEntry(userId, entryId)
            
            // Then
            assertFalse(success)
            
            // Verify entry was not deleted from database
            transaction {
                val dbEntry = CalendarEntry.findById(entryId)
                assertNotNull(dbEntry)
            }
        }
    }
}
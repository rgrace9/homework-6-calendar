package edu.northeastern.cs5010;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CalendarImportTest {

  @TempDir
  Path tempDir;

  @Test
  void testImportFromCsv() throws IOException {
    Calendar calendar = getCalendar();

    List<Event> events = calendar.getEvents();
    assertEquals(5, events.size(), "Calendar should have 5 imported events");

    Event teamMeeting = calendar.getEvent("Team Meeting", LocalDate.of(2025, 11, 15),
        LocalTime.of(10, 0));
    assertNotNull(teamMeeting, "Team Meeting should be imported");
    assertEquals("Team Meeting", teamMeeting.getSubject());
    assertEquals(LocalDate.of(2025, 11, 15), teamMeeting.getStartDate());
    assertEquals(LocalDate.of(2025, 11, 15), teamMeeting.getEndDate());
    assertEquals(LocalTime.of(10, 0), teamMeeting.getStartTime());
    assertEquals(LocalTime.of(11, 0), teamMeeting.getEndTime());
    assertEquals("Weekly sync", teamMeeting.getDescription());
    assertEquals("Conference Room A", teamMeeting.getLocation());
    assertEquals(Event.Visibility.PUBLIC, teamMeeting.getVisibility());
    assertFalse(teamMeeting.isAllDayEvent());

    Event birthday = calendar.getEvent("Birthday Party", LocalDate.of(2025, 11, 20), null);
    assertNotNull(birthday, "Birthday Party should be imported");
    assertEquals("Birthday Party", birthday.getSubject());
    assertEquals(LocalDate.of(2025, 11, 20), birthday.getStartDate());
    assertEquals(LocalDate.of(2025, 11, 20), birthday.getEndDate());
    assertNull(birthday.getStartTime());
    assertNull(birthday.getEndTime());
    assertEquals("John's birthday celebration", birthday.getDescription());
    assertEquals("His house", birthday.getLocation());
    assertEquals(Event.Visibility.PRIVATE, birthday.getVisibility());
    assertTrue(birthday.isAllDayEvent());

    Event projectReview = calendar.getEvent("Project Review, Q4",
        LocalDate.of(2025, 11, 22), LocalTime.of(14, 0));
    assertNotNull(projectReview, "Project Review should be imported");
    assertEquals("Project Review, Q4", projectReview.getSubject());
    assertEquals("Review quarterly goals, metrics", projectReview.getDescription());
    assertEquals("Room 123, Building B", projectReview.getLocation());
    assertEquals(LocalTime.of(14, 0), projectReview.getStartTime());
    assertEquals(LocalTime.of(16, 0), projectReview.getEndTime());

    Event vacation = calendar.getEvent("Vacation", LocalDate.of(2025, 12, 1), null);
    assertNotNull(vacation, "Vacation should be imported");
    assertEquals("Vacation", vacation.getSubject());
    assertEquals(LocalDate.of(2025, 12, 1), vacation.getStartDate());
    assertEquals(LocalDate.of(2025, 12, 5), vacation.getEndDate());
    assertNull(vacation.getDescription());
    assertNull(vacation.getLocation());
    assertTrue(vacation.isAllDayEvent());

    Event dentist = calendar.getEvent("Dentist Appointment",
        LocalDate.of(2025, 11, 18), LocalTime.of(9, 30));
    assertNotNull(dentist, "Dentist Appointment should be imported");
    assertEquals("Dentist Appointment", dentist.getSubject());
    assertNull(dentist.getDescription());
    assertNull(dentist.getLocation());
    assertEquals(Event.Visibility.PRIVATE, dentist.getVisibility());
  }

  private Calendar getCalendar() throws FileNotFoundException {
    Path csvFile = tempDir.resolve("test_calendar.csv");
    try (PrintWriter writer = new PrintWriter(csvFile.toFile())) {
      writer.println(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");
      writer.println(
          "Team Meeting,11/15/2025,10:00 AM,11/15/2025,11:00 AM,False,Weekly sync,Conference Room A,False");
      writer.println(
          "Birthday Party,11/20/2025,,,11/20/2025,,True,John's birthday celebration,His house,True");
      writer.println(
          "\"Project Review, Q4\",11/22/2025,2:00 PM,11/22/2025,4:00 PM,False,\"Review quarterly goals, metrics\",\"Room 123, Building B\",False");
      writer.println("Vacation,12/01/2025,,,12/05/2025,,True,,,False");
      writer.println("Dentist Appointment,11/18/2025,9:30 AM,11/18/2025,10:30 AM,False,,,True");
    }

    Calendar calendar = new Calendar("Test Calendar");
    calendar.importFromCsv(csvFile.toString());
    return calendar;
  }

  @Test
  void testImportEmptyFile() throws IOException {
    Path csvFile = tempDir.resolve("empty.csv");
    try (PrintWriter writer = new PrintWriter(csvFile.toFile())) {
      // Only write header, no events
      writer.println(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");
    }

    Calendar calendar = new Calendar("Empty Calendar");
    calendar.importFromCsv(csvFile.toString());

    assertEquals(0, calendar.getEvents().size(), "Calendar should have no events");
  }

  @Test
  void testImportInvalidCsvFormat() throws IOException {
    Path csvFile = tempDir.resolve("invalid.csv");
    try (PrintWriter writer = new PrintWriter(csvFile.toFile())) {
      writer.println(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");
      writer.println("Meeting,11/15/2025,10:00 AM");
    }

    Calendar calendar = new Calendar("Test Calendar");

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      calendar.importFromCsv(csvFile.toString());
    });

    assertTrue(exception.getMessage().contains("Invalid CSV format"));
  }

  @Test
  void testImportInvalidDate() throws IOException {
    Path csvFile = tempDir.resolve("invalid_date.csv");
    try (PrintWriter writer = new PrintWriter(csvFile.toFile())) {
      writer.println(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");
      writer.println("Meeting,Invalid Date,10:00 AM,11/15/2025,11:00 AM,False,,,False");
    }

    Calendar calendar = new Calendar("Test Calendar");

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      calendar.importFromCsv(csvFile.toString());
    });

    assertTrue(exception.getMessage().contains("Error parsing line"));
  }

  @Test
  void testImportAndExportRoundTrip() throws IOException {
    Calendar originalCalendar = new Calendar("Original Calendar");

    Event event1 = new Event.Builder("Meeting",
        LocalDate.of(2025, 11, 15),
        LocalDate.of(2025, 11, 15))
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .description("Important meeting")
        .location("Office")
        .visibility(Event.Visibility.PRIVATE)
        .build();

    Event event2 = new Event.Builder("All Day Event",
        LocalDate.of(2025, 11, 20),
        LocalDate.of(2025, 11, 20))
        .description("Celebration")
        .build();

    originalCalendar.addEvent(event1);
    originalCalendar.addEvent(event2);

    Path exportFile = tempDir.resolve("export.csv");
    originalCalendar.exportToCsv(exportFile.toString());

    Calendar importedCalendar = new Calendar("Imported Calendar");
    importedCalendar.importFromCsv(exportFile.toString());

    assertEquals(2, importedCalendar.getEvents().size());

    Event importedEvent1 = importedCalendar.getEvent("Meeting",
        LocalDate.of(2025, 11, 15), LocalTime.of(10, 0));
    assertNotNull(importedEvent1);
    assertEquals("Important meeting", importedEvent1.getDescription());
    assertEquals("Office", importedEvent1.getLocation());
    assertEquals(Event.Visibility.PRIVATE, importedEvent1.getVisibility());

    Event importedEvent2 = importedCalendar.getEvent("All Day Event",
        LocalDate.of(2025, 11, 20), null);
    assertNotNull(importedEvent2);
    assertEquals("Celebration", importedEvent2.getDescription());
    assertTrue(importedEvent2.isAllDayEvent());
  }

  @Test
  void testImportNonexistentFile() {
    Calendar calendar = new Calendar("Test Calendar");

    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
      calendar.importFromCsv("nonexistent_file.csv");
    });

    assertTrue(exception.getMessage().contains("Failed to import calendar"));
  }
}
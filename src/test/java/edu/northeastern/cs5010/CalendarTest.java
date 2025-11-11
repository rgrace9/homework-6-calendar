package edu.northeastern.cs5010;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import edu.northeastern.cs5010.model.Event.Visibility;
import edu.northeastern.cs5010.model.RecurringEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class CalendarTest {

  private Calendar calendar;
  private Calendar exerciseCalendar;
  private LocalDate nov15;
  private LocalDate nov16;
  private Event meetingEvent;
  private Event lunchEvent;
  private Event conferenceAllDayEvent;
  private String pilatesSeriesId;
  private RecurringEvent pilatesRecurringEvent;
  private Path tempFile;

  @BeforeEach
  public void setUp() throws IOException {
    calendar = new Calendar("Work Calendar");
    exerciseCalendar = new Calendar("Exercise Calendar");
    tempFile = Files.createTempFile("calendar_export_test", ".csv");

    nov15 = LocalDate.of(2025, 11, 15);
    nov16 = LocalDate.of(2025, 11, 16);
    meetingEvent = new Event.Builder("Meeting", nov15, nov15)
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(10, 30))
        .build();
    lunchEvent = new Event.Builder("Lunch", nov15, nov15)
        .startTime(LocalTime.of(12, 0))
        .endTime(LocalTime.of(13, 0))
        .build();
    conferenceAllDayEvent = new Event.Builder("Conference", nov16, nov16).build();
    Event pilates = new Event.Builder("Pilates", LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(6, 0))
        .endTime(LocalTime.of(6, 50))
        .build();
    pilatesRecurringEvent = new RecurringEvent(
        pilates,
        List.of(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY),
        4,
        null
    );
    exerciseCalendar.addRecurringEvent(pilatesRecurringEvent);
    pilatesSeriesId = pilatesRecurringEvent.getSeriesId();
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.deleteIfExists(tempFile);
  }

  @Test
  public void constructorWithValidTitle() {
    Calendar cal = new Calendar("My Calendar");
    assertEquals("My Calendar", cal.getTitle());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  public void constructorWithInvalidTitle(String title) {
    assertThrows(IllegalArgumentException.class, () -> {
      new Calendar(title);
    });
  }

  @Test
  public void getTitle() {
    assertEquals("Work Calendar", calendar.getTitle());
  }

  @Test
  public void addSingleEventResultsInOneEvent() {
    calendar.addEvent(meetingEvent);

    assertEquals(1, calendar.getEvents().size());
  }

  @Test
  public void addSingleEventContainsEvent() {
    calendar.addEvent(meetingEvent);

    assertTrue(calendar.getEvents().contains(meetingEvent));
  }

  @Test
  public void addMultipleEventsResultsInCorrectSize() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);
    calendar.addEvent(conferenceAllDayEvent);

    assertEquals(3, calendar.getEvents().size());
  }


  @Test
  public void getEventByDetailsFoundReturnsCorrectEvent() {
    calendar.addEvent(meetingEvent);
    Event found = calendar.getEvent("Meeting", nov15, LocalTime.of(10, 0));

    assertEquals(meetingEvent, found);
  }

  @ParameterizedTest
  @CsvSource({
      "Workout session, 2025-11-15, 10:00",
      "Meeting, 2025-11-16, 10:00",
      "Meeting, 2025-11-15, 14:00"
  })
  public void getEventByDetailsNotFound(String subject, String dateStr, String timeStr) {
    calendar.addEvent(lunchEvent);
    Event found = calendar.getEvent(subject, LocalDate.parse(dateStr), LocalTime.parse(timeStr));

    assertNull(found);
  }

  @Test
  public void getEventsOnDateWithEventsReturnsCorrectSize() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);
    calendar.addEvent(conferenceAllDayEvent);

    assertEquals(2, calendar.getEventsOnDate(nov15).size());
  }

  @Test
  public void getEventsOnDateContainsFirstEvent() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);

    assertTrue(calendar.getEventsOnDate(nov15).contains(meetingEvent));
  }

  @Test
  public void getEventsOnDateContainsSecondEvent() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);

    assertTrue(calendar.getEventsOnDate(nov15).contains(lunchEvent));
  }

  @Test
  public void getEventsOnDateNoEventsReturnsEmpty() {
    calendar.addEvent(meetingEvent);

    assertTrue(calendar.getEventsOnDate(LocalDate.of(2025, 11, 20)).isEmpty());
  }

  @Test
  public void getEventsInDateRangeReturnsCorrectSize() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);
    calendar.addEvent(conferenceAllDayEvent);

    assertEquals(3, calendar.getEventsInDateRange(nov15, nov16).size());
  }

  @Test
  public void getEventsInDateRangeNoEventsReturnsEmpty() {
    calendar.addEvent(conferenceAllDayEvent);

    assertTrue(calendar.getEventsInDateRange(
        LocalDate.of(2025, 11, 20),
        LocalDate.of(2025, 11, 25)).isEmpty());
  }

  @Test
  public void getEventsInDateRangeIncludesStartDate() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(conferenceAllDayEvent);

    assertTrue(calendar.getEventsInDateRange(nov15, nov16).contains(meetingEvent));
  }

  @Test
  public void getEventsInDateRangeIncludesEndDate() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(conferenceAllDayEvent);

    assertTrue(calendar.getEventsInDateRange(nov15, nov16).contains(conferenceAllDayEvent));
  }

  @ParameterizedTest
  @CsvSource({
      // During event
      "2025-11-15, 10:00, true",
      // Middle of event
      "2025-11-15, 10:30, true",
      // End of event
      "2025-11-15, 10:59, true",
      // After event
      "2025-11-15, 14:00, false",
      // Different date
      "2025-11-16, 10:30, false"
  })
  public void isUserBusy(String dateStr, String timeStr, boolean expected) {
    Event event = new Event.Builder("Dentist Appointment", nov15, nov15)
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event);

    assertEquals(expected, calendar.isUserBusy(
        LocalDate.parse(dateStr),
        LocalTime.parse(timeStr)));
  }

  @Test
  public void userBusyDuringEventReturnsTrue() {
    calendar.addEvent(conferenceAllDayEvent);
    assertTrue(calendar.isUserBusy(nov16, LocalTime.of(9, 30)));
  }

  @Test
  public void userNotBusyOutsideEventReturnsFalse() {
    calendar.addEvent(lunchEvent);
    assertFalse(calendar.isUserBusy(nov15, LocalTime.of(14, 15)));
  }

  @Test
  public void allDayEventMakesUserBusyAllDay() {
    calendar.addEvent(conferenceAllDayEvent);
    assertTrue(calendar.isUserBusy(nov16, LocalTime.of(15, 0)));
  }


  @Test
  public void editEventSuccessfullyUpdates() {
    calendar.addEvent(conferenceAllDayEvent);

    Event updated = conferenceAllDayEvent.toBuilder().subject("Interview").build();
    calendar.editEvent(conferenceAllDayEvent, updated);

    assertEquals("Interview", calendar.getEvent("Interview", nov16, null).getSubject());
  }

  @Test
  void testEditEventUpdatesEventInList() {
    calendar.addEvent(lunchEvent);
    calendar.addEvent(meetingEvent);

    Event updatedMeeting = meetingEvent.toBuilder()
        .startTime(LocalTime.of(14, 0))
        .endTime(LocalTime.of(16, 0))
        .build();

    calendar.editEvent(meetingEvent, updatedMeeting);

    Event result = calendar.getEvents().get(1);
    assertEquals(LocalTime.of(14, 0), result.getStartTime());
  }


  @Test
  public void editEventThrowsWhenDuplicateExists() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);

    Event lunchDuplicate = meetingEvent.toBuilder()
        .subject("Lunch")
        .startTime(LocalTime.of(12, 0))
        .endTime(LocalTime.of(13, 0))
        .build();
    assertThrows(IllegalArgumentException.class,
        () -> calendar.editEvent(meetingEvent, lunchDuplicate));
  }

  @Test
  public void editEventThrowsWhenConflictNotAllowed() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);

    Event overlapWithLunch = meetingEvent.toBuilder()
        .startTime(LocalTime.of(12, 25))
        .endTime(LocalTime.of(12, 55))
        .build();
    assertThrows(IllegalArgumentException.class,
        () -> calendar.editEvent(meetingEvent, overlapWithLunch));
  }

  @Test
  public void editEventAllowsConflicts() {
    Calendar calendarWithConflictsAllowed = new Calendar("Personal Calendar", true);
    calendarWithConflictsAllowed.addEvent(meetingEvent);
    calendarWithConflictsAllowed.addEvent(lunchEvent);

    Event overlapWithLunch = meetingEvent.toBuilder()
        .startTime(LocalTime.of(12, 25))
        .endTime(LocalTime.of(12, 55))
        .build();
    calendarWithConflictsAllowed.editEvent(meetingEvent, overlapWithLunch);
    assertEquals(LocalTime.of(12, 25),
        calendarWithConflictsAllowed.getEvent("Meeting", nov15, LocalTime.of(12, 25))
            .getStartTime());
  }

  @Test
  void addRecurringEventAddsAllInstances() {
    assertEquals(4, exerciseCalendar.getEvents().size());
  }

  @Test
  void recurringEventRejectedIfConflictExists() {
    Event conflictingEvent = new Event.Builder("Stretching",
        LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(6, 30))
        .endTime(LocalTime.of(7, 0))
        .build();

    RecurringEvent overlappingRecurring = new RecurringEvent(
        conflictingEvent,
        List.of(DayOfWeek.SUNDAY),
        2,
        null
    );

    assertThrows(IllegalArgumentException.class,
        () -> exerciseCalendar.addRecurringEvent(overlappingRecurring));
  }

  @Test
  void editSingleInstanceOfRecurringEvent() {
    Event firstInstance = exerciseCalendar.getEvents().getFirst();

    Event updated = firstInstance.toBuilder()
        .startTime(LocalTime.of(7, 0))
        .endTime(LocalTime.of(7, 50))
        .build();

    exerciseCalendar.editSingleInstance(firstInstance, updated);

    assertEquals(LocalTime.of(7, 0),
        exerciseCalendar.getEvents().getFirst().getStartTime());

    for (int i = 1; i < exerciseCalendar.getEvents().size(); i++) {
      assertEquals(LocalTime.of(6, 0),
          exerciseCalendar.getEvents().get(i).getStartTime());
    }
  }

  @Test
  void editFutureInstancesOfRecurringEvent() {
    Event updatedTemplate = new Event.Builder("Pilates (Updated)",
        LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(7, 0))
        .endTime(LocalTime.of(7, 50))
        .build();

    LocalDate fromDate = LocalDate.of(2025, 11, 12); // Wednesday
    exerciseCalendar.editFutureInstances(pilatesSeriesId, fromDate, updatedTemplate);

    for (Event event : exerciseCalendar.getEvents()) {
      if (!event.getStartDate().isBefore(fromDate)) {
        assertEquals(LocalTime.of(7, 0), event.getStartTime());
      } else {
        assertEquals(LocalTime.of(6, 0), event.getStartTime());
      }
    }
  }

  @Test
  void editEntireRecurringEventSeries() {
    Event updatedEvent = new Event.Builder("Pilates (Updated)",
        LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(5, 30))
        .endTime(LocalTime.of(6, 20))
        .build();

    exerciseCalendar.editEntireSeries(pilatesSeriesId, updatedEvent);

    for (Event e : exerciseCalendar.getEvents()) {
      assertEquals(LocalTime.of(5, 30), e.getStartTime());
    }
  }

  @Test
  void headerRowIsCorrect() throws IOException {
    Event privateEvent = new Event.Builder("Meeting", nov15, nov15)
        .startTime(LocalTime.of(20, 0))
        .endTime(LocalTime.of(20, 30))
        .visibility(Visibility.PRIVATE)
        .build();
    calendar.addEvent(privateEvent);
    calendar.addEvent(meetingEvent);
    calendar.addEvent(conferenceAllDayEvent);
    calendar.exportToCsv(tempFile.toString());

    List<String> lines = Files.readAllLines(tempFile);
    assertEquals(
        "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private",
        lines.getFirst()
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"Meeting", "11/15/2025", "10:00 AM"})
  void meetingEventRowContainsExpectedValues(String expected) throws IOException {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(conferenceAllDayEvent);
    calendar.exportToCsv(tempFile.toString());

    List<String> lines = Files.readAllLines(tempFile);
    String meetingLine = lines.get(1);
    assertTrue(meetingLine.contains(expected));
  }

  @Test
  void getEventMatchesAllDayEventWhenTimeIsNull() {
    calendar.addEvent(conferenceAllDayEvent);
    Event found = calendar.getEvent("Conference", nov16, null);
    assertEquals(conferenceAllDayEvent, found);
  }

  @Test
  void getEventReturnsNullWhenSubjectDiffersByCaseAndTimeDoesNotMatch() {
    calendar.addEvent(meetingEvent);

    Event found = calendar.getEvent("DifferentSubject", nov15, LocalTime.of(9, 0));
    assertNull(found);
  }

  @Test
  void getEventMatchesCaseInsensitiveSubject() {
    calendar.addEvent(meetingEvent);

    Event found = calendar.getEvent("meeting", nov15, LocalTime.of(10, 0));
    assertEquals(meetingEvent, found);
  }

  @Test
  void getEventReturnsNullWhenAllDayEventQueriedWithTime() {
    calendar.addEvent(conferenceAllDayEvent);

    Event found = calendar.getEvent("Conference", nov16, LocalTime.of(10, 0));
    assertNull(found);
  }

  @Test
  void getEventReturnsNullWhenDateDoesNotMatch() {
    calendar.addEvent(meetingEvent);

    Event found = calendar.getEvent("Meeting", nov16, LocalTime.of(10, 0));
    assertNull(found);
  }

  @Test
  void recurringEventThrowsWhenBaseEventIsNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new RecurringEvent(null, List.of(DayOfWeek.MONDAY), 3, null);
    });
  }

  @Test
  void recurringEventThrowsWhenBaseEventSpansMultipleDays() {
    Event multiDayEvent = new Event.Builder("Retreat",
        LocalDate.of(2025, 11, 1), LocalDate.of(2025, 11, 2))
        .build();

    assertThrows(IllegalArgumentException.class, () -> {
      new RecurringEvent(multiDayEvent, List.of(DayOfWeek.SATURDAY), 3, null);
    });
  }

  @Test
  void recurringEventThrowsWhenNoDaysProvided() {
    Event singleDay = new Event.Builder("Yoga", nov15, nov15).build();

    assertThrows(IllegalArgumentException.class, () -> {
      new RecurringEvent(singleDay, List.of(), 3, null);
    });
  }

  @Test
  void recurringEventThrowsWhenBothOccurrencesAndUntilDateProvided() {
    Event singleDay = new Event.Builder("Yoga", nov15, nov15).build();

    assertThrows(IllegalArgumentException.class, () -> {
      new RecurringEvent(singleDay, List.of(DayOfWeek.SATURDAY), 3, nov16);
    });
  }

  @Test
  void recurringEventThrowsWhenNeitherOccurrencesNorUntilDateProvided() {
    Event singleDay = new Event.Builder("Yoga", nov15, nov15).build();

    assertThrows(IllegalArgumentException.class, () -> {
      new RecurringEvent(singleDay, List.of(DayOfWeek.SATURDAY), null, null);
    });
  }

  @Test
  void recurringEventThrowsWhenOccurrencesIsZero() {
    Event singleDay = new Event.Builder("Yoga", nov15, nov15).build();

    assertThrows(IllegalArgumentException.class, () -> {
      new RecurringEvent(singleDay, List.of(DayOfWeek.SATURDAY), 0, null);
    });
  }

  @Test
  void generateEventsWithOccurrencesGeneratesExpectedCount() {
    Event base = new Event.Builder("Yoga", LocalDate.of(2025, 11, 3),
        LocalDate.of(2025, 11, 3)).build();

    RecurringEvent recurring = new RecurringEvent(
        base,
        List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        3,
        null
    );

    List<Event> generated = recurring.generateEvents();

    assertEquals(3, generated.size());
    assertEquals(recurring.getSeriesId(), generated.getFirst().getSeriesId());
  }

  @Test
  void generateEventsWithUntilDateStopsCorrectly() {
    Event base = new Event.Builder("Class", LocalDate.of(2025, 11, 1),
        LocalDate.of(2025, 11, 1)).build();

    RecurringEvent recurring = new RecurringEvent(
        base,
        List.of(DayOfWeek.SATURDAY),
        null,
        LocalDate.of(2025, 11, 22)
    );

    List<Event> generated = recurring.generateEvents();

    assertEquals(4, generated.size());
    assertTrue(generated.stream().allMatch(e -> e.getSeriesId() != null));
  }

  @Test
  void editEventThrowsWhenEventNotFound() {
    calendar.addEvent(meetingEvent);

    Event notInCalendar = new Event.Builder("Not Added", nov16, nov16)
        .startTime(LocalTime.of(9, 0))
        .endTime(LocalTime.of(10, 0))
        .build();

    Event updated = notInCalendar.toBuilder().subject("Updated").build();

    assertThrows(IllegalArgumentException.class, () ->
        calendar.editEvent(notInCalendar, updated));
  }

  @Test
  void editSingleInstanceThrowsWithNullOriginalEvent() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editSingleInstance(null, meetingEvent);
    });
  }

  @Test
  void editSingleInstanceThrowsWithNullUpdatedEvent() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editSingleInstance(meetingEvent, null);
    });
  }

  @Test
  void editSingleInstanceThrowsWhenEventNotFound() {
    Event notInCalendar = new Event.Builder("Not Added", nov15, nov15).build();
    Event updated = new Event.Builder("Updated", nov15, nov15).build();

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editSingleInstance(notInCalendar, updated);
    });
  }

  @Test
  void editFutureInstancesThrowsWithNullSeriesId() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editFutureInstances(null, nov15, meetingEvent);
    });
  }

  @Test
  void editFutureInstancesThrowsWithNullFromDate() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editFutureInstances("series123", null, meetingEvent);
    });
  }

  @Test
  void editFutureInstancesThrowsWithNullUpdatedEvent() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editFutureInstances("series123", nov15, null);
    });
  }

  @Test
  void editFutureInstancesThrowsWhenNoEventsFound() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editFutureInstances("nonexistent-series", nov15, meetingEvent);
    });
  }

  @Test
  void editEntireSeriesThrowsWithNullSeriesId() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEntireSeries(null, meetingEvent);
    });
  }

  @Test
  void editEntireSeriesThrowsWithNullUpdatedEvent() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEntireSeries("series123", null);
    });
  }

  @Test
  void editEntireSeriesThrowsWhenSeriesNotFound() {
    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEntireSeries("nonexistent-series", meetingEvent);
    });
  }

  @Test
  void editEntireSeriesDetectsConflictWithOtherEvent() {
    Event conflictingEvent = new Event.Builder("Yoga", LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(5, 30))
        .endTime(LocalTime.of(6, 30))
        .build();
    calendar.addEvent(conflictingEvent);

    Event updatedEvent = new Event.Builder("Pilates (Updated)",
        LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(5, 45))
        .endTime(LocalTime.of(6, 15))
        .build();

    assertThrows(IllegalArgumentException.class, () ->
        calendar.editEntireSeries(pilatesSeriesId, updatedEvent));
  }

  @Test
  void editEntireSeriesTriggersConflictCheckWithOtherEvent() {
    Event yoga = new Event.Builder("Yoga", LocalDate.of(2025, 11, 9), LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(16, 0))
        .endTime(LocalTime.of(17, 0))
        .build();
    exerciseCalendar.addEvent(yoga);

    Event updated = new Event.Builder("Pilates (Updated)",
        LocalDate.of(2025, 11, 9), LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(16, 0))
        .endTime(LocalTime.of(16, 30))
        .build();

    assertThrows(IllegalArgumentException.class,
        () -> exerciseCalendar.editEntireSeries(pilatesSeriesId, updated));
  }

  @Test
  void editEntireSeriesAllowsConflictCheckWithOtherSeries() {
    Calendar personalCalendar = new Calendar("Personal", true);
    personalCalendar.addRecurringEvent(pilatesRecurringEvent);
    Event yoga = new Event.Builder("Yoga", LocalDate.of(2025, 11, 9), LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(16, 0))
        .endTime(LocalTime.of(17, 0))
        .build();
    personalCalendar.addEvent(yoga);

    Event updated = new Event.Builder("Pilates (Updated)",
        LocalDate.of(2025, 11, 9), LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(16, 0))
        .endTime(LocalTime.of(16, 30))
        .build();

    personalCalendar.editEntireSeries(pilatesSeriesId, updated);

    for (Event e : personalCalendar.getEvents()) {
      if (pilatesRecurringEvent.getSeriesId().equals(e.getSeriesId())) {
        assertEquals(LocalTime.of(16, 0), e.getStartTime());
      }
    }
  }


  @Test
  void addEventConflictsWithAllDayEvent() {
    calendar.addEvent(conferenceAllDayEvent);

    Event timedEvent = new Event.Builder("Timed Meeting", nov16, nov16)
        .startTime(LocalTime.of(14, 0))
        .endTime(LocalTime.of(15, 0))
        .build();

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(timedEvent);
    });
  }

  @Test
  void addEventAllDayConflictsWithAllDay() {
    calendar.addEvent(conferenceAllDayEvent);

    Event anotherAllDay = new Event.Builder("Another Conference", nov16, nov16).build();

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.addEvent(anotherAllDay);
    });
  }

  @Test
  void editSeriesCreatesConflictWithExistingEvent() {
    exerciseCalendar.addEvent(meetingEvent);

    Event conflictingUpdate = new Event.Builder("Updated Pilates", nov15, nov15)
        .startTime(LocalTime.of(10, 15))
        .endTime(LocalTime.of(10, 45))
        .build();

    assertThrows(IllegalArgumentException.class, () -> {
      calendar.editEntireSeries(pilatesRecurringEvent.getSeriesId(), conflictingUpdate);
    });
  }

  @Test
  void exportToCsvThrowsRuntimeExceptionOnIOError() {
    calendar.addEvent(meetingEvent);

    String invalidPath = "/nonexistent/directory/calendar.csv";

    assertThrows(RuntimeException.class, () -> {
      calendar.exportToCsv(invalidPath);
    });
  }

  @ParameterizedTest
  @CsvSource({
      "'Meeting, \"Important\"', '\"Meeting, \"\"Important\"\"\"'",
      "'Has \"quotes\" only', '\"Has \"\"quotes\"\" only\"'",
      "'PlainText', 'PlainText'"
  })
  void exportToCsvEscapesCommasAndQuotes(String input, String expectedFragment) throws IOException {
    Event event = new Event.Builder(input, nov15, nov15)
        .description(input)
        .location(input)
        .build();

    calendar.addEvent(event);
    calendar.exportToCsv(tempFile.toString());

    String dataLine = Files.readAllLines(tempFile).get(1);
    assertTrue(dataLine.contains(expectedFragment));
  }

  @Test
  void getEventsOnDateWithMultiDayEvent() {
    Event multiDayEvent = new Event.Builder("Conference", nov15, nov16).build();
    calendar.addEvent(multiDayEvent);

    assertTrue(calendar.getEventsOnDate(nov15).contains(multiDayEvent));
    assertTrue(calendar.getEventsOnDate(nov16).contains(multiDayEvent));
  }

  @Test
  void getEventsInDateRangeExcludesOutsideEvents() {
    Event beforeRange = new Event.Builder("Before", LocalDate.of(2025, 11, 10),
        LocalDate.of(2025, 11, 10)).build();
    Event afterRange = new Event.Builder("After", LocalDate.of(2025, 11, 25),
        LocalDate.of(2025, 11, 25)).build();

    calendar.addEvent(beforeRange);
    calendar.addEvent(afterRange);
    calendar.addEvent(meetingEvent);

    List<Event> inRange = calendar.getEventsInDateRange(nov15, nov16);

    assertTrue(inRange.contains(meetingEvent));
    assertFalse(inRange.contains(beforeRange));
    assertFalse(inRange.contains(afterRange));
  }

}
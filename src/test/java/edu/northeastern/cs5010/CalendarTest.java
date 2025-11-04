package edu.northeastern.cs5010;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

  @BeforeEach
  public void setUp() {
    calendar = new Calendar("Work Calendar");
    exerciseCalendar = new Calendar("Exercise Calendar");
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
    RecurringEvent pilatesRecurringEvent = new RecurringEvent(
        pilates,
        List.of(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY),
        4,
        null
    );
    exerciseCalendar.addRecurringEvent(pilatesRecurringEvent);
    pilatesSeriesId = pilatesRecurringEvent.getSeriesId();
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
  void editEntireRecurringSeries() {
    Event updatedTemplate = new Event.Builder("Pilates (Updated)",
        LocalDate.of(2025, 11, 9),
        LocalDate.of(2025, 11, 9))
        .startTime(LocalTime.of(5, 30))
        .endTime(LocalTime.of(6, 20))
        .build();

    exerciseCalendar.editEntireSeries(pilatesSeriesId, updatedTemplate);

    for (Event e : exerciseCalendar.getEvents()) {
      assertEquals(LocalTime.of(5, 30), e.getStartTime());
    }
  }

}
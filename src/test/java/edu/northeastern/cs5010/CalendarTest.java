package edu.northeastern.cs5010;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class CalendarTest {

  private Calendar calendar;
  private LocalDate nov15;
  private LocalDate nov16;
  private Event meetingEvent;
  private Event lunchEvent;
  private Event conferenceAllDayEvent;

  @BeforeEach
  public void setUp() {
    calendar = new Calendar("Work Calendar");
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
  }

  // ========== Constructor Tests ==========

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
    Event event = new Event.Builder("Meeting", nov15, nov15)
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event);

    assertEquals(1, calendar.getEvent().size());
  }

  @Test
  public void addSingleEventContainsEvent() {
    Event event = new Event.Builder("Meeting", nov15, nov15)
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event);

    assertTrue(calendar.getEvent().contains(event));
  }

  @Test
  public void addMultipleEventsResultsInCorrectSize() {
    calendar.addEvent(meetingEvent);
    calendar.addEvent(lunchEvent);
    calendar.addEvent(conferenceAllDayEvent);

    assertEquals(3, calendar.getEvent().size());
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
    Event event = new Event.Builder("Mom's Birthday", nov15, nov15).build();
    calendar.addEvent(event);

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
    Event event1 = new Event.Builder("Meeting", nov15, nov15).build();
    Event event2 = new Event.Builder("Conference", nov16, nov16).build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertTrue(calendar.getEventsInDateRange(nov15, nov16).contains(event1));
  }

  @Test
  public void getEventsInDateRangeIncludesEndDate() {
    Event event1 = new Event.Builder("Meeting", nov15, nov15).build();
    Event event2 = new Event.Builder("Conference", nov16, nov16).build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    assertTrue(calendar.getEventsInDateRange(nov15, nov16).contains(event2));
  }

  @ParameterizedTest
  @CsvSource({
      "2025-11-15, 10:00, true",   // During event
      "2025-11-15, 10:30, true",   // Middle of event
      "2025-11-15, 10:59, true",   // End of event
      "2025-11-15, 14:00, false",  // After event
      "2025-11-16, 10:30, false"   // Different date
  })
  public void isUserBusy(String dateStr, String timeStr, boolean expected) {
    Event event = new Event.Builder("Meeting", nov15, nov15)
        .startTime(LocalTime.of(10, 0))
        .endTime(LocalTime.of(11, 0))
        .build();

    calendar.addEvent(event);

    assertEquals(expected, calendar.isUserBusy(
        LocalDate.parse(dateStr),
        LocalTime.parse(timeStr)));
  }
}
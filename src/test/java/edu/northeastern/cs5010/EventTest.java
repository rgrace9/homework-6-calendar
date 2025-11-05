package edu.northeastern.cs5010;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.northeastern.cs5010.Event.Visibility;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventTest {

  private LocalDate today;
  private LocalDate tomorrow;
  private LocalTime morning;
  private LocalTime afternoon;
  private LocalTime evening;

  @BeforeEach
  void setUp() {
    today = LocalDate.of(2025, 11, 1);
    tomorrow = LocalDate.of(2025, 11, 2);
    morning = LocalTime.of(9, 0);
    afternoon = LocalTime.of(14, 0);
    evening = LocalTime.of(18, 0);
  }

  @Test
  void hasSubject() {
    Event event = new Event.Builder("Meeting", today, today).build();

    assertEquals("Meeting", event.getSubject());
  }

  @Test
  void hasStartDate() {
    Event event = new Event.Builder("Meeting", today, today).build();

    assertEquals(today, event.getStartDate());
  }

  @Test
  void hasEndDate() {
    Event event = new Event.Builder("Meeting", today, today).build();

    assertEquals(today, event.getEndDate());
  }

  @Test
  void cannotBeCreatedWithoutSubject() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder(null, today, today).build();
    });
  }

  @Test
  void cannotBeCreatedWithEmptySubject() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("", today, today).build();
    });
  }

  @Test
  void cannotBeCreatedWithoutStartDate() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", null, today).build();
    });
  }

  @Test
  void cannotBeCreatedWithoutEndDate() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", today, null).build();
    });
  }

  @Test
  void endTimeCannotBeBeforeStartTime() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", today, today)
          .startTime(afternoon)
          .endTime(morning)
          .build();
    });
  }

  @Test
  void endTimeCanBeAfterStartTime() {
    Event event = new Event.Builder("Meeting", today, today)
        .startTime(morning)
        .endTime(afternoon)
        .build();

    assertEquals(morning, event.getStartTime());
  }

  @Test
  void endTimeCanBeBeforeStartTimeOnDifferentDays() {
    Event event = new Event.Builder("Meeting", today, tomorrow)
        .startTime(afternoon)
        .endTime(morning)
        .build();

    assertEquals(morning, event.getEndTime());
  }

  @Test
  void eventWithStartTimeIsNotAllDayEvent() {
    Event event = new Event.Builder("Conference", today, today)
        .startTime(afternoon)
        .endTime(evening)
        .build();

    assertFalse(event.isAllDayEvent());
  }

  @Test
  void eventWithStartTimeMustHaveEndTime() {
    Event event = new Event.Builder("Conference", today, today)
        .startTime(afternoon)
        .endTime(afternoon)
        .build();

    assertFalse(event.isAllDayEvent());
  }

  @Test
  void eventWithoutStartTimeIsAllDayEvent() {
    Event event = new Event.Builder("Conference", today, today).build();

    assertTrue(event.isAllDayEvent());
  }

  @Test
  void eventWithoutStartTimeHasNullStartTime() {
    Event event = new Event.Builder("Conference", today, today).build();

    assertNull(event.getStartTime());
  }

  @Test
  void eventWithoutStartTimeCannotHaveEndTime() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder(null, today, today).endTime(morning).build();
    });
  }

  @Test
  public void eventWithoutStartTimeButWithEndTimeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Invalid Event", tomorrow, tomorrow)
          .endTime(LocalTime.of(10, 0))
          .build();
    });
  }

  @Test
  public void eventWithStartTimeButWithoutEndTimeThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Invalid Event", today, today)
          .startTime(LocalTime.of(10, 0))
          .build();
    });
  }


  @Test
  void canSpanMultipleDaysHasCorrectStartDate() {
    LocalDate nextWeek = today.plusDays(7);
    Event event = new Event.Builder("Vacation", today, nextWeek).build();

    assertEquals(today, event.getStartDate());
  }

  @Test
  void canSpanMultipleDaysHasCorrectEndDate() {
    LocalDate nextWeek = today.plusDays(7);
    Event event = new Event.Builder("Vacation", today, nextWeek).build();

    assertEquals(nextWeek, event.getEndDate());
  }

  @Test
  void multiDayEventWithTimesHasCorrectStartDate() {
    Event event = new Event.Builder("Conference", today, tomorrow)
        .startTime(morning)
        .endTime(evening)
        .build();

    assertEquals(today, event.getStartDate());
  }

  @Test
  void multiDayEventWithTimesHasCorrectEndDate() {
    Event event = new Event.Builder("Conference", today, tomorrow)
        .startTime(morning)
        .endTime(evening)
        .build();

    assertEquals(tomorrow, event.getEndDate());
  }

  @Test
  void multiDayEventWithTimesHasCorrectStartTime() {
    Event event = new Event.Builder("Conference", today, tomorrow)
        .startTime(morning)
        .endTime(evening)
        .build();

    assertEquals(morning, event.getStartTime());
  }

  @Test
  void multiDayEventWithTimesHasCorrectEndTime() {
    Event event = new Event.Builder("Conference", today, tomorrow)
        .startTime(morning)
        .endTime(evening)
        .build();

    assertEquals(evening, event.getEndTime());
  }

  @Test
  void endDateCannotBeBeforeStartDate() {
    LocalDate yesterday = today.minusDays(1);
    assertThrows(IllegalArgumentException.class, () -> {
      new Event.Builder("Meeting", today, yesterday).build();
    });
  }

  @Test
  void canHaveDescription() {
    Event event = new Event.Builder("Meeting", today, today)
        .description("Discuss project requirements")
        .build();

    assertEquals("Discuss project requirements", event.getDescription());
  }

  @Test
  void canHaveLocation() {
    Event event = new Event.Builder("Meeting", today, today)
        .location("Conference Room A")
        .build();

    assertEquals("Conference Room A", event.getLocation());
  }

  @Test
  void canHaveVisibility() {
    Event event = new Event.Builder("Meeting", today, today)
        .visibility(Visibility.PRIVATE)
        .build();

    assertEquals(Visibility.PRIVATE, event.getVisibility());
  }

  @Test
  void eventDefaultVisibilityIsPublic() {
    Event event = new Event.Builder("Meeting", today, today).build();

    assertEquals(Visibility.PUBLIC, event.getVisibility());
  }

  @Test
  public void editDescription() {
    Event original = new Event.Builder("Meeting", today, tomorrow)
        .description("Team sync")
        .build();

    Event updated = original.toBuilder()
        .description("Updated team sync")
        .build();

    assertEquals("Updated team sync", updated.getDescription());
  }

  @Test
  public void editSubject() {
    Event original = new Event.Builder("Meeting", today, today).build();

    Event updated = original.toBuilder().subject("Interview").build();

    assertEquals("Interview", updated.getSubject());
  }

  @Test
  public void editStartDate() {
    Event original = new Event.Builder("Meeting", today, tomorrow).build();

    Event updated = original.toBuilder().startDate(tomorrow).build();

    assertEquals(tomorrow, updated.getStartDate());
  }

  @Test
  public void editEndDate() {
    Event original = new Event.Builder("Meeting", today, today).build();

    Event updated = original.toBuilder().endDate(tomorrow).build();

    assertEquals(tomorrow, updated.getEndDate());
  }

  @Test
  public void eventHasSeriesIdWhenRecurring() {
    Event meeting = new Event.Builder("Meeting", today, today).build();
    RecurringEvent meetingRecurring = new RecurringEvent(
        meeting,
        List.of(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY),
        4,
        null
    );

    assertNotNull(meetingRecurring.getSeriesId());
  }

  @Test
  void builderSetsSeriesIdCorrectly() {
    String testSeriesId = "1234567890";

    Event event = new Event.Builder(
        "Yoga",
        LocalDate.of(2025, 11, 14),
        LocalDate.of(2025, 11, 14))
        .startTime(java.time.LocalTime.of(9, 0))
        .endTime(java.time.LocalTime.of(10, 0))
        .seriesId(testSeriesId)
        .build();

    assertEquals(testSeriesId, event.getSeriesId());
  }
}
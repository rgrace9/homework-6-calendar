package edu.northeastern.cs5010;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.CalendarListener;
import edu.northeastern.cs5010.model.Event;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarListenerTest {

  private Calendar calendar;
  private Event meeting;
  private final LocalDate today = LocalDate.of(2025, 11, 11);

  static class TestListener implements CalendarListener {

    boolean eventIsAdded = false;
    boolean eventIsReplaced = false;

    @Override
    public void onEventAdded(Event event) {
      eventIsAdded = true;
    }

    @Override
    public void onEventReplaced(Event event) {
      eventIsReplaced = true;
    }
  }

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Work");
    meeting = new Event.Builder("Meeting", today, today).build();
  }

  @Test
  void addEventNotifiesListener() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);

    calendar.addEvent(meeting);

    assertTrue(listener.eventIsAdded);
  }

  @Test
  void replaceEventNotifiesListener() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);

    Event original = new Event.Builder("Meeting", today, today).build();
    Event updated = new Event.Builder("Meeting", today, today)
        .description("Updated").build();

    calendar.addEvent(original);
    calendar.editSingleInstance(original, updated);

    assertTrue(listener.eventIsReplaced);
  }

  @Test
  void allRegisteredListenersReceiveNotification() {
    TestListener firstListener = new TestListener();
    TestListener secondListener = new TestListener();
    calendar.addCalendarListener(firstListener);
    calendar.addCalendarListener(secondListener);

    calendar.addEvent(meeting);

    assertTrue(firstListener.eventIsAdded && secondListener.eventIsAdded);
  }

  @Test
  void removedListenerDoesNotReceiveAddedNotification() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);
    calendar.removeCalendarListener(listener);

    calendar.addEvent(meeting);

    assertFalse(listener.eventIsAdded);
  }

  @Test
  void removedListenerDoesNotReceiveReplacedNotification() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);
    calendar.removeCalendarListener(listener);

    Event original = new Event.Builder("Meeting", today, today).build();
    Event updated = new Event.Builder("Meeting", today, today)
        .description("Updated").build();

    calendar.addEvent(original);
    calendar.editSingleInstance(original, updated);

    assertFalse(listener.eventIsReplaced);
  }
}


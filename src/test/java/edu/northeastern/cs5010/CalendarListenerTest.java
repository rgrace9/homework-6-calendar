package edu.northeastern.cs5010;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
  private final LocalDate TODAY = LocalDate.of(2025, 11, 11);

  private TestListener registerListener() {
    TestListener listener = new TestListener();
    calendar.addCalendarListener(listener);
    return listener;
  }

  static class TestListener implements CalendarListener {

    boolean eventAdded = false;
    boolean eventReplaced = false;

    @Override
    public void onEventAdded(Event event) {
      eventAdded = true;
    }

    @Override
    public void onEventReplaced(Event event) {
      eventReplaced = true;
    }
  }

  @BeforeEach
  void setUp() {
    calendar = new Calendar("Work");
    meeting = new Event.Builder("Meeting", TODAY, TODAY).build();
  }

  @Test
  void addEventNotifiesListener() {
    TestListener listener = registerListener();

    calendar.addEvent(meeting);

    assertTrue(listener.eventAdded);
  }

  @Test
  void replaceEventNotifiesListener() {
    TestListener listener = registerListener();

    Event original = new Event.Builder("Meeting", TODAY, TODAY).build();
    Event updated = new Event.Builder("Meeting", TODAY, TODAY)
        .description("Updated").build();

    calendar.addEvent(original);
    calendar.editSingleInstance(original, updated);

    assertTrue(listener.eventReplaced);
  }

  @Test
  void allRegisteredListenersReceiveNotification() {
    TestListener firstListener = registerListener();
    TestListener secondListener = registerListener();

    calendar.addEvent(meeting);

    assertTrue(firstListener.eventAdded && secondListener.eventAdded);
  }

  @Test
  void removedListenerDoesNotReceiveAddedNotification() {
    TestListener listener = registerListener();
    calendar.removeCalendarListener(listener);

    calendar.addEvent(meeting);

    assertFalse(listener.eventAdded);
  }

  @Test
  void removedListenerDoesNotReceiveReplacedNotification() {
    TestListener listener = registerListener();
    calendar.removeCalendarListener(listener);

    Event original = new Event.Builder("Meeting", TODAY, TODAY).build();
    Event updated = new Event.Builder("Meeting", TODAY, TODAY)
        .description("Updated").build();

    calendar.addEvent(original);
    calendar.editSingleInstance(original, updated);

    assertFalse(listener.eventReplaced);
  }

  @Test
  void addingNullEventListenerThrowsException() {
    assertThrows(NullPointerException.class, () -> calendar.addCalendarListener(null));
  }

  @Test
  void removingNullEventListenerThrowsException() {
    assertThrows(NullPointerException.class, () -> calendar.removeCalendarListener(null));
  }
}

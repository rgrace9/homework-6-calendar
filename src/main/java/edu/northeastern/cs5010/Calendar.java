package edu.northeastern.cs5010;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Calendar {

  private final String title;
  private boolean allowConflicts = false;
  private final List<Event> events = new ArrayList<>();

  public Calendar(String title) {
    this(title, false);
  }

  public Calendar(String title, boolean allowConflicts) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar title cannot be null or empty");
    }
    this.title = title;
    this.allowConflicts = allowConflicts;
  }

  public String getTitle() {
    return title;
  }

  public List<Event> getEvents() {
    return new ArrayList<>(events);
  }

  public void addEvent(Event newEvent) {
    for (Event existing : events) {
      checkEventConflicts(newEvent, existing, "Event conflicts with an existing event");
    }

    events.add(newEvent);
  }

  public Event getEvent(String subject, LocalDate date, LocalTime time) {
    for (Event event : events) {
      boolean sameSubject = event.getSubject().equalsIgnoreCase(subject);
      boolean sameDate = event.getStartDate().equals(date);
      boolean sameTime = (event.getStartTime() == null && time == null)
          || (event.getStartTime() != null && event.getStartTime().equals(time));

      if (sameSubject && sameDate && sameTime) {
        return event;
      }
    }
    return null;
  }

  public List<Event> getEventsOnDate(LocalDate date) {
    List<Event> result = new ArrayList<>();
    for (Event event : events) {
      if ((date.isEqual(event.getStartDate()) || date.isEqual(event.getEndDate())) ||
          (date.isAfter(event.getStartDate()) && date.isBefore(event.getEndDate()))) {
        result.add(event);
      }
    }
    return result;
  }

  public List<Event> getEventsInDateRange(LocalDate start, LocalDate end) {
    List<Event> result = new ArrayList<>();
    for (Event event : events) {
      if (!(event.getEndDate().isBefore(start) || event.getStartDate().isAfter(end))) {
        result.add(event);
      }
    }
    return result;
  }

  public boolean isUserBusy(LocalDate date, LocalTime time) {
    for (Event event : events) {
      boolean sameDay = date.isEqual(event.getStartDate()) ||
          (date.isAfter(event.getStartDate()) && date.isBefore(event.getEndDate())) ||
          date.isEqual(event.getEndDate());

      if (!sameDay) {
        continue;
      }

      if (event.isAllDayEvent()) {
        return true;
      }

      LocalTime startTime = event.getStartTime();
      LocalTime endTime = event.getEndTime();

      boolean withinTime = startTime != null && endTime != null && !time.isBefore(startTime) &&
          !time.isAfter(endTime);
      if (withinTime) {
        return true;
      }
    }
    return false;
  }

  public void editEvent(Event original, Event updated) {
    for (Event event : events) {
      if (event == original) {
        continue;
      }

      checkEventConflicts(
          updated,
          event,
          "Edited event conflicts with another event"
      );
    }

    int index = events.indexOf(original);
    if (index >= 0) {
      events.set(index, updated);
    } else {
      throw new IllegalArgumentException("Event to edit not found in calendar");
    }
  }

  public void addRecurringEvent(RecurringEvent recurringEvent) {
    List<Event> generatedEventsList = recurringEvent.generateEvents();
    for (Event newEvent : generatedEventsList) {
      for (Event existingEvent : events) {
        checkEventConflicts(
            newEvent,
            existingEvent,
            "Recurring event cannot be created because at least one instance conflicts with an existing event"
        );
      }
    }

    for (Event newEvent : generatedEventsList) {
      addEvent(newEvent);
    }
  }

  public void editSingleInstance(Event originalEvent, Event updatedEvent) {
    if (originalEvent == null || updatedEvent == null) {
      throw new IllegalArgumentException("Events cannot be null");
    }

    int eventIndex = events.indexOf(originalEvent);
    if (eventIndex < 0) {
      throw new IllegalArgumentException("Event not found in calendar");
    }

    for (Event existingEvent : events) {
      if (existingEvent == originalEvent) {
        continue;
      }
      checkEventConflicts(
          updatedEvent,
          existingEvent,
          "Updated event conflicts with an existing event"
      );
    }
    events.set(eventIndex, updatedEvent);
  }

  public void editFutureInstances(String seriesId, LocalDate fromDate, Event updatedEvent) {
    if (seriesId == null || fromDate == null || updatedEvent == null) {
      throw new IllegalArgumentException("Arguments cannot be null");
    }

    List<Event> futureEvents = new ArrayList<>();
    for (Event e : events) {
      if (seriesId.equals(e.getSeriesId()) && !e.getStartDate().isBefore(fromDate)) {
        futureEvents.add(e);
      }
    }
    if (futureEvents.isEmpty()) {
      throw new IllegalArgumentException("No future events found for this series");
    }

    checkSeriesConflicts(seriesId, futureEvents, updatedEvent);

    for (int i = 0; i < events.size(); i++) {
      Event e = events.get(i);
      if (seriesId.equals(e.getSeriesId()) && !e.getStartDate().isBefore(fromDate)) {
        events.set(i, updatedEvent.toBuilder()
            .startDate(e.getStartDate())
            .endDate(e.getEndDate())
            .seriesId(seriesId)
            .build());
      }
    }


  }

  public void editEntireSeries(String seriesId, Event updatedEvent) {
    if (seriesId == null || updatedEvent == null) {
      throw new IllegalArgumentException("There must be a seriesId and updated event");
    }

    List<Event> seriesEvents = new ArrayList<>();
    for (Event e : events) {
      if (seriesId.equals(e.getSeriesId())) {
        seriesEvents.add(e);
      }
    }
    if (seriesEvents.isEmpty()) {
      throw new IllegalArgumentException("Series not found in calendar");
    }

    checkSeriesConflicts(seriesId, seriesEvents, updatedEvent);

    for (int i = 0; i < events.size(); i++) {
      Event e = events.get(i);
      if (seriesId.equals(e.getSeriesId())) {
        events.set(i, updatedEvent.toBuilder()
            .startDate(e.getStartDate())
            .endDate(e.getEndDate())
            .seriesId(seriesId)
            .build());
      }
    }
  }

  private boolean eventsOverlap(Event existingEvent, Event newEvent) {
    boolean overlappingDates = !(existingEvent.getEndDate().isBefore(newEvent.getStartDate()) ||
        existingEvent.getStartDate().isAfter(newEvent.getEndDate()));
    if (!overlappingDates) {
      return false;
    }

    if (existingEvent.isAllDayEvent() || newEvent.isAllDayEvent()) {
      return true;
    }

    return !existingEvent.getEndTime().isBefore(newEvent.getStartTime()) &&
        !existingEvent.getStartTime().isAfter(newEvent.getEndTime());
  }

  private void checkEventDuplication(Event existingEvent, Event newEvent) {
    boolean sameSubject = existingEvent.getSubject().equalsIgnoreCase(newEvent.getSubject());
    boolean sameDate = existingEvent.getStartDate().equals(newEvent.getStartDate());
    boolean sameTime = (existingEvent.getStartTime() == null && newEvent.getStartTime() == null)
        || (existingEvent.getStartTime() != null && existingEvent.getStartTime()
        .equals(newEvent.getStartTime()));

    if (sameSubject && sameDate && sameTime) {
      throw new IllegalArgumentException("Duplicate event not allowed");
    }
  }

  private void checkEventConflicts(Event newEvent, Event existingEvent, String errorMessage) {
    checkEventDuplication(existingEvent, newEvent);

    if (!allowConflicts && eventsOverlap(existingEvent, newEvent)) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  private void checkSeriesConflicts(String id, List<Event> eventsToUpdate, Event updatedEvent) {
    for (Event event : eventsToUpdate) {
      Event updatedCopy = updatedEvent.toBuilder()
          .startDate(event.getStartDate())
          .endDate(event.getEndDate())
          .seriesId(id)
          .build();

      for (Event other : events) {
        if (!id.equals(other.getSeriesId())) {
          checkEventConflicts(updatedCopy, other, "Update would cause a conflict");
        }
      }
    }
  }

}

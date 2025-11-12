package edu.northeastern.cs5010.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a calendar that stores and manages events. Supports single and recurring events and
 * CSV export. Detects events that conflict.
 */
public class Calendar {

  private final String title;
  private boolean allowConflicts = false;
  private final List<Event> events = new ArrayList<>();

  private final List<CalendarListener> listeners = new ArrayList<>();

  /**
   * Creates a calendar with the given title and no event conflicts allowed.
   *
   * @param title the calendar title (non-empty)
   * @throws IllegalArgumentException if the title is null or empty
   */
  public Calendar(String title) {
    this(title, false);
  }

  /**
   * Creates a calendar with the given title and optional conflict allowance.
   *
   * @param title          the calendar title (non-empty)
   * @param allowConflicts true if overlapping events are permitted, false otherwise
   * @throws IllegalArgumentException if the title is null or empty
   */
  public Calendar(String title, boolean allowConflicts) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar title cannot be null or empty");
    }
    this.title = title;
    this.allowConflicts = allowConflicts;
  }

  /**
   * Returns the calendar title.
   *
   * @return the calendar title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns a copy of all events in the calendar.
   *
   * @return a list of events
   */
  public List<Event> getEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Adds a new event to the calendar.
   *
   * @param newEvent the event to add
   * @throws IllegalArgumentException if the event conflicts or duplicates another
   */
  public void addEvent(Event newEvent) {
    for (Event existing : events) {
      checkEventConflicts(newEvent, existing, "Event conflicts with an existing event");
    }

    events.add(newEvent);
    announceEventAdded(newEvent);
  }

  /**
   * Finds an event matching the given subject, date, and optional time.
   *
   * @param subject the event subject (case-insensitive)
   * @param date    the event date
   * @param time    the event start time (nullable)
   * @return the matching event, or {@code null} if not found
   */
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

  /**
   * Returns all events occurring on the specific date.
   *
   * @param date the date to search
   * @return a list of events on that date
   */
  public List<Event> getEventsOnDate(LocalDate date) {
    List<Event> result = new ArrayList<>();
    for (Event event : events) {
      if ((date.isEqual(event.getStartDate()) || date.isEqual(event.getEndDate()))
          || (date.isAfter(event.getStartDate()) && date.isBefore(event.getEndDate()))) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Returns all events within a date range.
   *
   * @param start the start date (inclusive)
   * @param end   the end date (inclusive)
   * @return a list of events in the range
   */
  public List<Event> getEventsInDateRange(LocalDate start, LocalDate end) {
    List<Event> result = new ArrayList<>();
    for (Event event : events) {
      if (!(event.getEndDate().isBefore(start) || event.getStartDate().isAfter(end))) {
        result.add(event);
      }
    }
    return result;
  }

  /**
   * Checks if the calendar has an event on the specific date and time.
   *
   * @param date the date to check
   * @param time the time to check
   * @return true if the user is busy, false if not
   */
  public boolean isUserBusy(LocalDate date, LocalTime time) {
    for (Event event : events) {
      boolean sameDay = date.isEqual(event.getStartDate())
          || (date.isAfter(event.getStartDate()) && date.isBefore(event.getEndDate()))
          || date.isEqual(event.getEndDate());

      if (!sameDay) {
        continue;
      }

      if (event.isAllDayEvent()) {
        return true;
      }

      LocalTime startTime = event.getStartTime();
      LocalTime endTime = event.getEndTime();

      boolean withinTime = startTime != null && endTime != null && !time.isBefore(startTime)
          && !time.isAfter(endTime);
      if (withinTime) {
        return true;
      }
    }
    return false;
  }

  /**
   * Edits an existing event.
   *
   * @param original the original event
   * @param updated  the updated event
   * @throws IllegalArgumentException if not found or conflicts occur
   */
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

  /**
   * Adds a recurring event and all generated instances to the calendar.
   *
   * @param recurringEvent the recurring event to add
   * @throws IllegalArgumentException if any instance conflicts with existing events
   */
  public void addRecurringEvent(RecurringEvent recurringEvent) {
    List<Event> generatedEventsList = recurringEvent.generateEvents();
    for (Event newEvent : generatedEventsList) {
      for (Event existingEvent : events) {
        checkEventConflicts(
            newEvent,
            existingEvent,
            "Recurring event cannot be created because at least one instance "
                + "conflicts with an existing event"
        );
      }
    }

    for (Event newEvent : generatedEventsList) {
      addEvent(newEvent);
    }
  }

  /**
   * Updates a single instance of a recurring event.
   *
   * @param originalEvent the event to update
   * @param updatedEvent  the new event details
   * @throws IllegalArgumentException if missing arguments, event not found or conflicts occur
   */
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
    announceEventReplaced(updatedEvent);
  }

  /**
   * Updates all future instances of a recurring event from a specific date onward.
   *
   * @param seriesId     the series ID
   * @param fromDate     the date to begin updates
   * @param updatedEvent the template for updated instances
   * @throws IllegalArgumentException if no events found or conflicts occur
   */
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
        announceEventReplaced(updatedEvent);
      }
    }
  }

  /**
   * Updates all events in a recurring series.
   *
   * @param seriesId     the series ID
   * @param updatedEvent the new event details for the entire series
   * @throws IllegalArgumentException if the series is not found or conflicts occur
   */
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
        announceEventReplaced(updatedEvent);
      }
    }
  }

  private boolean eventsOverlap(Event existingEvent, Event newEvent) {
    boolean overlappingDates = !(existingEvent.getEndDate().isBefore(newEvent.getStartDate())
        || existingEvent.getStartDate().isAfter(newEvent.getEndDate()));
    if (!overlappingDates) {
      return false;
    }

    if (existingEvent.isAllDayEvent() || newEvent.isAllDayEvent()) {
      return true;
    }

    return !existingEvent.getEndTime().isBefore(newEvent.getStartTime())
        && !existingEvent.getStartTime().isAfter(newEvent.getEndTime());
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

  /**
   * Exports the calendar to a CSV file in Google Calendar format.
   *
   * @param filePath the destination file path
   * @throws RuntimeException if export fails
   */
  public void exportToCsv(String filePath) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    try (PrintWriter writer = new PrintWriter(filePath)) {
      writer.println(
          "Subject,Start Date,Start Time,End Date,End Time,"
              + "All Day Event,Description,Location,Private");
      for (Event event : events) {
        boolean isAllDay = event.isAllDayEvent();

        String subject = event.getSubject();
        String startDate = event.getStartDate().format(dateFormatter);
        String endDate = event.getEndDate().format(dateFormatter);
        String startTime = isAllDay ? "" : event.getStartTime().format(timeFormatter);
        String endTime = isAllDay ? "" : event.getEndTime().format(timeFormatter);
        String description = event.getDescription() == null ? "" : event.getDescription();
        String location = event.getLocation() == null ? "" : event.getLocation();
        String isPrivate = event.getVisibility() == Event.Visibility.PRIVATE ? "True" : "False";

        writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
            escapeCsv(subject),
            startDate,
            startTime,
            endDate,
            endTime,
            isAllDay ? "True" : "False",
            escapeCsv(description),
            escapeCsv(location),
            isPrivate
        );
      }

      System.out.println("Calendar exported to " + filePath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to export calendar: " + e.getMessage(), e);
    }
  }

  /**
   * Imports events from a CSV file in Google Calendar format.
   *
   * @param filePath the source file path
   * @throws RuntimeException error if import fails
   */
  public void importFromCsv(String filePath) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String headerLine = reader.readLine(); // Skip header
      if (headerLine == null) {
        throw new RuntimeException("CSV file is empty");
      }

      String line;
      int lineNumber = 1;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        try {
          List<String> fields = parseCsv(line);

          if (fields.size() != 9) {
            throw new RuntimeException("Invalid CSV format at line " + lineNumber
                + ": expected 9 fields, got " + fields.size());
          }

          String subject = fields.get(0);
          String startDateStr = fields.get(1);
          String startTimeStr = fields.get(2);
          String endDateStr = fields.get(3);
          String endTimeStr = fields.get(4);
          String allDayStr = fields.get(5);
          String description = fields.get(6);
          String location = fields.get(7);
          final String isPrivateStr = fields.get(8);

          LocalDate startDate = LocalDate.parse(startDateStr, dateFormatter);
          LocalDate endDate = LocalDate.parse(endDateStr, dateFormatter);

          Event.Builder builder = new Event.Builder(subject, startDate, endDate);

          boolean isAllDay = allDayStr.equalsIgnoreCase("True");
          if (!isAllDay && !startTimeStr.isEmpty() && !endTimeStr.isEmpty()) {
            LocalTime startTime = LocalTime.parse(startTimeStr, timeFormatter);
            LocalTime endTime = LocalTime.parse(endTimeStr, timeFormatter);
            builder.startTime(startTime).endTime(endTime);
          }

          if (!description.isEmpty()) {
            builder.description(description);
          }

          if (!location.isEmpty()) {
            builder.location(location);
          }

          if (isPrivateStr.equalsIgnoreCase("True")) {
            builder.visibility(Event.Visibility.PRIVATE);
          } else {
            builder.visibility(Event.Visibility.PUBLIC);
          }

          Event event = builder.build();
          addEvent(event);

        } catch (Exception e) {
          throw new RuntimeException("Error parsing line " + lineNumber + ": "
              + e.getMessage(), e);
        }
      }

      System.out.println("Calendar imported from " + filePath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to import calendar: " + e.getMessage(), e);
    }
  }

  private String escapeCsv(String text) {
    if (text.contains(",") || text.contains("\"")) {
      return "\"" + text.replace("\"", "\"\"") + "\"";
    }
    return text;
  }

  private List<String> parseCsv(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder currentField = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          // Escaped quote - add one quote and skip next
          currentField.append('"');
          i++;
        } else {
          // Toggle quote mode
          inQuotes = !inQuotes;
        }
      } else if (c == ',' && !inQuotes) {
        // End of field
        fields.add(currentField.toString());
        currentField = new StringBuilder();
      } else {
        currentField.append(c);
      }
    }

    fields.add(currentField.toString());

    return fields;
  }

  /**
   * Registers a listener to receive event notifications for the calendar.
   *
   * @param listener the listener to add
   */
  public void addCalendarListener(CalendarListener listener) {
    Objects.requireNonNull(listener);
    listeners.add(listener);
  }

  /**
   * Unregisters a listener so it no longer receives event notifications.
   *
   * @param listener the listener to remove
   */
  public void removeCalendarListener(CalendarListener listener) {
    Objects.requireNonNull(listener);
    listeners.remove(listener);
  }

  /**
   * Notifies all registered listeners that an event was added.
   *
   * @param event the event that was added
   */
  private void announceEventAdded(Event event) {
    for (CalendarListener listener : new ArrayList<>(listeners)) {
      listener.onEventAdded(event);
    }
  }

  /**
   * Notifies all registered listeners that an event was replaced.
   *
   * @param event the event that was replaced
   */
  private void announceEventReplaced(Event event) {
    for (CalendarListener listener : new ArrayList<>(listeners)) {
      listener.onEventReplaced(event);
    }
  }
}

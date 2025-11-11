package edu.northeastern.cs5010.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a recurring event based on a base {@link Event}. A recurring event can repeat on
 * specified days of the week for either a given number of occurrences or until a specific end date.
 * Each generated event shares a common seriesId to identify which series it belongs to.
 */
public class RecurringEvent {

  private final Event baseEvent;
  private final List<DayOfWeek> daysOfWeek;
  private final Integer occurrences;
  private final LocalDate untilDate;
  private final String seriesId;

  /**
   * Creates a recurring event.
   *
   * @param baseEvent   the event (must start and end on the same day)
   * @param daysOfWeek  the days of the week on which the event repeats
   * @param occurrences the number of occurrences
   * @param untilDate   the end date for the recurrence
   * @throws IllegalArgumentException if validation fails
   */
  public RecurringEvent(Event baseEvent, List<DayOfWeek> daysOfWeek, Integer occurrences,
      LocalDate untilDate) {
    if (baseEvent == null) {
      throw new IllegalArgumentException("Base event cannot be null");
    }

    if (!baseEvent.getStartDate().isEqual(baseEvent.getEndDate())) {
      throw new IllegalArgumentException("Base event cannot span more than one day");
    }

    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("At least one day of the week must be specified");
    }

    boolean hasOccurrences = occurrences != null;
    boolean hasSpecificEndDate = untilDate != null;

    if (hasOccurrences == hasSpecificEndDate) {
      throw new IllegalArgumentException(
          "You must specify EITHER the number of occurrences OR the end date");
    }

    if (hasOccurrences && occurrences < 1) {
      throw new IllegalArgumentException("Occurrences must be greater than zero");
    }

    this.baseEvent = baseEvent;
    this.daysOfWeek = new ArrayList<>(daysOfWeek);
    this.occurrences = occurrences;
    this.untilDate = untilDate;
    this.seriesId = UUID.randomUUID().toString();

  }

  /**
   * Returns the unique identifier for this recurring series.
   *
   * @return the recurring series ID
   */
  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Generates all individual {@link Event} instances for this recurring series based on the days of
   * the week and either the number of occurrences or the untilDate.
   *
   * @return a list of generated events in the recurring events series
   */
  public List<Event> generateEvents() {
    List<Event> generatedEventsList = new ArrayList<>();

    LocalDate eventDate = baseEvent.getStartDate();
    int createdEvents = 0;

    if (occurrences != null) {
      while (createdEvents < occurrences) {
        if (daysOfWeek.contains(eventDate.getDayOfWeek())) {
          Event recurringEvent = baseEvent.toBuilder()
              .startDate(eventDate)
              .endDate(eventDate)
              .seriesId(seriesId)
              .build();
          generatedEventsList.add(recurringEvent);
          createdEvents++;
        }

        eventDate = eventDate.plusDays(1);
      }
    } else if (untilDate != null) {
      while (!eventDate.isAfter(untilDate)) {
        if (daysOfWeek.contains(eventDate.getDayOfWeek())) {
          Event recurringEvent = baseEvent.toBuilder()
              .startDate(eventDate)
              .endDate(eventDate)
              .seriesId(seriesId)
              .build();
          generatedEventsList.add(recurringEvent);
        }
        eventDate = eventDate.plusDays(1);
      }
    }
    return generatedEventsList;
  }
}

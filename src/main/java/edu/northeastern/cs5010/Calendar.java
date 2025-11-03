package edu.northeastern.cs5010;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Calendar {

  private String title;
  private boolean allowConflicts = false;
  private List<Event> events = new ArrayList<>();

  public Calendar(String title) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar title cannot be null or empty");
    }
    this.title = title;
  }

  public void setAllowConflicts(boolean doesAllow) {
    this.allowConflicts = doesAllow;
  }

  public String getTitle() {
    return title;
  }

  public List<Event> getEvent() {
    return new ArrayList<>(events);
  }

  public void addEvent(Event event) {
    events.add(event);
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

}

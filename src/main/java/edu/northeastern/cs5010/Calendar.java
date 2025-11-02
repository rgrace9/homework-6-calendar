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
    return null;
  }

  public List<Event> getEventsOnDate(LocalDate date) {
    return new ArrayList<>();
  }

  public List<Event> getEventsInDateRange(LocalDate start, LocalDate end) {
    return new ArrayList<>();
  }

  public boolean isUserBusy(LocalDate date, LocalTime time) {
    return false;
  }

}

package edu.northeastern.cs5010;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {

  private String subject;
  private LocalDate startDate;
  private LocalDate endDate;

  private LocalTime startTime;
  private LocalTime endTime;
  private String description;
  private String location;
  private Visibility visibility;

  public enum Visibility {
    PUBLIC,
    PRIVATE
  }

  public static class Builder {

    private String subject;
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private String location;
    private Visibility visibility;


    public Builder(String subject, LocalDate startDate, LocalDate endDate) {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject cannot be null or empty");
      }
      if (startDate == null) {
        throw new IllegalArgumentException("Start date cannot be null");
      }
      if (endDate == null) {
        throw new IllegalArgumentException("End date cannot be null");
      }

      this.subject = subject;
      this.startDate = startDate;
      this.endDate = endDate;
    }

    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder startDate(LocalDate date) {
      this.startDate = date;
      return this;
    }

    public Builder endDate(LocalDate date) {
      this.endDate = date;
      return this;
    }


    public Builder startTime(LocalTime start) {
      this.startTime = start;
      return this;
    }

    public Builder endTime(LocalTime end) {
      this.endTime = end;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder location(String location) {
      this.location = location;
      return this;
    }

    public Builder visibility(Visibility visibility) {
      this.visibility = visibility;
      return this;
    }

    public Event build() {
      validateDateTime();
      return new Event(this);
    }

    private void validateDateTime() {
      if (endDate.isBefore(startDate)) {
        throw new IllegalArgumentException("End date cannot be before start date");
      }

      if (startTime == null && endTime != null) {
        throw new IllegalArgumentException("Event without start time must NOT have end time");
      }

      if (startTime != null && endTime == null) {
        throw new IllegalArgumentException("Event with start time must have end time");
      }

      if (endDate.isEqual(startDate) && endTime != null && endTime.isBefore(
          startTime)) {
        throw new IllegalArgumentException("End time cannot be before start time on the same day");
      }

    }
  }

  private Event(Builder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.description = builder.description;
    this.location = builder.location;
    this.visibility = builder.visibility != null ? builder.visibility : Visibility.PUBLIC;
  }

  public String getSubject() {
    return subject;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public Boolean isAllDayEvent() {
    return startTime == null;
  }

  public Builder toBuilder() {
    return new Builder(this.subject, this.startDate, this.endDate)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .description(this.description)
        .location(this.location)
        .visibility(this.visibility);
  }

}

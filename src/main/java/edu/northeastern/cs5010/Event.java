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
      this.subject = subject;
      this.startDate = startDate;
      this.endDate = endDate;
    }

    public Builder startTime(LocalTime start) {
      this.startTime = start;
      return this;
    }

    public Builder endTime(LocalTime end) {
      this.startTime = end;
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
      return new Event(this);
    }
  }

  private Event(Builder builder) {
    this.subject = builder.subject;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
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
  
}

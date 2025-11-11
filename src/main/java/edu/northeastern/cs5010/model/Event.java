package edu.northeastern.cs5010.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single calendar event.
 */
public class Event {

  private final String subject;
  private final LocalDate startDate;
  private final LocalDate endDate;

  private final LocalTime startTime;
  private final LocalTime endTime;
  private final String description;
  private final String location;
  private final Visibility visibility;
  private final String seriesId;

  /**
   * Defines the visibility of an event.
   */
  public enum Visibility {
    PUBLIC,
    PRIVATE
  }


  /**
   * Builder class for constructing {@link Event} instances with optional fields.
   */
  public static class Builder {

    private String subject;
    private LocalDate startDate;
    private LocalDate endDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private String location;
    private Visibility visibility;
    private String seriesId;

    /**
     * Create a builder with the required fields.
     *
     * @param subject   the event subject
     * @param startDate the event start date
     * @param endDate   the event end date
     * @throws IllegalArgumentException if any argument is null or if subject is empty
     */
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

    /**
     * Sets the event subject.
     *
     * @param subject the subject of the event
     * @return this builder instance
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the event start date.
     *
     * @param date the start date of the event
     * @return this builder instance
     */
    public Builder startDate(LocalDate date) {
      this.startDate = date;
      return this;
    }

    /**
     * Sets the event end date.
     *
     * @param date the end date of the event
     * @return this builder instance
     */
    public Builder endDate(LocalDate date) {
      this.endDate = date;
      return this;
    }


    /**
     * Sets the event start time.
     *
     * @param start the start time of the event
     * @return this builder instance
     */
    public Builder startTime(LocalTime start) {
      this.startTime = start;
      return this;
    }

    /**
     * Sets the event end time.
     *
     * @param end the end time of the event
     * @return this builder instance
     */
    public Builder endTime(LocalTime end) {
      this.endTime = end;
      return this;
    }

    /**
     * Sets the event description.
     *
     * @param description the description of the event
     * @return this builder instance
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the event location.
     *
     * @param location the location of the event
     * @return this builder instance
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the event visibility.
     *
     * @param visibility the visibility of the event
     * @return this builder instance
     */
    public Builder visibility(Visibility visibility) {
      this.visibility = visibility;
      return this;
    }

    /**
     * Sets the event series ID if it is part of a recurring event series.
     *
     * @param id the series ID of the event
     * @return this builder instance
     */
    public Builder seriesId(String id) {
      this.seriesId = id;
      return this;
    }

    /**
     * Builds and returns the {@link Event} instance. Validates that the event’s date and time
     * fields are valid before creation.
     *
     * @return a new {@link Event} based on the builder’s current values
     * @throws IllegalArgumentException if the event’s date or time values are invalid
     */
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
    this.seriesId = builder.seriesId;
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

  public String getSeriesId() {
    return seriesId;
  }

  /**
   * Returns whether the event is an all-day event.
   *
   * @return true if the event has no start or end time, or false if not
   */
  public Boolean isAllDayEvent() {
    return startTime == null;
  }

  /**
   * Creates a new {@link Builder} with the event's data.
   *
   * @return a builder initialized with the event's field values
   */
  public Builder toBuilder() {
    return new Builder(this.subject, this.startDate, this.endDate)
        .startTime(this.startTime)
        .endTime(this.endTime)
        .description(this.description)
        .location(this.location)
        .visibility(this.visibility);
  }

}

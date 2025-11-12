package edu.northeastern.cs5010.model;

/**
 * A listener interface for receiving notifications when events are added or modified in a specific
 * calendar.
 */
public interface CalendarListener {

  /**
   * Called when an event is added to a calendar.
   *
   * @param event the event that was added
   */
  void onEventAdded(Event event);

  /**
   * Called when an event is replaced in a calendar.
   *
   * @param event the replaced event
   */
  void onEventReplaced(Event event);
}

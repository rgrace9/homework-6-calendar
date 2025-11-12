package edu.northeastern.cs5010.controller;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.view.CalendarView;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Manages a collection of calendars and handles saving and restoring them.
 *
 * @param calendars the list of calendars managed by this controller
 */
public record CalendarController(List<Calendar> calendars, Path storagePath) {

  /**
   * Creates a controller for the specified list of calendars.
   *
   */
  public CalendarController(List<Calendar> calendars) {
    this(calendars, Paths.get("calendars"));
  }

  /**
   * Saves all calendars to individual CSV files inside the storage path.
   */
  public void saveAllCalendars() {
    if (calendars.isEmpty()) {
      return;
    }
    try {
      if (!Files.exists(storagePath)) {
        Files.createDirectories(storagePath);
      }

      for (Calendar calendar : calendars) {
        Path file = storagePath.resolve(calendar.getTitle() + ".csv");
        calendar.exportToCsv(file.toString());
      }
      System.out.println("All calendars saved successfully to " + storagePath);
    } catch (IOException e) {
      System.err.println("Error saving calendars: " + e.getMessage());
    }
  }

  /**
   * Restores all calendars from the CSV files inside the storage path.
   *
   */
  public void restoreAllCalendars() {
    calendars.clear();
    if (!Files.exists(storagePath)) {
      return;
    }
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(storagePath, "*.csv")) {
      for (Path file : stream) {
        String name = file.getFileName().toString().replaceFirst("[.][^.]+$", "");
        Calendar calendar = new Calendar(name);
        calendar.importFromCsv(file.toString());
        calendars.add(calendar);
        calendar.addCalendarListener(new CalendarView());
      }
    } catch (IOException e) {
      System.err.println("Error restoring calendars: " + e.getMessage());
    }
  }
}

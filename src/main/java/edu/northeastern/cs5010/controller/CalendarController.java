package edu.northeastern.cs5010.controller;

import edu.northeastern.cs5010.model.Calendar;
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
public record CalendarController(List<Calendar> calendars) {

  /**
   * Creates a controller for the specified list of calendars.
   *
   */
  public CalendarController {
  }

  /**
   * Saves all calendars to individual CSV files inside "calendars" directory.
   */
  public void saveAllCalendars() {
    Path folder = Paths.get("calendars");
    try {
      if (!Files.exists(folder)) {
        Files.createDirectories(folder);
      }

      for (Calendar calendar : calendars) {
        Path file = folder.resolve(calendar.getTitle() + ".csv");
        calendar.exportToCsv(file.toString());
      }
      System.out.println("All calendars saved successfully!");
    } catch (IOException e) {
      System.err.println("Error saving calendars: " + e.getMessage());
    }
  }

  /**
   * Restores all calendars from the CSV files inside the "calendars" directory
   *
   */
  public void restoreAllCalendars() {
    Path folder = Paths.get("calendars");
    calendars.clear();

    if (!Files.exists(folder)) {
      System.out.println("No saved calendars found.");
      return;
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, "*.csv")) {
      for (Path file : stream) {
        String fileName = file.getFileName().toString();
        String calendarName = fileName.replaceFirst("[.][^.]+$", "");

        Calendar calendar = new Calendar(calendarName);
        calendar.importFromCsv(file.toString());
        calendars.add(calendar);
      }
      System.out.println("All calendars restored successfully!");
    } catch (IOException e) {
      System.err.println("Error restoring calendars: " + e.getMessage());
    }
  }
}

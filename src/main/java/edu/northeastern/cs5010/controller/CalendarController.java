package edu.northeastern.cs5010.controller;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import edu.northeastern.cs5010.view.CreateEventView;
import edu.northeastern.cs5010.view.EventDetailView;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Controller responsible for restoring, managing, and saving calendars.
 */
public class CalendarController {

  private final List<Calendar> calendars;
  private final Path storagePath;

  public CalendarController(List<Calendar> calendars) {
    this.calendars = calendars;
    this.storagePath = Paths.get("calendars");
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
   * Restores all calendars from CSV files inside the storage path.
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
      }
      System.out.println("Calendars restored from " + storagePath);
    } catch (IOException e) {
      System.err.println("Error restoring calendars: " + e.getMessage());
    }
  }

  /**
   * Returns the list of calendars.
   */
  public List<Calendar> getCalendars() {
    return calendars;
  }

  /**
   * Starts the calendar app: restores calendars, opens views, and saves changes on exit.
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      List<Calendar> calendars = new java.util.ArrayList<>();
      CalendarController controller = new CalendarController(calendars);

      controller.restoreAllCalendars();

      if (controller.getCalendars().isEmpty()) {
        controller.getCalendars().add(new Calendar("Personal Calendar"));
      }

      Calendar selectedCalendar = controller.getCalendars().getFirst();

      if (selectedCalendar.getEvents().isEmpty()) {
        Event sample = new Event.Builder("Welcome!", LocalDate.now(), LocalDate.now())
            .description("Your first event!")
            .build();
        selectedCalendar.addEvent(sample);
      }

      Event sampleEvent = selectedCalendar.getEvents().getLast();

      CreateEventView createView = new CreateEventView(selectedCalendar);
      createView.setVisible(true);

      EventDetailView detailView = new EventDetailView(selectedCalendar, sampleEvent);
      detailView.setVisible(true);

      Runtime.getRuntime().addShutdownHook(new Thread(controller::saveAllCalendars));
    });
  }
}

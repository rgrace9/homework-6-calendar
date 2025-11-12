package edu.northeastern.cs5010;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.northeastern.cs5010.controller.CalendarController;
import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarControllerTest {

  private Path folder;
  private CalendarController controller;
  private List<Calendar> calendars;

  @BeforeEach
  void setUp() throws IOException {
    folder = Paths.get("src", "test", "resources", "calendarTest");

    if (!Files.exists(folder)) {
      Files.createDirectories(folder);
    }

    calendars = new ArrayList<>();
    Calendar work = new Calendar("Work");
    work.addEvent(new Event.Builder("Meeting",
        LocalDate.of(2025, 11, 11),
        LocalDate.of(2025, 11, 11)).build());
    calendars.add(work);
    controller = new CalendarController(calendars);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (Files.exists(folder)) {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
        for (Path file : stream) {
          Files.deleteIfExists(file);
        }
      }
      Files.deleteIfExists(folder);
    }
  }

  @Test
  void saveAllCalendarsCreatesDirectory() {
    controller.saveAllCalendars();
    assertTrue(Files.exists(folder));
  }

  @Test
  void saveAllCalendarsCreatesCsvFile() throws IOException {
    controller.saveAllCalendars();

    Path actualFile = Paths.get("calendars", "Work.csv");

    Path folder = Paths.get("src", "test", "resources", "calendarTest");
    Path file = folder.resolve("Work.csv");

    if (Files.exists(actualFile)) {
      Files.copy(actualFile, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    assertTrue(Files.exists(file));
  }


  @Test
  void restoreAllCalendarsAddsCalendarsToList() {
    controller.saveAllCalendars();
    calendars.clear();
    controller.restoreAllCalendars();
    assertEquals(1, calendars.size());
  }

  @Test
  void restoreAllCalendarsRestoresCorrectCalendarName() {
    controller.saveAllCalendars();
    calendars.clear();
    controller.restoreAllCalendars();
    assertEquals("Work", calendars.getFirst().getTitle());
  }
}

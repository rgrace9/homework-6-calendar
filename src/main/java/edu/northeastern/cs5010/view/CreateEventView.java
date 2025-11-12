package edu.northeastern.cs5010.view;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import edu.northeastern.cs5010.model.RecurringEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Swing view for creating either a single or recurring event in a specific calendar. This view does
 * not implement any model listeners yet — only internal UI logic.
 */
public class CreateEventView extends JFrame {

  private final Calendar calendar;

  // Core event fields
  private final JTextField titleField = new JTextField(20);
  private final JTextField startDateField = new JTextField(10);
  private final JTextField endDateField = new JTextField(10);
  private final JTextField startTimeField = new JTextField(8);
  private final JTextField endTimeField = new JTextField(8);
  private final JTextField locationField = new JTextField(20);
  private final JTextArea descriptionArea = new JTextArea(3, 20);

  // Recurrence options
  private final JCheckBox recurringCheck = new JCheckBox("Make this a recurring event");
  private final JPanel recurrencePanel = new JPanel();
  private final JCheckBox[] dayBoxes = new JCheckBox[]{
      new JCheckBox("Mon"), new JCheckBox("Tue"), new JCheckBox("Wed"),
      new JCheckBox("Thu"), new JCheckBox("Fri"), new JCheckBox("Sat"), new JCheckBox("Sun")
  };
  private final JTextField occurrencesField = new JTextField(5);
  private final JTextField untilDateField = new JTextField(10);

  // Buttons
  private final JButton createButton = new JButton("Create Event");
  private final JButton cancelButton = new JButton("Cancel");

  public CreateEventView(Calendar calendar) {
    this.calendar = calendar;

    setTitle("Create Event - " + calendar.getTitle());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));

    // === Main container with vertical layout ===
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // === Form fields ===
    JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    formPanel.setBorder(BorderFactory.createTitledBorder("Event Details"));
    formPanel.add(new JLabel("Title:"));
    formPanel.add(titleField);
    formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
    formPanel.add(startDateField);
    formPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
    formPanel.add(endDateField);
    formPanel.add(new JLabel("Start Time (optional, HH:MM):"));
    formPanel.add(startTimeField);
    formPanel.add(new JLabel("End Time (optional, HH:MM):"));
    formPanel.add(endTimeField);
    formPanel.add(new JLabel("Location (optional):"));
    formPanel.add(locationField);
    formPanel.add(new JLabel("Description (optional):"));
    formPanel.add(new JScrollPane(descriptionArea));

    // === Recurring toggle ===
    JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    togglePanel.add(recurringCheck);

    // === Recurrence panel ===
    recurrencePanel.setLayout(new GridLayout(0, 2, 5, 5));
    recurrencePanel.setBorder(BorderFactory.createTitledBorder("Recurrence Options"));
    recurrencePanel.setVisible(false);

    JPanel daysPanel = new JPanel();
    for (JCheckBox box : dayBoxes) {
      daysPanel.add(box);
    }
    recurrencePanel.add(new JLabel("Days of Week:"));
    recurrencePanel.add(daysPanel);
    recurrencePanel.add(new JLabel("Occurrences (optional):"));
    recurrencePanel.add(occurrencesField);
    recurrencePanel.add(new JLabel("Until Date (optional, YYYY-MM-DD):"));
    recurrencePanel.add(untilDateField);

    // === Buttons ===
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);

    // === Add sections vertically ===
    contentPanel.add(formPanel);
    contentPanel.add(togglePanel);
    contentPanel.add(recurrencePanel);
    contentPanel.add(Box.createVerticalStrut(10)); // spacing
    contentPanel.add(buttonPanel);

    add(contentPanel, BorderLayout.CENTER);

    // === UI behavior ===
    recurringCheck.addActionListener(this::toggleRecurrenceFields);
    createButton.addActionListener(this::handleCreate);
    cancelButton.addActionListener(e -> dispose());

    pack();
    setLocationRelativeTo(null);
  }

  /**
   * Toggles recurrence options visibility when the recurring checkbox is clicked.
   */
  private void toggleRecurrenceFields(ActionEvent e) {
    recurrencePanel.setVisible(recurringCheck.isSelected());
    pack();
  }

  /**
   * Handles saving the event — calls the correct Calendar method.
   */
  private void handleCreate(ActionEvent e) {
    try {
      String title = titleField.getText().trim();
      if (title.isEmpty()) {
        throw new IllegalArgumentException("Title is required.");
      }

      LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
      LocalDate endDate = LocalDate.parse(endDateField.getText().trim());

      Event baseEvent = new Event.Builder(title, startDate, endDate)
          .description(descriptionArea.getText().trim())
          .location(locationField.getText().trim())
          .build();

      if (!recurringCheck.isSelected()) {
        calendar.addEvent(baseEvent);
      } else {
        List<DayOfWeek> days = new ArrayList<>();
        DayOfWeek[] allDays = DayOfWeek.values();
        for (int i = 0; i < dayBoxes.length; i++) {
          if (dayBoxes[i].isSelected()) {
            days.add(allDays[i]);
          }
        }

        Integer occurrences = null;
        LocalDate untilDate = null;
        if (!occurrencesField.getText().isBlank()) {
          occurrences = Integer.parseInt(occurrencesField.getText().trim());
        }
        if (!untilDateField.getText().isBlank()) {
          untilDate = LocalDate.parse(untilDateField.getText().trim());
        }

        RecurringEvent recurringEvent = new RecurringEvent(baseEvent, days, occurrences, untilDate);
        calendar.addRecurringEvent(recurringEvent);
      }

      JOptionPane.showMessageDialog(this, "Event created successfully!");
      dispose();

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }
}

package edu.northeastern.cs5010.view;

import edu.northeastern.cs5010.model.Calendar;
import edu.northeastern.cs5010.model.Event;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Swing view for displaying and editing details of a single or recurring event. This view shows the
 * series ID (if applicable) and allows edits accordingly.
 */
public class EventDetailView extends JFrame {

  private final Calendar calendar;
  private final Event event;

  // Common fields
  private final JTextField titleField = new JTextField(20);
  private final JTextField startDateField = new JTextField(10);
  private final JTextField endDateField = new JTextField(10);
  private final JTextField locationField = new JTextField(20);
  private final JTextArea descriptionArea = new JTextArea(4, 20);

  // Optional field for recurring series
  private final JLabel seriesIdLabel = new JLabel("Series ID:");
  private final JTextField seriesIdField = new JTextField(20);

  // Action buttons
  private final JButton saveSingleButton = new JButton("Save This Event");
  private final JButton saveSeriesButton = new JButton("Save Entire Series");
  private final JButton cancelButton = new JButton("Cancel");

  public EventDetailView(Calendar calendar, Event event) {
    this.calendar = calendar;
    this.event = event;

    setTitle("Edit Event - " + event.getSubject());
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));

    JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
    form.add(new JLabel("Title:"));
    form.add(titleField);
    form.add(new JLabel("Start Date (YYYY-MM-DD):"));
    form.add(startDateField);
    form.add(new JLabel("End Date (YYYY-MM-DD):"));
    form.add(endDateField);
    form.add(new JLabel("Location (optional):"));
    form.add(locationField);
    form.add(new JLabel("Description (optional):"));
    form.add(new JScrollPane(descriptionArea));

    // Only show series ID if this is a recurring event
    if (event.getSeriesId() != null) {
      seriesIdField.setText(event.getSeriesId().toString());
      seriesIdField.setEditable(false);
      form.add(seriesIdLabel);
      form.add(seriesIdField);
    }

    add(form, BorderLayout.CENTER);

    JPanel buttons = new JPanel(new FlowLayout());
    if (event.getSeriesId() != null) {
      buttons.add(saveSingleButton);
      buttons.add(saveSeriesButton);
    } else {
      buttons.add(saveSingleButton);
    }
    buttons.add(cancelButton);
    add(buttons, BorderLayout.SOUTH);

    populateFields();

    // Event listeners (still simple and direct per assignment)
    saveSingleButton.addActionListener(this::saveSingle);
    saveSeriesButton.addActionListener(this::saveSeries);
    cancelButton.addActionListener(e -> dispose());

    pack();
    setLocationRelativeTo(null);
  }

  private void populateFields() {
    titleField.setText(event.getSubject());
    startDateField.setText(event.getStartDate().toString());
    endDateField.setText(event.getEndDate().toString());
    locationField.setText(event.getLocation());
    descriptionArea.setText(event.getDescription());
  }

  private void saveSingle(ActionEvent e) {
    Event updated = buildUpdatedEvent();
    calendar.editSingleInstance(event, updated);
    JOptionPane.showMessageDialog(this, "Single event updated successfully!");
    dispose();
  }

  private void saveSeries(ActionEvent e) {
    Event updated = buildUpdatedEvent();
    calendar.editEntireSeries(event.getSeriesId(), updated);
    JOptionPane.showMessageDialog(this, "All events in series updated!");
    dispose();
  }

  private Event buildUpdatedEvent() {
    return event.toBuilder()
        .subject(titleField.getText().trim())
        .startDate(LocalDate.parse(startDateField.getText().trim()))
        .endDate(LocalDate.parse(endDateField.getText().trim()))
        .location(locationField.getText().trim())
        .description(descriptionArea.getText().trim())
        .build();
  }
}

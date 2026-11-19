package task2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class OutpatientAppointmentFrame extends JFrame {
    private final JTextField patientIdField = new JTextField(16);
    private final JComboBox<String> deptCombo = new JComboBox<>();
    private final JComboBox<String> doctorCombo = new JComboBox<>();
    private final JComboBox<String> slotCombo = new JComboBox<>(new String[]{
            "09:30 AM", "10:30 AM", "11:30 AM", "02:30 PM", "04:00 PM"
    });
    private final JButton bookButton = new JButton("Book Appointment");
    private final JButton clearButton = new JButton("Clear");
    private final JLabel statusLabel = new JLabel("Enter patient details to schedule an appointment.");

    private final Map<String, List<String>> deptDoctorsMap = new LinkedHashMap<>();
    private final Set<String> bookedSlots = new HashSet<>();
    private boolean bookingInProgress = false;

    public OutpatientAppointmentFrame() {
        super("Hospital Outpatient Appointment Desk");
        initDepartmentCache();
        buildUI();
        wireEvents();
    }

    private void initDepartmentCache() {
        deptDoctorsMap.put("Cardiology", Arrays.asList("Dr. R. Meenakshi", "Dr. K. Suresh"));
        deptDoctorsMap.put("Orthopedics", Arrays.asList("Dr. A. Nair", "Dr. V. Karthik"));
        deptDoctorsMap.put("Neurology", Arrays.asList("Dr. S. Priya", "Dr. M. Raghavan"));
        deptDoctorsMap.put("General Medicine", Arrays.asList("Dr. P. Lakshmi", "Dr. D. Arun"));
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Outpatient Appointment Booking"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; formPanel.add(patientIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Department:"), gbc);
        for (String dept : deptDoctorsMap.keySet()) {
            deptCombo.addItem(dept);
        }
        gbc.gridx = 1; formPanel.add(deptCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1; formPanel.add(doctorCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 1; formPanel.add(slotCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        buttonPanel.add(bookButton);
        buttonPanel.add(clearButton);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 10, 12));
        statusLabel.setForeground(new Color(20, 70, 120));

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(formPanel, BorderLayout.NORTH);
        wrapper.add(buttonPanel, BorderLayout.CENTER);
        wrapper.add(statusLabel, BorderLayout.SOUTH);
        setContentPane(wrapper);

        refreshDoctorList();
        bookingInProgress = false;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void wireEvents() {
        patientIdField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { bookingInProgress = true; }
            @Override public void removeUpdate(DocumentEvent e) { bookingInProgress = !patientIdField.getText().trim().isEmpty(); }
            @Override public void changedUpdate(DocumentEvent e) { bookingInProgress = true; }
        });

        deptCombo.addActionListener(e -> {
            refreshDoctorList();
            bookingInProgress = true;
        });

        doctorCombo.addActionListener(e -> bookingInProgress = true);
        slotCombo.addActionListener(e -> bookingInProgress = true);

        bookButton.addActionListener(e -> handleBookAppointment());
        clearButton.addActionListener(e -> resetForm());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (bookingInProgress) {
                    int choice = JOptionPane.showConfirmDialog(
                            OutpatientAppointmentFrame.this,
                            "A booking is currently in progress. Discard changes and close?",
                            "Confirm Window Close",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                dispose();
            }
        });
    }

    private void refreshDoctorList() {
        String selectedDept = (String) deptCombo.getSelectedItem();
        List<String> doctors = deptDoctorsMap.getOrDefault(selectedDept, Collections.emptyList());
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String doc : doctors) {
            model.addElement(doc);
        }
        doctorCombo.setModel(model);
    }

    private void handleBookAppointment() {
        String patientId = patientIdField.getText().trim().toUpperCase();
        String dept = (String) deptCombo.getSelectedItem();
        String doctor = (String) doctorCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();

        if (!patientId.matches("^PAT-[0-9]{4,6}$")) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: Patient ID must match PAT-XXXX (e.g., PAT-1024).");
            JOptionPane.showMessageDialog(this, "Invalid Patient ID format. Expected PAT-XXXX.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (doctor == null || slot == null) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: Please select both a doctor and a time slot.");
            return;
        }

        String bookingKey = doctor + "|" + slot;
        if (bookedSlots.contains(bookingKey)) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: " + doctor + " is already booked at " + slot + ".");
            JOptionPane.showMessageDialog(this, "Selected slot is already occupied for " + doctor + ".", "Slot Conflict", JOptionPane.ERROR_MESSAGE);
            return;
        }

        bookedSlots.add(bookingKey);
        bookingInProgress = false;
        String summary = String.format("Confirmed: %s -> %s (%s) at %s", patientId, doctor, dept, slot);
        statusLabel.setForeground(new Color(0, 128, 0));
        statusLabel.setText(summary);
        JOptionPane.showMessageDialog(this, summary, "Appointment Confirmed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetForm() {
        patientIdField.setText("");
        deptCombo.setSelectedIndex(0);
        refreshDoctorList();
        slotCombo.setSelectedIndex(0);
        bookingInProgress = false;
        statusLabel.setForeground(new Color(20, 70, 120));
        statusLabel.setText("Form cleared. Ready for next patient.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OutpatientAppointmentFrame().setVisible(true));
    }
}

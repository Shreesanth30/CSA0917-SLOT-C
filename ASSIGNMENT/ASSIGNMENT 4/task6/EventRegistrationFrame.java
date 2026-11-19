package task6;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class EventRegistrationFrame extends JFrame {
    private static final int PAGE_SIZE = 25;

    private final JTextField studentIdField = new JTextField(12);
    private final JTextField nameField = new JTextField(16);
    private final JTextField emailField = new JTextField(16);
    private final JComboBox<String> deptCombo = new JComboBox<>(new String[]{"CSE", "ECE", "AI&DS", "MECH", "CIVIL"});
    private final JComboBox<String> eventCombo = new JComboBox<>(new String[]{
            "Hackathon 2026", "RoboWars", "Cultural Night", "Debate Championship"
    });
    private final JButton registerButton = new JButton("Register Student");

    private final JComboBox<String> filterCombo = new JComboBox<>(new String[]{
            "All Events", "Hackathon 2026", "RoboWars", "Cultural Night", "Debate Championship"
    });
    private final JButton exportButton = new JButton("Export CSV");
    private final JButton prevButton = new JButton("< Prev");
    private final JButton nextButton = new JButton("Next >");
    private final JLabel pageLabel = new JLabel("Page 1 of 1");
    private final JLabel statusLabel = new JLabel("Ready.");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Reg ID", "Student ID", "Name", "Email", "Dept", "Event", "Timestamp"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    private final RegistrationService service;
    private int currentPage = 0;
    private int totalRows = 0;

    public EventRegistrationFrame(RegistrationService service) {
        super("Campus Cultural Committee - Event Registration Manager");
        this.service = service;
        buildMenuBar();
        buildLayout();
        wireHandlers();
        refreshTableAsync();
    }

    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("Refresh Table");
        JMenuItem exportItem = new JMenuItem("Export Filtered CSV...");
        JMenuItem exitItem = new JMenuItem("Exit");
        refreshItem.addActionListener(e -> refreshTableAsync());
        exportItem.addActionListener(e -> handleExportCsv());
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(refreshItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Campus Event Registration Desktop Application\nSwing + JDBC Layered Architecture",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void buildLayout() {
        setLayout(new BorderLayout(8, 8));

        // Registration Form (NORTH)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("New Student Event Registration"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; formPanel.add(studentIdField, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 3; formPanel.add(nameField, gbc);
        gbc.gridx = 4; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 5; formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; formPanel.add(deptCombo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Event:"), gbc);
        gbc.gridx = 3; formPanel.add(eventCombo, gbc);
        gbc.gridx = 4; gbc.gridwidth = 2; formPanel.add(registerButton, gbc);

        // Center Panel: Filter Toolbar + Paginated JTable
        JPanel centerPanel = new JPanel(new BorderLayout(6, 6));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Registered Students"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.add(new JLabel("Filter by Event:"));
        toolbar.add(filterCombo);
        toolbar.add(exportButton);
        toolbar.add(prevButton);
        toolbar.add(pageLabel);
        toolbar.add(nextButton);

        centerPanel.add(toolbar, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));

        add(formPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { dispose(); }
        });

        setSize(920, 560);
        setLocationRelativeTo(null);
    }

    private void wireHandlers() {
        registerButton.addActionListener(e -> handleRegister());
        filterCombo.addActionListener(e -> {
            currentPage = 0;
            refreshTableAsync();
        });
        prevButton.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                refreshTableAsync();
            }
        });
        nextButton.addActionListener(e -> {
            if ((currentPage + 1) * PAGE_SIZE < totalRows) {
                currentPage++;
                refreshTableAsync();
            }
        });
        exportButton.addActionListener(e -> handleExportCsv());
    }

    private void handleRegister() {
        String id = studentIdField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String dept = (String) deptCombo.getSelectedItem();
        String event = (String) eventCombo.getSelectedItem();

        registerButton.setEnabled(false);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText("Validating and saving registration...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.registerStudent(id, name, email, dept, event);
                return null;
            }

            @Override
            protected void done() {
                registerButton.setEnabled(true);
                try {
                    get();
                    studentIdField.setText("");
                    nameField.setText("");
                    emailField.setText("");
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Success: Registered " + id.trim().toUpperCase() + " for " + event + ".");
                    JOptionPane.showMessageDialog(EventRegistrationFrame.this,
                            "Registration confirmed for " + id.trim().toUpperCase() + " (" + event + ").",
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                    currentPage = 0;
                    refreshTableAsync();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Rejected: " + cause.getMessage());
                    JOptionPane.showMessageDialog(EventRegistrationFrame.this,
                            cause.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void refreshTableAsync() {
        String selectedEvent = (String) filterCombo.getSelectedItem();
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText("Loading page " + (currentPage + 1) + " from MySQL...");

        new SwingWorker<List<Registration>, Void>() {
            private int count = 0;

            @Override
            protected List<Registration> doInBackground() throws Exception {
                count = service.countRegistrations(selectedEvent);
                return service.loadPage(selectedEvent, currentPage, PAGE_SIZE);
            }

            @Override
            protected void done() {
                try {
                    List<Registration> rows = get();
                    totalRows = count;
                    tableModel.setRowCount(0);
                    for (Registration r : rows) {
                        tableModel.addRow(new Object[]{
                                r.getRegId(), r.getStudentId(), r.getStudentName(),
                                r.getEmail(), r.getDepartment(), r.getEventName(), r.getRegisteredAt()
                        });
                    }
                    int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / PAGE_SIZE));
                    pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages + " (" + totalRows + " records)");
                    prevButton.setEnabled(currentPage > 0);
                    nextButton.setEnabled((currentPage + 1) * PAGE_SIZE < totalRows);
                    statusLabel.setForeground(new Color(20, 70, 120));
                    statusLabel.setText("Loaded " + rows.size() + " rows (" + selectedEvent + ").");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Database load error: " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void handleExportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("event_registrations.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File target = chooser.getSelectedFile();
        String selectedEvent = (String) filterCombo.getSelectedItem();
        statusLabel.setText("Exporting CSV...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return service.exportToCsv(selectedEvent, target);
            }

            @Override
            protected void done() {
                try {
                    int exported = get();
                    statusLabel.setForeground(new Color(0, 128, 0));
                    statusLabel.setText("Exported " + exported + " records to " + target.getName());
                    JOptionPane.showMessageDialog(EventRegistrationFrame.this,
                            "Exported " + exported + " rows to " + target.getAbsolutePath(),
                            "CSV Export Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Export failed: " + cause.getMessage());
                }
            }
        }.execute();
    }
}

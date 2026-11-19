package task6;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class RegistrationService {
    private final RegistrationDAO dao;

    public RegistrationService(RegistrationDAO dao) {
        this.dao = dao;
    }

    public void registerStudent(String rawId, String rawName, String rawEmail, String dept, String event)
            throws IllegalArgumentException, SQLException {
        if (rawId == null || !rawId.trim().matches("^[A-Za-z0-9]{6,15}$")) {
            throw new IllegalArgumentException("Invalid Student ID: must be 6-15 alphanumeric characters (e.g., 24CS1042).");
        }
        if (rawName == null || rawName.trim().length() < 2) {
            throw new IllegalArgumentException("Student Name must be at least 2 characters.");
        }
        if (rawEmail == null || !rawEmail.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid Email address format.");
        }
        if (dept == null || dept.trim().isEmpty() || event == null || event.trim().isEmpty()) {
            throw new IllegalArgumentException("Department and Event must be selected.");
        }

        String studentId = rawId.trim().toUpperCase();
        String studentName = rawName.trim();
        String email = rawEmail.trim().toLowerCase();

        // Layer 1 duplicate check (fast, user-friendly feedback)
        if (dao.existsByStudentAndEvent(studentId, event)) {
            throw new IllegalStateException("Duplicate Registration: Student " + studentId + " is already registered for " + event + ".");
        }

        // Layer 2 DB insert protected by UNIQUE(student_id, event_name) constraint
        try {
            dao.insert(new Registration(0, studentId, studentName, email, dept, event, null));
        } catch (SQLIntegrityConstraintViolationException dupEx) {
            throw new IllegalStateException("Duplicate Registration: Student " + studentId + " is already registered for " + event + ".");
        }
    }

    public int countRegistrations(String eventFilter) throws SQLException {
        return dao.countFiltered(eventFilter);
    }

    public List<Registration> loadPage(String eventFilter, int page, int pageSize) throws SQLException {
        return dao.findPage(eventFilter, pageSize, page * pageSize);
    }

    public int exportToCsv(String eventFilter, File targetFile) throws SQLException, IOException {
        List<Registration> rows = dao.findAllForExport(eventFilter);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile))) {
            bw.write("RegID,StudentID,StudentName,Email,Department,EventName,RegisteredAt");
            bw.newLine();
            for (Registration r : rows) {
                bw.write(String.format("%d,"%s","%s","%s","%s","%s","%s"",
                        r.getRegId(),
                        r.getStudentId().replace(""", """"),
                        r.getStudentName().replace(""", """"),
                        r.getEmail().replace(""", """"),
                        r.getDepartment().replace(""", """"),
                        r.getEventName().replace(""", """"),
                        r.getRegisteredAt()));
                bw.newLine();
            }
        }
        return rows.size();
    }
}

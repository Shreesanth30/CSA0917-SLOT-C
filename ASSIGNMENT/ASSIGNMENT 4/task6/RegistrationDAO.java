package task6;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {

    public boolean existsByStudentAndEvent(String studentId, String eventName) throws SQLException {
        String sql = "SELECT 1 FROM registrations WHERE student_id = ? AND event_name = ? LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, eventName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Registration reg) throws SQLException {
        String sql = "INSERT INTO registrations (student_id, student_name, email, department, event_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reg.getStudentId());
            ps.setString(2, reg.getStudentName());
            ps.setString(3, reg.getEmail());
            ps.setString(4, reg.getDepartment());
            ps.setString(5, reg.getEventName());
            ps.executeUpdate();
        }
    }

    public int countFiltered(String eventFilter) throws SQLException {
        boolean all = (eventFilter == null || "All Events".equals(eventFilter));
        String sql = all
                ? "SELECT COUNT(*) FROM registrations"
                : "SELECT COUNT(*) FROM registrations WHERE event_name = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!all) {
                ps.setString(1, eventFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<Registration> findPage(String eventFilter, int limit, int offset) throws SQLException {
        boolean all = (eventFilter == null || "All Events".equals(eventFilter));
        String sql = all
                ? "SELECT reg_id, student_id, student_name, email, department, event_name, registered_at FROM registrations ORDER BY reg_id DESC LIMIT ? OFFSET ?"
                : "SELECT reg_id, student_id, student_name, email, department, event_name, registered_at FROM registrations WHERE event_name = ? ORDER BY reg_id DESC LIMIT ? OFFSET ?";
        List<Registration> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (!all) {
                ps.setString(idx++, eventFilter);
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Registration(
                            rs.getInt("reg_id"),
                            rs.getString("student_id"),
                            rs.getString("student_name"),
                            rs.getString("email"),
                            rs.getString("department"),
                            rs.getString("event_name"),
                            rs.getTimestamp("registered_at")
                    ));
                }
            }
        }
        return list;
    }

    public List<Registration> findAllForExport(String eventFilter) throws SQLException {
        return findPage(eventFilter, Integer.MAX_VALUE, 0);
    }
}

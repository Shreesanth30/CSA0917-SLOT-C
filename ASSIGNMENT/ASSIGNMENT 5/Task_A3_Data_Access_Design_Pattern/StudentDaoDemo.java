package partA_design;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Part A (Task A3 & Task A4): Generic Dao<T, K> implementation and safe Serialization.
 */
public class StudentDaoDemo {
    public interface Dao<T, K> {
        boolean insert(T entity) throws SQLException;
        T findById(K id) throws SQLException;
        List<T> findAll() throws SQLException;
        boolean update(T entity) throws SQLException;
        boolean delete(K id) throws SQLException;
    }

    public record Student(String regNo, String fullName, String branch, int semester, double cgpa) {}

    public static class StudentDao implements Dao<Student, String> {
        private final Connection con;
        public StudentDao(Connection con) { this.con = con; }

        @Override
        public boolean insert(Student s) throws SQLException {
            String sql = "INSERT INTO student(reg_no, full_name, branch, semester, cgpa) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, s.regNo());
                ps.setString(2, s.fullName());
                ps.setString(3, s.branch());
                ps.setInt(4, s.semester());
                ps.setDouble(5, s.cgpa());
                return ps.executeUpdate() == 1;
            }
        }

        @Override
        public Student findById(String regNo) throws SQLException {
            String sql = "SELECT reg_no, full_name, branch, semester, cgpa FROM student WHERE reg_no = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, regNo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Student(rs.getString("reg_no"), rs.getString("full_name"),
                                rs.getString("branch"), rs.getInt("semester"), rs.getDouble("cgpa"));
                    }
                }
            }
            return null;
        }

        @Override
        public List<Student> findAll() throws SQLException {
            List<Student> list = new ArrayList<>();
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT reg_no, full_name, branch, semester, cgpa FROM student")) {
                while (rs.next()) {
                    list.add(new Student(rs.getString("reg_no"), rs.getString("full_name"),
                            rs.getString("branch"), rs.getInt("semester"), rs.getDouble("cgpa")));
                }
            }
            return list;
        }

        @Override
        public boolean update(Student s) throws SQLException {
            String sql = "UPDATE student SET full_name=?, branch=?, semester=?, cgpa=? WHERE reg_no=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, s.fullName());
                ps.setString(2, s.branch());
                ps.setInt(3, s.semester());
                ps.setDouble(4, s.cgpa());
                ps.setString(5, s.regNo());
                return ps.executeUpdate() == 1;
            }
        }

        @Override
        public boolean delete(String regNo) throws SQLException {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM student WHERE reg_no = ?")) {
                ps.setString(1, regNo);
                return ps.executeUpdate() == 1;
            }
        }
    }

    // Task A4: Safe Serializable Domain Classes (transient Connection & password)
    public static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final String regNo;
        private transient String password;      // Excluded from serialized stream for security
        private transient Connection dbCon;     // Excluded to prevent NotSerializableException

        public Resident(String name, String regNo, String password, Connection dbCon) {
            this.name = name;
            this.regNo = regNo;
            this.password = password;
            this.dbCon = dbCon;
        }
    }

    public static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int roomNumber;
        private final List<Resident> residents = new ArrayList<>();
        public Room(int roomNumber) { this.roomNumber = roomNumber; }
    }

    public static class Hostel implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String hostelName;
        private final List<Room> rooms = new ArrayList<>();
        public Hostel(String hostelName) { this.hostelName = hostelName; }
    }
}

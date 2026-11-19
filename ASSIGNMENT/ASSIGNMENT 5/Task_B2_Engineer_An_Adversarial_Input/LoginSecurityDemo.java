package part2_persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Task B2: Demonstrates vulnerability of concatenated SQL statements vs.
 * structural safety of parameterized PreparedStatement queries.
 */
public class LoginSecurityDemo {
    // UNSAFE: Vulnerable to SQL Injection via string concatenation
    public static boolean unsafeLogin(Connection con, String username, String password)
            throws SQLException {
        String query = "SELECT * FROM users WHERE username = '" + username
                     + "' AND password = '" + password + "'";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            return rs.next();
        }
    }

    // SAFE: Separates SQL syntax tree from bound literal data
    public static boolean safeLogin(Connection con, String username, String password)
            throws SQLException {
        String query = "SELECT id, role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}

package part2_persistence;

import java.sql.*;
import java.util.*;

/**
 * Task B3 Runner: Executes B3.1 (ResultSetMetaData verification),
 * B3.2 (Statement vs PreparedStatement vs Batch timing for 1k, 10k, 100k),
 * and B3.3 (Auto-commit vs Transaction rollback on mid-batch failure).
 */
public class TaskB3BenchmarkRunner {
    public static void verifyMetadata(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT pass_id, reg_no, name, route_no, amount FROM bus_pass LIMIT 10")) {
            ResultSetMetaData meta = rs.getMetaData();
            System.out.println("=== Task B3.1: ResultSetMetaData Verification ===");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                System.out.printf("Col %d: %-10s | Type: %-12s%n",
                        i, meta.getColumnName(i), meta.getColumnTypeName(i));
            }
        }
    }

    public static List<String[]> generatePasses(int count, boolean injectInvalidAtHalf) {
        List<String[]> list = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            double amt = (injectInvalidAtHalf && i == count / 2 + 1) ? -99.0 : 4500.0;
            list.add(new String[]{
                "P" + (200000 + i),
                "19232" + String.format("%05d", i),
                "Student_" + i,
                String.valueOf((i % 25) + 1),
                String.valueOf(amt)
            });
        }
        return list;
    }

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/transport_db?rewriteBatchedStatements=true";
        try (Connection con = DriverManager.getConnection(url, "root", "password")) {
            for (int size : new int[]{1000, 10000, 100000}) {
                con.createStatement().executeUpdate("TRUNCATE TABLE bus_pass");
                List<String[]> data = generatePasses(size, false);
                long t0 = System.nanoTime();
                int loaded = PassLoader.loadAll(con, data);
                double ms = (System.nanoTime() - t0) / 1_000_000.0;
                System.out.printf("Batch Loaded %,d rows in %.2f ms%n", loaded, ms);
            }
            verifyMetadata(con);

            // B3.3 Half-way failure rollback verification
            con.createStatement().executeUpdate("TRUNCATE TABLE bus_pass");
            try {
                PassLoader.loadAll(con, generatePasses(1000, true));
            } catch (Exception expected) {
                System.out.println("Caught expected failure: " + expected.getMessage());
            }
            try (ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(*) FROM bus_pass")) {
                rs.next();
                System.out.println("Rows remaining after atomic rollback: " + rs.getInt(1));
            }
        }
    }
}

package part2_persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Task B4: Practical migration strategy for 12-campus scale with validation
 * failure reporting, batch transaction loading, and DatabaseMetaData portability checks.
 */
public class CampusMigration {
    public static void printDatabasePortabilityReport(Connection con) throws SQLException {
        DatabaseMetaData meta = con.getMetaData();
        System.out.println("=== JDBC / Server Portability Verification ===");
        System.out.println("Database Product : " + meta.getDatabaseProductName()
                + " " + meta.getDatabaseProductVersion());
        System.out.println("JDBC Driver      : " + meta.getDriverName()
                + " " + meta.getDriverVersion());
        System.out.println("Supports Batch   : " + meta.supportsBatchUpdates());
        System.out.println("Supports Txns    : " + meta.supportsTransactions());
    }

    public static int migrateFile(Connection con, Path csvFile, Path failureLog)
            throws IOException, SQLException {
        List<String[]> validBatch = new ArrayList<>(1000);
        List<String> failures = new ArrayList<>();
        int lineNum = 0;

        try (BufferedReader br = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (lineNum == 1 && line.startsWith("pass_id,")) {
                    continue; // skip header
                }
                String[] parts = line.split(",", -1);
                if (parts.length != 7) {
                    failures.add("Line " + lineNum + " [COLUMN_COUNT]: " + line);
                    continue;
                }
                String passId = parts[0].trim();
                String regNo = parts[1].trim();
                String name = parts[2].trim();
                if (passId.isEmpty() || regNo.isEmpty() || name.isEmpty()) {
                    failures.add("Line " + lineNum + " [MISSING_REQUIRED_FIELD]: " + line);
                    continue;
                }
                try {
                    int route = Integer.parseInt(parts[3].trim());
                    double amt = Double.parseDouble(parts[6].trim());
                    if (route <= 0 || amt <= 0) {
                        failures.add("Line " + lineNum + " [NON_POSITIVE_VALUE]: " + line);
                        continue;
                    }
                    validBatch.add(new String[]{passId, regNo, name,
                            String.valueOf(route), String.valueOf(amt)});
                } catch (NumberFormatException ex) {
                    failures.add("Line " + lineNum + " [PARSE_ERROR]: " + line);
                }
            }
        }

        try (BufferedWriter fw = Files.newBufferedWriter(failureLog, StandardCharsets.UTF_8)) {
            for (String err : failures) {
                fw.write(err);
                fw.newLine();
            }
        }

        return PassLoader.loadAll(con, validBatch);
    }
}

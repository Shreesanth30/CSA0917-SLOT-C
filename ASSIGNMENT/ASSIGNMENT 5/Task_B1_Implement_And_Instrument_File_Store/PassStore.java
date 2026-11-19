package part2_persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PassStore {
    private final Path file;

    public PassStore(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name, int route, double amount)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name,
                    String.valueOf(route), String.valueOf(amount)));
            w.newLine();
        }
    }

    /** Returns the five fields of the pass with the given ID, or null if not found. */
    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) {
            return null;
        }
        // Completed TODO 1: Read line by line with BufferedReader inside try-with-resources;
        // skip damaged lines (not strictly 5 fields); return fields of matching pass
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    // Skip damaged / truncated line safely without aborting search
                    continue;
                }
                if (fields[0].trim().equals(passId)) {
                    return fields;
                }
            }
        }
        return null;
    }

    /** Lists all valid pass records assigned to a given route number. */
    public List<String[]> listByRoute(int routeNo) throws IOException {
        List<String[]> matches = new ArrayList<>();
        if (!Files.exists(file)) {
            return matches;
        }
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    continue;
                }
                try {
                    if (Integer.parseInt(fields[3].trim()) == routeNo) {
                        matches.add(fields);
                    }
                } catch (NumberFormatException ignored) {
                    // Skip corrupted route number field
                }
            }
        }
        return matches;
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(f);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);

        System.out.println(Arrays.toString(store.find("P1003")));
        System.out.println(Arrays.toString(store.find("P1002")));
        System.out.println(Arrays.toString(store.find("P9999")));
        System.out.println("Route 12 matches: " + store.listByRoute(12).size());
        Files.delete(f);
    }
}

import java.io.*;
import java.util.*;

public class DailySalesReport {
    // Version A: Manual try-catch-finally that leaks earlier readers if a later file fails or close throws
    public static int generateManualLeaky(List<String> storeFiles, String outPath) throws IOException {
        List<BufferedReader> openedReaders = new ArrayList<>();
        BufferedWriter writer = null;
        int openHandlesAtFailure = 0;
        try {
            writer = new BufferedWriter(new FileWriter(outPath));
            double total = 0.0;
            for (String path : storeFiles) {
                BufferedReader br = new BufferedReader(new FileReader(path));
                openedReaders.add(br);
                String line = br.readLine();
                if (line == null || line.startsWith("CORRUPT")) {
                    openHandlesAtFailure = openedReaders.size() + 1; // +1 for writer
                    throw new IOException("Corrupt sales record in " + path + " (open handles=" + openHandlesAtFailure + ")");
                }
                total += Double.parseDouble(line.trim());
            }
            writer.write("TOTAL_SALES=" + total);
            return 0;
        } finally {
            // Buggy manual cleanup: only closes writer, forgets openedReaders loop (classic resource leak)
            if (writer != null) {
                writer.close();
            }
        }
    }

    // Version B: Safe try-with-resources closing in reverse order of acquisition
    public static double generateSafeTryWithResources(List<String> storeFiles, String outPath) throws IOException {
        double total = 0.0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
            for (String path : storeFiles) {
                try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                    String line = br.readLine();
                    if (line == null || line.startsWith("CORRUPT")) {
                        throw new IOException("Corrupt sales record in " + path);
                    }
                    total += Double.parseDouble(line.trim());
                } // br automatically closed here even if exception is thrown
            }
            writer.write("TOTAL_SALES=" + total);
        } // writer automatically closed here
        return total;
    }

    public static void main(String[] args) throws IOException {
        List<String> validFiles = List.of("store1.txt", "store2.txt", "store3.txt");
        try (FileWriter f1 = new FileWriter("store1.txt");
             FileWriter f2 = new FileWriter("store2.txt");
             FileWriter f3 = new FileWriter("store3.txt")) {
            f1.write("12500.50");
            f2.write("18400.00");
            f3.write("9100.25");
        }

        double total = generateSafeTryWithResources(validFiles, "summary.txt");
        System.out.println("Safe report total: " + total);

        try (FileWriter f3Bad = new FileWriter("store3.txt")) {
            f3Bad.write("CORRUPT_DATA");
        }
        try {
            generateManualLeaky(validFiles, "summary_fail.txt");
        } catch (IOException e) {
            System.out.println("Manual version failed with leaked handles: " + e.getMessage());
        }
        try {
            generateSafeTryWithResources(validFiles, "summary_safe_fail.txt");
        } catch (IOException e) {
            System.out.println("Try-with-resources failed cleanly (0 leaked handles): " + e.getMessage());
        }

        for (String fn : List.of("store1.txt", "store2.txt", "store3.txt", "summary.txt", "summary_fail.txt", "summary_safe_fail.txt")) {
            new File(fn).delete();
        }
    }
}

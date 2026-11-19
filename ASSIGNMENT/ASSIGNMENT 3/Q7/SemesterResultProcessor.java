import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class SemesterResultProcessor {
    static class StudentResult {
        final String regNo;
        final int totalMarks;
        final String grade;

        StudentResult(String regNo, int totalMarks, String grade) {
            this.regNo = regNo;
            this.totalMarks = totalMarks;
            this.grade = grade;
        }
    }

    static class FileProcessException extends Exception {
        FileProcessException(String msg) { super(msg); }
        FileProcessException(String msg, Throwable cause) { super(msg, cause); }
    }

    static class BatchSummary {
        final ConcurrentHashMap<String, StudentResult> validResults = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, String> failedFiles = new ConcurrentHashMap<>();
        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);
    }

    static String computeGrade(int marks) {
        if (marks >= 90) return "S";
        if (marks >= 80) return "A";
        if (marks >= 70) return "B";
        if (marks >= 60) return "C";
        if (marks >= 50) return "D";
        return "F";
    }

    static StudentResult parseAndValidateFile(Path filePath) throws FileProcessException {
        if (!Files.exists(filePath)) {
            throw new FileProcessException("Missing score file: " + filePath.getFileName());
        }
        try {
            List<String> lines = Files.readAllLines(filePath);
            if (lines.isEmpty()) {
                throw new FileProcessException("Empty score file: " + filePath.getFileName());
            }
            // Expected format: REGNO,M1,M2,M3,M4,M5
            String[] parts = lines.get(0).trim().split(",");
            if (parts.length != 6) {
                throw new FileProcessException("Malformed record (expected 6 fields): " + lines.get(0));
            }
            String regNo = parts[0].trim();
            int sum = 0;
            for (int i = 1; i <= 5; i++) {
                int m = Integer.parseInt(parts[i].trim());
                if (m < 0 || m > 100) {
                    throw new FileProcessException("Out-of-range mark (" + m + ") for " + regNo);
                }
                sum += m;
            }
            int avg = sum / 5;
            return new StudentResult(regNo, avg, computeGrade(avg));
        } catch (NumberFormatException e) {
            throw new FileProcessException("Corrupt numeric mark in " + filePath.getFileName(), e);
        } catch (IOException e) {
            throw new FileProcessException("I/O read error in " + filePath.getFileName(), e);
        }
    }

    public static BatchSummary processBatch(List<Path> files, int poolSize) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        BatchSummary summary = new BatchSummary();
        List<Callable<Boolean>> tasks = new ArrayList<>(files.size());

        for (Path p : files) {
            tasks.add(() -> {
                try {
                    StudentResult res = parseAndValidateFile(p);
                    summary.validResults.put(res.regNo, res);
                    summary.successCount.incrementAndGet();
                    return true;
                } catch (FileProcessException e) {
                    summary.failedFiles.put(p.getFileName().toString(), e.getMessage());
                    summary.failureCount.incrementAndGet();
                    return false;
                }
            });
        }

        List<Future<Boolean>> futures = executor.invokeAll(tasks);
        for (Future<Boolean> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                summary.failureCount.incrementAndGet();
            }
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        return summary;
    }

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("sem_results_test");
        List<Path> testFiles = new ArrayList<>();

        // Create 1,000 test files (with injected missing & corrupt records)
        int totalFiles = 1000;
        for (int i = 0; i < totalFiles; i++) {
            Path fp = tempDir.resolve("score_" + i + ".csv");
            testFiles.add(fp);
            if (i % 50 == 13) {
                // Leave file missing intentionally
                continue;
            } else if (i % 50 == 27) {
                Files.writeString(fp, "19252" + i + ",88,CORRUPT,76,90,85");
            } else if (i % 50 == 41) {
                Files.writeString(fp, "19252" + i + ",95,108,82,79,91"); // out of range >100
            } else {
                Files.writeString(fp, "19252" + i + ",85,90,78,88,92");
            }
        }

        System.out.println("=== Q7: Fault-Tolerant Concurrent Semester Result Processor ===");
        int[] pools = {1, 2, 4, 8};
        for (int p : pools) {
            long t0 = System.nanoTime();
            BatchSummary sum = processBatch(testFiles, p);
            long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
            System.out.printf("PoolSize=%d | Total=%d | Success=%d | Failed=%d | Time=%d ms%n",
                    p, totalFiles, sum.successCount.get(), sum.failureCount.get(), elapsedMs);
        }

        // Cleanup temp directory
        try (var stream = Files.walk(tempDir)) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }
}

package part2_persistence;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class LineCounterBenchmark {
    public static long countLinesUnbuffered(Path path) throws IOException {
        long lines = 0;
        try (InputStream in = new FileInputStream(path.toFile())) {
            int b;
            while ((b = in.read()) != -1) {
                if (b == '\n') {
                    lines++;
                }
            }
        }
        return lines;
    }

    public static long countLinesBuffered(Path path) throws IOException {
        long lines = 0;
        try (InputStream in = new BufferedInputStream(new FileInputStream(path.toFile()), 8192)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                for (int i = 0; i < n; i++) {
                    if (buf[i] == '\n') {
                        lines++;
                    }
                }
            }
        }
        return lines;
    }
}

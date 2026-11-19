# TASK A2 — DON'T READ WHAT YOU DON'T NEED (5 Marks)

## A2.1 — Predict the Cost (2 Marks)
- A 50 MB file equals $50 \times 1,048,576 = 52,428,800$ bytes.
- Calling `FileInputStream.read()` one byte at a time executes **52,428,801 native `read()` system calls** (52,428,800 bytes + 1 EOF call).
- Each call transitions between JVM user space and OS kernel space (~1–2 µs context-switch overhead), turning a ~150 ms sequential disk read into >100 seconds of syscall overhead.

## A2.2 — Redesign Using Buffering (2 Marks)
```text
ALGORITHM CountLinesBuffered(filePath)
INPUT: filePath (String)
OUTPUT: totalLines (Long Integer)
BEGIN
    DECLARE totalLines <- 0
    DECLARE buffer <- BYTE_ARRAY[8192]
    OPEN stream AS BufferedInputStream(FileInputStream(filePath), 8192)
    TRY
        WHILE (bytesRead <- stream.read(buffer)) != -1 DO
            FOR i <- 0 TO bytesRead - 1 DO
                IF buffer[i] = NEWLINE_BYTE THEN
                    totalLines <- totalLines + 1
                END IF
            END FOR
        END WHILE
    FINALLY
        CLOSE stream
    END TRY
    RETURN totalLines
END
```
- **Syscall Reduction:** Reduces OS `read()` calls from 52,428,801 down to $\lceil 52,428,800 / 8,192 \rceil + 1 = 6,401$ calls (an **8,192x reduction**).

## A2.3 — When Buffering Does Not Help (1 Mark)
- **Random-access small reads** (seeking to scattered offsets across a huge file, which invalidates the read-ahead buffer on every jump) or reading an **in-memory `ByteArrayInputStream`**.

## Alternative Concept & Complexity Analysis (`Files.readAllLines()`)
- **Time Complexity:** Both run in $O(N)$ linear time relative to file size $N$.
- **Memory Complexity:** `Files.readAllLines()` requires $O(N)$ heap RAM (~120–150 MB for a 50 MB file due to UTF-16 `String` objects), risking `OutOfMemoryError` on huge logs, whereas a buffered stream uses $O(B)$ constant memory (8 KB).

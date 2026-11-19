# TASK A1 — DESIGN A FILE FORMAT (7 Marks)
*(Follows SIMATS Engineering Standard for Algorithmic Pseudocode)*

## A1.1 — Represent the Record (2 Marks)
1. **Delimited Text File (`passes.csv`, UTF-8):**
   - **Field Order:** `pass_id,reg_no,name,route_no,boarding_stop,validity_date,amount_paid`
   - **Delimiter & Escaping:** Comma (`,`) delimiter with newline (`\n`) record terminator. Fields containing commas or quotes are enclosed in double quotes (`"..."`) per RFC 4180 (e.g., `P1004,192311004,"Karthik, R.",12,Chromepet,2026-12-31,4500.00`).
2. **Binary File (`passes.dat` via `DataOutputStream`):**
   - `writeUTF(passId)` [2B length + UTF-8 bytes]
   - `writeUTF(regNo)` [2B length + UTF-8 bytes]
   - `writeUTF(name)` [2B length + UTF-8 bytes]
   - `writeInt(routeNo)` [4B big-endian integer]
   - `writeUTF(boardingStop)` [2B length + UTF-8 bytes]
   - `writeUTF(validityDate)` [12B length-prefixed ISO date]
   - `writeDouble(amountPaid)` [8B IEEE-754 double]
   - Because `writeUTF` stores exact byte lengths, embedded commas in names or stops never cause delimiter collisions.

## A1.2 — Design the Operations (3 Marks)
```text
ALGORITHM AddPass(filePath, passRecord)
INPUT: filePath (String), passRecord (Record with 7 fields)
OUTPUT: Appends formatted record to text file
BEGIN
    OPEN writer AS BufferedWriter(FileWriter(filePath, APPEND = TRUE, CHARSET = UTF-8))
    TRY
        DECLARE escapedName <- EscapeCsvField(passRecord.name)
        DECLARE escapedStop <- EscapeCsvField(passRecord.boardingStop)
        DECLARE line <- CONCATENATE(passRecord.passId, ",", passRecord.regNo, ",",
                                    escapedName, ",", passRecord.routeNo, ",",
                                    escapedStop, ",", passRecord.validityDate, ",",
                                    passRecord.amountPaid)
        WRITE line TO writer
        WRITE NEWLINE TO writer
        FLUSH writer
    FINALLY
        CLOSE writer
    END TRY
END

ALGORITHM FindPassById(filePath, targetPassId)
INPUT: filePath (String), targetPassId (String)
OUTPUT: Matching PassRecord or NULL
BEGIN
    IF NOT FILE_EXISTS(filePath) THEN
        RETURN NULL
    END IF
    OPEN reader AS BufferedReader(FileReader(filePath, CHARSET = UTF-8))
    TRY
        WHILE (line <- READLINE(reader)) != NULL DO
            DECLARE fields <- ParseCsvLine(line)
            IF LENGTH(fields) != 7 THEN
                LOG_WARNING("Damaged line skipped: " + line)
                CONTINUE
            END IF
            IF fields[0] = targetPassId THEN
                RETURN ConstructPassRecord(fields)
            END IF
        END WHILE
    FINALLY
        CLOSE reader
    END TRY
    RETURN NULL
END

ALGORITHM ListPassesByRoute(filePath, targetRouteNo)
INPUT: filePath (String), targetRouteNo (Integer)
OUTPUT: List of PassRecord objects assigned to targetRouteNo
BEGIN
    DECLARE resultList <- EMPTY_LIST
    IF NOT FILE_EXISTS(filePath) THEN
        RETURN resultList
    END IF
    OPEN reader AS BufferedReader(FileReader(filePath, CHARSET = UTF-8))
    TRY
        WHILE (line <- READLINE(reader)) != NULL DO
            DECLARE fields <- ParseCsvLine(line)
            IF LENGTH(fields) = 7 AND IS_INTEGER(fields[3]) THEN
                IF TO_INTEGER(fields[3]) = targetRouteNo THEN
                    APPEND ConstructPassRecord(fields) TO resultList
                END IF
            END IF
        END WHILE
    FINALLY
        CLOSE reader
    END TRY
    RETURN resultList
END
```
- **Stream Justification:** `BufferedReader` / `BufferedWriter` wrap `FileReader` / `FileWriter` with an 8 KB character buffer, avoiding per-character OS syscalls and decoding UTF-8 text cleanly into lines (unlike byte-oriented `FileInputStream`/`FileOutputStream`).

## A1.3 — Design for Damaged Data (2 Marks)
- **Damaged Line:** Detected via `LENGTH(fields) != 7` or invalid numeric/date parse; logged and skipped without halting the scan.
- **Missing File:** `FindPassById` returns `NULL` and `ListPassesByRoute` returns an empty list; `AddPass` creates the file automatically.
- **Incomplete Write (Power Failure):** A truncated trailing line lacks 7 valid fields and is safely skipped on read. For batch updates, writing to a temporary file, calling `fsync()`, and executing `Files.move(..., ATOMIC_MOVE)` guarantees all-or-nothing durability.

## Alternative Concept & Complexity Analysis (`RandomAccessFile`)
- **Lookup Time:** Seeking to byte offset `i * recordSize` runs in $O(1)$ time when index `i` is known, versus $O(n)$ sequential line scan in a delimited text file.
- **Space Wasted:** Fixed-width padding (e.g., 137 bytes/record vs. ~55 bytes average CSV line) wastes ~55%–60% of disk storage.

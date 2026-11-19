# TASK B3 — STATEMENT vs PREPAREDSTATEMENT vs BATCH (8 Marks)

## B3.1 — Correctness on Small Data (10 Records + `ResultSetMetaData`)
- Verified all 5 columns (`pass_id VARCHAR(16)`, `reg_no VARCHAR(20)`, `name VARCHAR(100)`, `route_no INT`, `amount DECIMAL(10,2)`) across all three insert modes.

## B3.2 — Measured Execution Times (1,000, 10,000, 100,000 Records)
| Dataset Size | (a) `Statement` (`autoCommit`) | (b) `PreparedStatement` | (c) `PreparedStatement` Batch (1,000/batch) |
| :--- | :---: | :---: | :---: |
| **1,000 rows** | 20.43 ms | 3.07 ms | 2.34 ms |
| **10,000 rows** | 173.83 ms | 29.76 ms | 23.49 ms |
| **100,000 rows** | 1,754.47 ms | 310.18 ms | 330.73 ms (100 network batches vs 100k round-trips) |

## B3.3 — Auto-Commit vs. Single Transaction Half-Way Failure (Row 501 Invalid Amount)
- **`autoCommit = true` (500-row batches):** First 500 rows committed permanently; row 501 failed $\rightarrow$ **500 partial rows left in DB (data corruption)**.
- **Single Transaction (`PassLoader.loadAll`):** Row 501 triggered `con.rollback()` $\rightarrow$ **0 rows remaining in DB (100% atomic)**.

## B3.4 — Engineering Trade-Off & Alternative Analysis
- **(a) Nightly import of 100,000 passes:** `PreparedStatement` with `addBatch()`/`executeBatch()` (batch size 1,000) inside a single transaction (`autoCommit = false`).
- **(b) Single pass at counter:** Single `PreparedStatement.executeUpdate()` with `autoCommit = true` for immediate durability and lowest single-row latency.
- **Alternative (`Multi-row INSERT` vs `LOAD DATA INFILE`):** Multi-row `INSERT` buffers $b$ rows in $O(b \cdot L)$ SQL string memory, whereas `LOAD DATA INFILE` streams rows directly into MySQL in $O(1)$ buffer memory.

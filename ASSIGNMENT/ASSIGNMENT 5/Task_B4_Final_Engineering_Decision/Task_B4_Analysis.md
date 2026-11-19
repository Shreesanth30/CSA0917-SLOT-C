# TASK B4 — FINAL ENGINEERING DECISION (5 Marks)

## B4.1 & B4.2 — Practical Migration & `DatabaseMetaData` Portability
- `CampusMigration.java` validates every input line before adding it to the batch, logs rejected lines to `migration_failures.log`, loads valid rows atomically via `PassLoader.loadAll()`, and inspects `DatabaseMetaData` (`getDatabaseProductName()`, `supportsBatchUpdates()`, `supportsTransactions()`).

## B4.3 — Production Decision (12 Campuses, 100x Scale, Concurrent Counters)
- **Decision:** Centralized Relational Database (MySQL/PostgreSQL via JDBC connection pool).
- **Justification:** At 800,000 passes/year and concurrent counters across 12 campuses, text files and Java serialization suffer file-locking contention and $O(n)$ full-file scan latency (~540 ms per lookup), whereas an indexed database provides ACID transactions, row-level concurrency, and $O(\log n)$ B-Tree primary key lookups (< 1 ms).
- **Alternative (12 Separate Serialized Files):** Cross-campus lookup requires deserializing all 12 files ($O(N)$ time and $O(N)$ RAM) and cannot coordinate concurrent counter writes safely.

# TASK A3 — DISCOVER THE DATA-ACCESS DESIGN PATTERN (5 Marks)

## A3.1 — Identify the Common Pattern (2 Marks)
- **Repeated (Invariant) Steps:** Acquiring a JDBC `Connection`, preparing a `PreparedStatement`, executing the SQL command, iterating `ResultSet`, and closing resources via try-with-resources.
- **Varying Parts:** Target SQL table name, column list, bound parameter types, `ResultSet`-to-POJO mapping logic, and primary key type `K`.

## A3.2 — Generic `Dao<T, K>` Interface & `StudentDao` (2 Marks)
- Implemented in `StudentDaoDemo.java` with `insert(T)`, `findById(K)`, `findAll()`, `update(T)`, and `delete(K)`.

## A3.3 — Map Objects to Tables (1 Mark)
| `Student` Java Field | Java Type | `student` SQL Column | SQL Type & Constraint |
| :--- | :--- | :--- | :--- |
| `regNo` | `String` | `reg_no` | `VARCHAR(15) PRIMARY KEY` |
| `fullName` | `String` | `full_name` | `VARCHAR(100) NOT NULL` |
| `branch` | `String` | `branch` | `VARCHAR(50) NOT NULL` |
| `semester` | `int` | `semester` | `INT NOT NULL` |
| `cgpa` | `double` | `cgpa` | `DECIMAL(4,2) NOT NULL` |

## Alternative Concept & Complexity Analysis
- Writing SQL directly inside GUI/service classes requires $O(M)$ scattered edits across $M$ classes whenever a column is renamed, whereas `Dao<T, K>` isolates schema changes to $O(1)$ class with identical $O(1)$ runtime operation cost.

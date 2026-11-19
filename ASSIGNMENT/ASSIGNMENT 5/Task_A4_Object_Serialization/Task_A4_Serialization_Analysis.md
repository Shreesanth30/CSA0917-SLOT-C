# TASK A4 — WHAT SHOULD BE SERIALIZED? (4 Marks)
1. **Runtime Failure (`NotSerializableException`):** `Resident` holds a reference to a live `java.sql.Connection`, which wraps native OS network sockets and cannot be serialized. Attempting `ObjectOutputStream.writeObject(hostel)` throws `java.io.NotSerializableException`.
2. **Security Vulnerability:** Serializing `Resident.password` writes plaintext credentials onto disk, violating credential security policies.
3. **Engineering Fix:** Declare `private transient Connection dbCon;` and `private transient String password;`, define `private static final long serialVersionUID = 1L;`, and re-inject a fresh pooled `Connection` after deserialization.

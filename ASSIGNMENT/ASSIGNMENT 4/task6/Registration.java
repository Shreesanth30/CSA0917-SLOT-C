package task6;

import java.sql.Timestamp;

public class Registration {
    private final int regId;
    private final String studentId;
    private final String studentName;
    private final String email;
    private final String department;
    private final String eventName;
    private final Timestamp registeredAt;

    public Registration(int regId, String studentId, String studentName,
                        String email, String department, String eventName, Timestamp registeredAt) {
        this.regId = regId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.department = department;
        this.eventName = eventName;
        this.registeredAt = registeredAt;
    }

    public int getRegId() { return regId; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public String getEventName() { return eventName; }
    public Timestamp getRegisteredAt() { return registeredAt; }
}

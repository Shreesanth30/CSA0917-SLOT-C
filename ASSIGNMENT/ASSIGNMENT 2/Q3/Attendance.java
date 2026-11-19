import java.util.*;

class Student {
    private final String regNo;
    private final String name;

    Student(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        return regNo.equals(((Student) o).regNo);
    }

    // TODO 1: Override hashCode() so that it is consistent with equals()
    @Override
    public int hashCode() {
        return Objects.hashCode(regNo);
    }

    @Override
    public String toString() {
        return regNo + " " + name;
    }
}

class BrokenStudent {
    final String regNo;
    final String name;

    BrokenStudent(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BrokenStudent)) return false;
        return regNo.equals(((BrokenStudent) o).regNo);
    }
    // Intentionally omits hashCode() to reproduce the bug before TODO 1
}

public class Attendance {
    public static void main(String[] args) {
        // Required Complete the Code check
        Set<Student> present = new HashSet<>();
        present.add(new Student("192311001", "Arun")); // scanned at Gate 1
        present.add(new Student("192311002", "Divya"));
        present.add(new Student("192311001", "Arun")); // scanned again at Gate 3
        System.out.println("Unique students: " + present.size());

        // Experiment 1: Without hashCode() (broken contract)
        Set<BrokenStudent> brokenSet = new HashSet<>();
        brokenSet.add(new BrokenStudent("192524478", "Sam"));
        brokenSet.add(new BrokenStudent("192524478", "Sam"));
        brokenSet.add(new BrokenStudent("192524479", "Kiran"));
        System.out.println("Experiment 1 (Broken hashCode, duplicate scan): size = " + brokenSet.size() + " (Expected buggy: 3)");

        // Experiment 2: Distinct students with valid contract
        Set<Student> validSet = new HashSet<>();
        validSet.add(new Student("192524478", "Sam"));
        validSet.add(new Student("192524479", "Kiran"));
        validSet.add(new Student("192524478", "Sam Gate 2"));
        System.out.println("Experiment 2 (Fixed hashCode, multi-gate scan): size = " + validSet.size() + " (Expected: 2)");

        // Experiment 3: TreeSet with Comparator comparing name only (inconsistent with equals on regNo)
        Set<Student> inconsistentTree = new TreeSet<>(Comparator.comparing(s -> s.toString().split(" ")[1]));
        inconsistentTree.add(new Student("101", "Arun"));
        inconsistentTree.add(new Student("102", "Arun")); // same name, different regNo -> treated as equal by TreeSet!
        System.out.println("Experiment 3 (TreeSet name-only comparator): size = " + inconsistentTree.size() + " (Expected: 1)");
    }
}

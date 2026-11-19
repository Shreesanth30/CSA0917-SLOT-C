import java.util.*;

class Patient {
    final String name;
    final int severity;
    final long arrival;

    Patient(String name, int severity, long arrival) {
        this.name = name;
        this.severity = severity;
        this.arrival = arrival;
    }

    @Override
    public String toString() {
        return name + "(S" + severity + ")";
    }
}

public class Triage {
    private long counter = 0;

    // TODO 1: Create a PriorityQueue that serves the lowest severity number first and,
    // for equal severity, the earliest arrival first
    private final PriorityQueue<Patient> queue = new PriorityQueue<>(
            Comparator.comparingInt((Patient p) -> p.severity)
                    .thenComparingLong(p -> p.arrival)
    );

    void admit(String name, int severity) {
        queue.add(new Patient(name, severity, counter++));
    }

    Patient next() {
        return queue.poll();
    }

    public static void main(String[] args) {
        Triage er = new Triage();
        er.admit("Ravi", 3);
        er.admit("Meena", 1);
        er.admit("John", 3);
        er.admit("Asha", 5);
        er.admit("Kumar", 1);
        er.admit("Lata", 2);

        StringBuilder order = new StringBuilder();
        Patient p;
        while ((p = er.next()) != null) {
            order.append(p).append(" ");
        }
        System.out.println(order.toString().trim());

        // Demonstration: Direct PriorityQueue iteration is NOT sorted order (heap order only)
        Triage heapDemo = new Triage();
        heapDemo.admit("Ravi", 3);
        heapDemo.admit("Meena", 1);
        heapDemo.admit("John", 3);
        heapDemo.admit("Asha", 5);
        heapDemo.admit("Kumar", 1);
        heapDemo.admit("Lata", 2);
        System.out.println("Direct heap iteration (not treatment order): " + heapDemo.queue);
    }
}

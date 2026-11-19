package partA_design;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Task A4: Safe Object Serialization for Hostel -> Room -> Resident graph.
 * Marks live Connection and plaintext password as transient.
 */
public class HostelSerializationDemo {
    public static class Resident implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final String regNo;
        private transient String password;   // Security: never serialize plaintext credentials
        private transient Connection dbCon;  // Runtime resource: prevents NotSerializableException

        public Resident(String name, String regNo, String password, Connection dbCon) {
            this.name = name;
            this.regNo = regNo;
            this.password = password;
            this.dbCon = dbCon;
        }

        public void reattachConnection(Connection newCon) {
            this.dbCon = newCon;
        }

        @Override
        public String toString() {
            return "Resident{name='" + name + "', regNo='" + regNo +
                   "', password=" + password + ", dbCon=" + dbCon + "}";
        }
    }

    public static class Room implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int roomNo;
        private final List<Resident> residents = new ArrayList<>();
        public Room(int roomNo) { this.roomNo = roomNo; }
        public void addResident(Resident r) { residents.add(r); }
        public List<Resident> getResidents() { return residents; }
    }

    public static class Hostel implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String hostelName;
        private final List<Room> rooms = new ArrayList<>();
        public Hostel(String hostelName) { this.hostelName = hostelName; }
        public void addRoom(Room r) { rooms.add(r); }
        public List<Room> getRooms() { return rooms; }
    }

    public static void main(String[] args) throws Exception {
        Hostel h = new Hostel("Kaveri Block");
        Room r101 = new Room(101);
        r101.addResident(new Resident("Arun Kumar", "192311001", "PlainSecret123", null));
        h.addRoom(r101);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(h);
        }

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            Hostel restored = (Hostel) ois.readObject();
            System.out.println("Deserialized safely: " + restored.getRooms().get(0).getResidents().get(0));
        }
    }
}

public class TicketConstructorDemo {
    static class ParkingTicket {
        private final String ticketId;
        private final String vehicleRegNo;
        private final int floor;
        private final int slot;
        private final long entryMinute;
        private final boolean duplicate;

        // Primary constructor
        public ParkingTicket(String ticketId, String vehicleRegNo, int floor, int slot, long entryMinute, boolean duplicate) {
            this.ticketId = ticketId;
            this.vehicleRegNo = vehicleRegNo;
            this.floor = floor;
            this.slot = slot;
            this.entryMinute = entryMinute;
            this.duplicate = duplicate;
        }

        // Overloaded constructor chaining via this(...)
        public ParkingTicket(String ticketId, String vehicleRegNo, int floor, int slot, long entryMinute) {
            this(ticketId, vehicleRegNo, floor, slot, entryMinute, false);
        }

        // Copy constructor for issuing duplicate receipt without mutating original
        public ParkingTicket(ParkingTicket original, String duplicateId) {
            this(duplicateId, original.vehicleRegNo, original.floor, original.slot, original.entryMinute, true);
        }

        @Override
        public String toString() {
            return String.format("Ticket[%s | Reg=%s | F%d-S%d | Entry=%d | Dup=%s]",
                    ticketId, vehicleRegNo, floor, slot, entryMinute, duplicate);
        }
    }

    public static void main(String[] args) {
        ParkingTicket t1 = new ParkingTicket("TKT-1001", "TN01AB1111", 0, 1, 600);
        ParkingTicket t1Dup = new ParkingTicket(t1, "TKT-1001-DUP");
        System.out.println("=== Task 04: Constructor Chaining & Copy Constructor ===");
        System.out.println("Original : " + t1);
        System.out.println("Duplicate: " + t1Dup);
    }
}

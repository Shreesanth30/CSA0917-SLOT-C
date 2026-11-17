import java.util.*;

public class CampusParkingSystem {

    enum UserCategory { STUDENT, STAFF, VISITOR }

    static class ParkingTicket {
        private static int seq = 5000;
        private final String ticketNo;
        private final String regNo;
        private final String vehicleType;
        private final UserCategory userCategory;
        private final boolean hasValidPass;
        private final int floor;
        private final int slot;
        private final int entryMinute;
        private final int kWhUsed;

        public ParkingTicket(String regNo, String vehicleType, UserCategory userCategory,
                             boolean hasValidPass, int floor, int slot, int entryMinute, int kWhUsed) {
            if (entryMinute < 0) throw new IllegalArgumentException("Entry minute cannot be negative");
            if (floor < 0 || slot < 0) throw new IllegalArgumentException("Invalid slot coordinates");
            this.ticketNo = "SIMATS-" + (++seq);
            this.regNo = regNo;
            this.vehicleType = vehicleType;
            this.userCategory = userCategory;
            this.hasValidPass = hasValidPass;
            this.floor = floor;
            this.slot = slot;
            this.entryMinute = entryMinute;
            this.kWhUsed = kWhUsed;
        }

        public String getTicketNo() { return ticketNo; }
        public String getRegNo() { return regNo; }
        public String getVehicleType() { return vehicleType; }
        public UserCategory getUserCategory() { return userCategory; }
        public boolean isHasValidPass() { return hasValidPass; }
        public int getFloor() { return floor; }
        public int getSlot() { return slot; }
        public int getEntryMinute() { return entryMinute; }
        public int getKWhUsed() { return kWhUsed; }
    }

    private final String[][] slots;
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();
    private double totalDailyRevenue = 0.0;

    public CampusParkingSystem(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new String[slotsPerFloor[f]];
        }
    }

    public String enterVehicle(String regNo, String vehicleType, UserCategory user,
                               boolean hasPass, int entryMinute, int kWh) {
        if (!Set.of("TWO_WHEELER", "CAR", "ELECTRIC_CAR").contains(vehicleType)) {
            return "[REJECT] Unknown vehicle type: " + vehicleType;
        }
        if (activeTickets.containsKey(regNo)) {
            return "[REJECT] Duplicate registration number already parked: " + regNo;
        }
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] == null) {
                    slots[f][s] = regNo;
                    ParkingTicket t = new ParkingTicket(regNo, vehicleType, user, hasPass, f, s, entryMinute, kWh);
                    activeTickets.put(regNo, t);
                    return String.format("[ENTRY] %s (%s, %s) -> Ticket %s at [F%d, S%d]",
                            regNo, vehicleType, user, t.getTicketNo(), f, s);
                }
            }
        }
        return "[REJECT] Facility Full for " + regNo;
    }

    public String exitVehicle(String regNo, int exitMinute) {
        ParkingTicket t = activeTickets.get(regNo);
        if (t == null) {
            return "[REJECT] Vehicle not found in active registry: " + regNo;
        }
        if (exitMinute < t.getEntryMinute()) {
            return "[REJECT] Invalid exit time before entry time for " + regNo;
        }
        int duration = exitMinute - t.getEntryMinute();
        int billableHours = (duration == 0) ? 0 : (duration + 59) / 60;

        double hourlyRate = switch (t.getVehicleType()) {
            case "TWO_WHEELER" -> 10.0;
            case "CAR", "ELECTRIC_CAR" -> 30.0;
            default -> 0.0;
        };

        double parkingFee = (t.getUserCategory() != UserCategory.VISITOR && t.isHasValidPass())
                ? 0.0
                : billableHours * hourlyRate;

        // EV charging is charged separately even if pass holder
        double chargingFee = t.getVehicleType().equals("ELECTRIC_CAR") ? (t.getKWhUsed() * 12.0) : 0.0;
        double totalFee = parkingFee + chargingFee;

        slots[t.getFloor()][t.getSlot()] = null;
        activeTickets.remove(regNo);
        totalDailyRevenue += totalFee;

        return String.format("[EXIT]  %s | Duration=%3d min (%d hr) | Parking=Rs %5.1f | EV=Rs %5.1f | Total=Rs %5.1f",
                regNo, duration, billableHours, parkingFee, chargingFee, totalFee);
    }

    public static void main(String[] args) {
        // Small 2-floor facility (2 slots on F0, 1 slot on F1 = 3 total slots)
        CampusParkingSystem campus = new CampusParkingSystem(new int[]{2, 1});

        System.out.println("=== Task 11: Comprehensive System & Boundary Verification ===");
        // 1. Normal + Pass + Boundary cases
        System.out.println(campus.enterVehicle("TN01AA1111", "TWO_WHEELER", UserCategory.STUDENT, true, 500, 0));
        System.out.println(campus.enterVehicle("TN02BB2222", "CAR", UserCategory.VISITOR, false, 510, 0));
        // Last free slot boundary
        System.out.println(campus.enterVehicle("TN03CC3333", "ELECTRIC_CAR", UserCategory.VISITOR, false, 520, 10));

        // 2. Invalid cases
        System.out.println(campus.enterVehicle("TN04DD4444", "CAR", UserCategory.VISITOR, false, 525, 0)); // Full
        System.out.println(campus.enterVehicle("TN02BB2222", "CAR", UserCategory.VISITOR, false, 525, 0)); // Duplicate
        System.out.println(campus.enterVehicle("TN05EE5555", "HELICOPTER", UserCategory.VISITOR, false, 525, 0)); // Unknown

        // 3. Exits: Pass holder (free), Exact 60-min slab boundary, EV with charging
        System.out.println(campus.exitVehicle("TN01AA1111", 620)); // Student pass -> Rs 0
        System.out.println(campus.exitVehicle("TN02BB2222", 570)); // Exact 60 min = 1 hr * 30 = Rs 30.0
        System.out.println(campus.exitVehicle("TN03CC3333", 670)); // 150 min = 3 hr * 30 + 10*12 = 90 + 120 = Rs 210.0

        // 4. Zero-minute boundary stay
        System.out.println(campus.enterVehicle("TN06FF6666", "CAR", UserCategory.VISITOR, false, 700, 0));
        System.out.println(campus.exitVehicle("TN06FF6666", 700)); // 0 min -> Rs 0.0

        System.out.printf("Daily Collection Verified: Rs %.2f (Hand-calculated: 0 + 30 + 210 + 0 = Rs 240.00)%n",
                campus.totalDailyRevenue);
    }
}

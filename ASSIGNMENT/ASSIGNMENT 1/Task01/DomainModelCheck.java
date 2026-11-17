import java.util.*;

public class DomainModelCheck {
    record VehicleRecord(String regNo, String category, long entryEpochMinutes, boolean hasMonthlyPass) {}

    public static void main(String[] args) {
        List<VehicleRecord> active = new ArrayList<>();
        active.add(new VehicleRecord("TN01AB1111", "TWO_WHEELER", 540, true));
        active.add(new VehicleRecord("TN02CD2222", "CAR", 555, false));
        active.add(new VehicleRecord("TN03EV3333", "ELECTRIC_CAR", 600, false));

        System.out.println("=== Task 01: Campus Smart-Parking Domain Objects Verification ===");
        for (VehicleRecord v : active) {
            System.out.printf("RegNo: %-10s | Type: %-12s | EntryMin: %4d | Pass: %-5s%n",
                    v.regNo(), v.category(), v.entryEpochMinutes(), v.hasMonthlyPass());
        }
    }
}

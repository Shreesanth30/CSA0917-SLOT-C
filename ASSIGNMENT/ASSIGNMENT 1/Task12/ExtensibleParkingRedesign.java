import java.time.LocalTime;
import java.util.*;

public class ExtensibleParkingRedesign {

    // Strategy Interface for pluggable pricing rules (Open/Closed Principle)
    @FunctionalInterface
    interface PricingRule {
        double computeFee(int durationMinutes, LocalTime entryTime);
    }

    // Pass Abstraction separating user pass privileges from Vehicle hierarchy
    interface ParkingPass {
        boolean waivesParkingFee();
        String passName();
    }

    static class MonthlyCampusPass implements ParkingPass {
        public boolean waivesParkingFee() { return true; }
        public String passName() { return "MONTHLY_CAMPUS_PASS"; }
    }

    static class OneDayVisitorPass implements ParkingPass {
        public boolean waivesParkingFee() { return true; }
        public String passName() { return "ONE_DAY_VISITOR_PASS"; }
    }

    static class NoPass implements ParkingPass {
        public boolean waivesParkingFee() { return false; }
        public String passName() { return "NO_PASS"; }
    }

    // Vehicle Category decoupled from rigid subclass explosion via PricingRule composition
    static class VehicleCategory {
        private final String name;
        private final PricingRule rule;
        private final double evRatePerKwh;

        public VehicleCategory(String name, PricingRule rule, double evRatePerKwh) {
            this.name = name;
            this.rule = rule;
            this.evRatePerKwh = evRatePerKwh;
        }

        public String getName() { return name; }
        public PricingRule getRule() { return rule; }
        public double getEvRatePerKwh() { return evRatePerKwh; }
    }

    static class Vehicle {
        private final String regNo;
        private final VehicleCategory category;
        private final ParkingPass pass;

        public Vehicle(String regNo, VehicleCategory category, ParkingPass pass) {
            this.regNo = regNo;
            this.category = category;
            this.pass = pass;
        }

        public double calculateTotalFee(int minutes, LocalTime entryTime, int kWh) {
            double base = pass.waivesParkingFee() ? 0.0 : category.getRule().computeFee(minutes, entryTime);
            return base + (kWh * category.getEvRatePerKwh());
        }

        public String getRegNo() { return regNo; }
        public VehicleCategory getCategory() { return category; }
        public ParkingPass getPass() { return pass; }
    }

    public static void main(String[] args) {
        // Jagged 5-floor facility with variable slots per floor
        String[][] jaggedSlots = {
            new String[15], new String[20], new String[25], new String[20], new String[20]
        };

        // Existing & New Categories configured WITHOUT modifying Vehicle class
        VehicleCategory twoWheeler = new VehicleCategory("TWO_WHEELER",
                (mins, t) -> ((mins + 59) / 60) * 10.0, 0.0);
        VehicleCategory car = new VehicleCategory("CAR",
                (mins, t) -> ((mins + 59) / 60) * 30.0, 0.0);
        VehicleCategory electricCar = new VehicleCategory("ELECTRIC_CAR",
                (mins, t) -> ((mins + 59) / 60) * 30.0, 12.0);

        // NEW Category 1: E-Bike (Flat Rs 15 fee regardless of duration)
        VehicleCategory eBike = new VehicleCategory("E_BIKE",
                (mins, t) -> mins <= 0 ? 0.0 : 15.0, 5.0);

        // NEW Category 2: Delivery Van (Double car rate = Rs 60/hr between 09:00 and 17:00, Rs 30/hr off-peak)
        VehicleCategory deliveryVan = new VehicleCategory("DELIVERY_VAN", (mins, entryTime) -> {
            int hrs = (mins <= 0) ? 0 : (mins + 59) / 60;
            boolean peak = !entryTime.isBefore(LocalTime.of(9, 0)) && entryTime.isBefore(LocalTime.of(17, 0));
            return hrs * (peak ? 60.0 : 30.0);
        }, 0.0);

        Vehicle v1 = new Vehicle("TN07EB1001", eBike, new NoPass());
        Vehicle v2 = new Vehicle("TN08DV2002", deliveryVan, new NoPass());
        Vehicle v3 = new Vehicle("TN08DV2003", deliveryVan, new NoPass());
        Vehicle v4 = new Vehicle("TN09VS3003", car, new OneDayVisitorPass());

        System.out.println("=== Task 12: Extensible Redesign Validation (5-Floor Jagged + New Categories) ===");
        System.out.printf("5-Floor Jagged Capacities: %d, %d, %d, %d, %d (Total = 100 slots)%n",
                jaggedSlots[0].length, jaggedSlots[1].length, jaggedSlots[2].length, jaggedSlots[3].length, jaggedSlots[4].length);
        System.out.printf("E-Bike (180 mins, 2 kWh)          -> Rs %.2f (Flat Rs 15 + Rs 10 EV)%n",
                v1.calculateTotalFee(180, LocalTime.of(10, 30), 2));
        System.out.printf("Delivery Van Peak (11:00, 120m)   -> Rs %.2f (2 hrs * Rs 60 double rate)%n",
                v2.calculateTotalFee(120, LocalTime.of(11, 0), 0));
        System.out.printf("Delivery Van Off-Peak (18:30, 120m)-> Rs %.2f (2 hrs * Rs 30 normal rate)%n",
                v3.calculateTotalFee(120, LocalTime.of(18, 30), 0));
        System.out.printf("Visitor with One-Day Pass (240m)  -> Rs %.2f (Pass waives parking fee)%n",
                v4.calculateTotalFee(240, LocalTime.of(14, 0), 0));
    }
}

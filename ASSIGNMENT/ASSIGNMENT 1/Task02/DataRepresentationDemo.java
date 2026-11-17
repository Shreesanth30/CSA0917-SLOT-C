public class DataRepresentationDemo {
    public static void main(String[] args) {
        // Jagged 2D Array representing 3 floors with variable slot counts: 10, 20, 20 (Total 50)
        String[][] slots = new String[3][];
        slots[0] = new String[10];
        slots[1] = new String[20];
        slots[2] = new String[20];

        slots[0][0] = "TN01AB1111";
        slots[2][19] = "TN09XY9999";

        int parkedMinutes = 90;
        double ratePerHour = 30.0;

        // Faulty integer division risk
        int wrongHours = parkedMinutes / 60; // 90 / 60 = 1 (loses 30 mins!)
        double wrongFee = wrongHours * ratePerHour;

        // Safe ceiling integer arithmetic: (minutes + 59) / 60
        long billableHours = (parkedMinutes == 0) ? 0 : (parkedMinutes + 59L) / 60L;
        double exactFee = billableHours * ratePerHour;

        System.out.println("=== Task 02: Jagged Array & Casting Precision Verification ===");
        System.out.printf("Jagged Floor Lengths: F0=%d, F1=%d, F2=%d%n", slots[0].length, slots[1].length, slots[2].length);
        System.out.printf("Duration = %d mins | Faulty int div fee = Rs %.2f | Safe ceiling fee = Rs %.2f%n",
                parkedMinutes, wrongFee, exactFee);
    }
}

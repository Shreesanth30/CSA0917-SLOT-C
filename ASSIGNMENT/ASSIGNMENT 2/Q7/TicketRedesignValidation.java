import java.util.*;

public class TicketRedesignValidation {
    // Original bottleneck: repeated String concatenation inside loop
    public static String originalConcat(List<String> fields) {
        String ticket = "";
        for (String f : fields) {
            ticket = ticket + f + "\n";
        }
        return ticket;
    }

    // Redesigned solution: pre-sized StringBuilder with direct char/String append
    public static String redesignedPreSizedBuilder(List<String> fields) {
        int estimatedChars = 0;
        for (String f : fields) {
            estimatedChars += f.length() + 1;
        }
        StringBuilder sb = new StringBuilder(estimatedChars);
        for (String f : fields) {
            sb.append(f).append('\n');
        }
        return sb.toString();
    }

    static List<String> sampleTicket(int n) {
        List<String> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add("FIELD_" + i + "_VAL_" + (1000 + i));
        }
        return list;
    }

    public static void main(String[] args) {
        int[] fieldCounts = {20, 100, 400};
        int tickets = 5000;
        System.out.println("=== Q7: Original (+) vs Redesigned Pre-Sized StringBuilder ===");
        for (int fc : fieldCounts) {
            List<String> sample = sampleTicket(fc);
            // Warmup
            for (int i = 0; i < 200; i++) {
                originalConcat(sample);
                redesignedPreSizedBuilder(sample);
            }
            long t0 = System.nanoTime();
            long s1 = 0;
            for (int t = 0; t < tickets; t++) s1 += originalConcat(sample).length();
            long origMs = (System.nanoTime() - t0) / 1_000_000L;

            t0 = System.nanoTime();
            long s2 = 0;
            for (int t = 0; t < tickets; t++) s2 += redesignedPreSizedBuilder(sample).length();
            long redesMs = (System.nanoTime() - t0) / 1_000_000L;

            if (s1 != s2) throw new AssertionError("Mismatch!");
            System.out.printf("Fields=%3d | %d tickets | Original(+)=%4d ms | Redesigned(StringBuilder)=%3d ms | Speedup=%.1fx%n",
                    fc, tickets, origMs, redesMs, (double) origMs / Math.max(1L, redesMs));
        }
    }
}

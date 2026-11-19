import java.util.*;

public class PharmacyInventoryBenchmark {
    static class Medicine {
        final String code;
        final String name;
        int stock;

        Medicine(String code, String name, int stock) {
            this.code = code;
            this.name = name;
            this.stock = stock;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Q4: HashMap vs TreeMap vs LinkedHashMap Pharmacy Inventory ===");
        int[] sizes = {500, 5000, 25000};
        for (int n : sizes) {
            Map<String, Medicine> hashMap = new HashMap<>();
            Map<String, Medicine> treeMap = new TreeMap<>();
            Map<String, Medicine> linkedHashMap = new LinkedHashMap<>();

            List<String> keys = new ArrayList<>(n);
            Random rng = new Random(101);
            for (int i = 0; i < n; i++) {
                // Intentionally out-of-code-order insertion
                int codeNum = (i * 7919 + rng.nextInt(500)) % (n * 10 + 1000);
                String code = String.format("MED-%06d", codeNum);
                if (hashMap.containsKey(code)) {
                    code = String.format("MED-%06d-%d", codeNum, i);
                }
                keys.add(code);
                Medicine m = new Medicine(code, "Drug_" + i, 50 + (i % 200));
                hashMap.put(code, m);
                treeMap.put(code, m);
                linkedHashMap.put(code, m);
            }

            int lookups = 100_000;
            String[] queries = new String[lookups];
            for (int i = 0; i < lookups; i++) {
                queries[i] = keys.get(rng.nextInt(n));
            }

            long t0 = System.nanoTime();
            int sink = 0;
            for (String q : queries) sink += hashMap.get(q).stock;
            long hmLookupMs = (System.nanoTime() - t0) / 1_000_000L;

            t0 = System.nanoTime();
            for (String q : queries) sink += treeMap.get(q).stock;
            long tmLookupMs = (System.nanoTime() - t0) / 1_000_000L;

            t0 = System.nanoTime();
            for (String q : queries) sink += linkedHashMap.get(q).stock;
            long lhmLookupMs = (System.nanoTime() - t0) / 1_000_000L;
            if (sink == -1) System.out.print("");

            // Code-order report generation
            t0 = System.nanoTime();
            List<String> hmSorted = new ArrayList<>(hashMap.keySet());
            Collections.sort(hmSorted);
            long hmCodeReportUs = (System.nanoTime() - t0) / 1_000L;

            t0 = System.nanoTime();
            List<String> tmSorted = new ArrayList<>(treeMap.keySet());
            long tmCodeReportUs = (System.nanoTime() - t0) / 1_000L;

            // Arrival-order report generation
            t0 = System.nanoTime();
            List<String> lhmArrival = new ArrayList<>(linkedHashMap.keySet());
            long lhmArrivalReportUs = (System.nanoTime() - t0) / 1_000L;

            if (hmSorted.size() + tmSorted.size() + lhmArrival.size() < 0) System.out.print("");

            System.out.printf("N=%d | 100k Lookups(ms): HM=%d, TM=%d, LHM=%d | Code-Order Report(us): HM_sort=%d, TM_direct=%d | Arrival Report(us): LHM_direct=%d%n",
                    n, hmLookupMs, tmLookupMs, lhmLookupMs, hmCodeReportUs, tmCodeReportUs, lhmArrivalReportUs);
        }
    }
}

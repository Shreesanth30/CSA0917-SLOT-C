import java.util.*;

public class TicketBuilderBenchmark {
    static class TicketData {
        final String pnr;
        final List<String> passengers;
        final String coach;
        final String berth;
        final double fare;
        final String journey;

        TicketData(String pnr, List<String> passengers, String coach, String berth, double fare, String journey) {
            this.pnr = pnr;
            this.passengers = passengers;
            this.coach = coach;
            this.berth = berth;
            this.fare = fare;
            this.journey = journey;
        }
    }

    public static String buildWithString(List<String> fields) {
        String out = "";
        for (String f : fields) {
            out = out + f + "\n";
        }
        return out;
    }

    public static String buildWithStringBuilder(List<String> fields) {
        StringBuilder sb = new StringBuilder(fields.size() * 24);
        for (String f : fields) {
            sb.append(f).append('\n');
        }
        return sb.toString();
    }

    public static String buildWithStringBuffer(List<String> fields) {
        StringBuffer sbuf = new StringBuffer(fields.size() * 24);
        for (String f : fields) {
            sbuf.append(f).append('\n');
        }
        return sbuf.toString();
    }

    public static String buildWithStringJoin(List<String> fields) {
        return String.join("\n", fields) + "\n";
    }

    public static String buildWithStringJoiner(List<String> fields) {
        StringJoiner sj = new StringJoiner("\n", "", "\n");
        for (String f : fields) {
            sj.add(f);
        }
        return sj.toString();
    }

    static List<String> makeFields(int numFields, int ticketIdx) {
        List<String> list = new ArrayList<>(numFields);
        list.add("PNR: " + (8000000000L + ticketIdx));
        list.add("Train: 12627 Karnataka Exp");
        list.add("Route: MAS -> SBC");
        list.add("Coach/Berth: S" + ((ticketIdx % 9) + 1) + "/" + ((ticketIdx % 72) + 1));
        list.add("Fare: INR " + (450 + (ticketIdx % 1200)));
        for (int i = list.size(); i < numFields; i++) {
            list.add("Passenger_" + i + "_Age_" + (20 + ((i + ticketIdx) % 50)) + "_Status_CNF");
        }
        return list;
    }

    static long avgNano(int tickets, int fieldsPerTicket, int mode, int runs) {
        List<List<String>> dataset = new ArrayList<>(tickets);
        for (int t = 0; t < tickets; t++) {
            dataset.add(makeFields(fieldsPerTicket, t));
        }
        // Warmup
        for (int t = 0; t < Math.min(200, tickets); t++) {
            runMode(dataset.get(t), mode);
        }
        long totalNs = 0;
        for (int r = 0; r < runs; r++) {
            long start = System.nanoTime();
            long sink = 0;
            for (int t = 0; t < tickets; t++) {
                String res = runMode(dataset.get(t), mode);
                sink += res.length();
            }
            long end = System.nanoTime();
            if (sink == -1) System.out.print("");
            totalNs += (end - start);
        }
        return totalNs / runs;
    }

    static String runMode(List<String> fields, int mode) {
        switch (mode) {
            case 1: return buildWithString(fields);
            case 2: return buildWithStringBuilder(fields);
            case 3: return buildWithStringBuffer(fields);
            case 4: return buildWithStringJoin(fields);
            default: return buildWithStringJoiner(fields);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Q1: Railway E-Ticket String vs StringBuilder vs StringBuffer ===");
        // Workload A: Short tickets (10 fields), 10,000 tickets
        // Workload B: Medium-large tickets (50 fields), 10,000 tickets
        // Workload C: Heavy tickets (120 fields), 5,000 tickets
        int[][] configs = {
            {10000, 10},
            {10000, 50},
            {5000, 120}
        };
        String[] modes = {"String (+)", "StringBuilder", "StringBuffer", "String.join", "StringJoiner"};
        for (int[] cfg : configs) {
            int nTickets = cfg[0];
            int nFields = cfg[1];
            System.out.printf("%nWorkload: %d tickets x %d fields%n", nTickets, nFields);
            for (int m = 1; m <= 5; m++) {
                long avgMs = avgNano(nTickets, nFields, m, 5) / 1_000_000L;
                long estObjects = (m == 1) ? ((long) nTickets * nFields * 2L) : ((long) nTickets * 3L);
                System.out.printf("  %-15s | Avg Time: %4d ms | Est. Interm. String/Char Objects: ~%d%n",
                        modes[m - 1], avgMs, estObjects);
            }
        }
    }
}

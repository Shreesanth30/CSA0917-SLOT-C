import java.util.*;

public class PlaylistExperiment {
    public static void main(String[] args) {
        System.out.println("=== Q2: ArrayList vs LinkedList vs ArrayDeque Playlist Benchmark ===");
        int[] sizes = {1000, 10000, 50000};
        for (int n : sizes) {
            List<Integer> arrayList = new ArrayList<>();
            List<Integer> linkedList = new LinkedList<>();
            ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

            long t0 = System.nanoTime();
            for (int i = 0; i < n; i++) arrayList.add(i);
            long alAppendMs = (System.nanoTime() - t0) / 1_000_000L;

            t0 = System.nanoTime();
            for (int i = 0; i < n; i++) linkedList.add(i);
            long llAppendMs = (System.nanoTime() - t0) / 1_000_000L;

            t0 = System.nanoTime();
            for (int i = 0; i < n; i++) arrayDeque.addLast(i);
            long adAppendMs = (System.nanoTime() - t0) / 1_000_000L;

            // Random access (get k-th) 5000 times
            Random rng = new Random(42);
            int ops = Math.min(5000, n);
            int[] indices = new int[ops];
            for (int i = 0; i < ops; i++) indices[i] = rng.nextInt(n);

            long sum = 0;
            t0 = System.nanoTime();
            for (int idx : indices) sum += arrayList.get(idx);
            long alGetUs = (System.nanoTime() - t0) / 1_000L;

            t0 = System.nanoTime();
            for (int idx : indices) sum += linkedList.get(idx);
            long llGetUs = (System.nanoTime() - t0) / 1_000L;
            if (sum == -1) System.out.print("");

            // Front insertions (2000 ops)
            int frontOps = Math.min(2000, n);
            List<Integer> alFront = new ArrayList<>(arrayList);
            LinkedList<Integer> llFront = new LinkedList<>(linkedList);
            ArrayDeque<Integer> adFront = new ArrayDeque<>(arrayDeque);

            t0 = System.nanoTime();
            for (int i = 0; i < frontOps; i++) alFront.add(0, -i);
            long alFrontMs = (System.nanoTime() - t0) / 1_000_000L;

            t0 = System.nanoTime();
            for (int i = 0; i < frontOps; i++) llFront.addFirst(-i);
            long llFrontMs = (System.nanoTime() - t0) / 1_000_000L;

            t0 = System.nanoTime();
            for (int i = 0; i < frontOps; i++) adFront.addFirst(-i);
            long adFrontMs = (System.nanoTime() - t0) / 1_000_000L;

            // Iteration sum
            long iterSum = 0;
            t0 = System.nanoTime();
            for (int x : arrayList) iterSum += x;
            long alIterUs = (System.nanoTime() - t0) / 1_000L;

            t0 = System.nanoTime();
            for (int x : linkedList) iterSum += x;
            long llIterUs = (System.nanoTime() - t0) / 1_000L;
            if (iterSum == -1) System.out.print("");

            System.out.printf("Size N=%d | Append(ms): AL=%d, LL=%d, AD=%d | %d Random Gets(us): AL=%d, LL=%d | %d Front Adds(ms): AL=%d, LL=%d, AD=%d | Iter(us): AL=%d, LL=%d%n",
                    n, alAppendMs, llAppendMs, adAppendMs, ops, alGetUs, llGetUs, frontOps, alFrontMs, llFrontMs, adFrontMs, alIterUs, llIterUs);
        }
    }
}

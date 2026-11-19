import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AnswerScriptScanner {
    static class ScannerThread extends Thread {
        private final int stationId;
        private final int batchSize;
        private final AtomicInteger totalScanned;

        ScannerThread(int stationId, int batchSize, AtomicInteger totalScanned) {
            super("Station-" + stationId);
            this.stationId = stationId;
            this.batchSize = batchSize;
            this.totalScanned = totalScanned;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= batchSize; i++) {
                    Thread.sleep(5); // TIMED_WAITING
                    totalScanned.incrementAndGet();
                }
                System.out.println(getName() + " finished (" + batchSize + " scripts)");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class ScannerTask implements Runnable {
        private final String name;
        private final int batchSize;
        private final AtomicInteger totalScanned;

        ScannerTask(String name, int batchSize, AtomicInteger totalScanned) {
            this.name = name;
            this.batchSize = batchSize;
            this.totalScanned = totalScanned;
        }

        @Override
        public void run() {
            try {
                for (int i = 1; i <= batchSize; i++) {
                    Thread.sleep(5);
                    totalScanned.incrementAndGet();
                }
                System.out.println(name + " [Runnable] finished (" + batchSize + " scripts)");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger total = new AtomicInteger(0);
        ScannerThread s1 = new ScannerThread(1, 25, total);
        ScannerThread s2 = new ScannerThread(2, 25, total);
        Thread s3 = new Thread(new ScannerTask("Station-3", 25, total));
        Thread s4 = new Thread(new ScannerTask("Station-4", 25, total));

        System.out.println("Before start: s1.isAlive()=" + s1.isAlive() + ", state=" + s1.getState());
        s1.start(); s2.start(); s3.start(); s4.start();
        System.out.println("After start: s1.isAlive()=" + s1.isAlive() + ", state=" + s1.getState());

        s1.join(); s2.join(); s3.join(); s4.join();
        System.out.println("After join: s1.isAlive()=" + s1.isAlive() + ", state=" + s1.getState());
        System.out.println("Total scripts scanned across 4 stations: " + total.get());
    }
}

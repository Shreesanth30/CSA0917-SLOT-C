public class TokenCounter {
    private final int[] buffer = new int[5]; // the counter holds at most 5 tokens
    private int count = 0;
    private int in = 0;
    private int out = 0;

    // TODO 1: Wait while the counter is full, add the token at position in, then wake waiting threads
    public synchronized void put(int token) throws InterruptedException {
        while (count == buffer.length) {
            wait();
        }
        buffer[in] = token;
        in = (in + 1) % buffer.length;
        count++;
        notifyAll();
    }

    // TODO 2: Wait while the counter is empty, remove the token at position out, wake waiting threads and return the token
    public synchronized int take() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        int token = buffer[out];
        out = (out + 1) % buffer.length;
        count--;
        notifyAll();
        return token;
    }

    // Part A: Deadlock-free Wallet Transfer via consistent lock ordering
    static class Wallet {
        final int id;
        int balance;
        Wallet(int id, int balance) { this.id = id; this.balance = balance; }
    }

    public static void safeTransfer(Wallet from, Wallet to, int amount) {
        Wallet first = from.id < to.id ? from : to;
        Wallet second = from.id < to.id ? to : from;
        synchronized (first) {
            synchronized (second) {
                if (from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TokenCounter counter = new TokenCounter();
        int[] collected = new int[2];
        Thread cook = new Thread(() -> {
            try {
                for (int t = 1; t <= 20; t++) counter.put(t);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread[] students = new Thread[2];
        for (int s = 0; s < 2; s++) {
            final int id = s;
            students[s] = new Thread(() -> {
                try {
                    for (int k = 0; k < 10; k++) collected[id] += counter.take();
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        cook.start();
        for (Thread t : students) t.start();
        cook.join();
        for (Thread t : students) t.join();
        System.out.println("Sum of tokens collected: " + (collected[0] + collected[1]) + " (expected 210)");

        Wallet w1 = new Wallet(101, 500);
        Wallet w2 = new Wallet(102, 500);
        Thread tA = new Thread(() -> { for (int i = 0; i < 100; i++) safeTransfer(w1, w2, 2); });
        Thread tB = new Thread(() -> { for (int i = 0; i < 100; i++) safeTransfer(w2, w1, 2); });
        tA.start(); tB.start(); tA.join(); tB.join();
        System.out.println("Safe Wallet Balances after 200 concurrent transfers: W1=" + w1.balance + ", W2=" + w2.balance);
    }
}

public class SeatBooking {
    private final boolean[] booked;
    private int bookingsMade = 0;

    SeatBooking(int seats) { booked = new boolean[seats]; }

    // Unsynchronized version (demonstrates race condition)
    public boolean bookUnsynchronized(int seat) {
        if (!booked[seat]) {
            Thread.yield(); // widen race window
            booked[seat] = true;
            bookingsMade++;
            return true;
        }
        return false;
    }

    // TODO 1: Make the check-then-book operation atomic so that a seat can never be sold twice
    public synchronized boolean book(int seat) {
        if (!booked[seat]) {
            booked[seat] = true;
            bookingsMade++;
            return true;
        }
        return false;
    }

    // Fine-grained synchronized block version
    private final Object lock = new Object();
    public boolean bookWithBlock(int seat) {
        synchronized (lock) {
            if (!booked[seat]) {
                booked[seat] = true;
                bookingsMade++;
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 1. Unsynchronized run showing over-selling (>10)
        SeatBooking unsafeScreen = new SeatBooking(10);
        Thread[] unsafeUsers = new Thread[100];
        for (int i = 0; i < unsafeUsers.length; i++) {
            final int seat = i % 10;
            unsafeUsers[i] = new Thread(() -> unsafeScreen.bookUnsynchronized(seat));
            unsafeUsers[i].start();
        }
        for (Thread t : unsafeUsers) t.join();
        System.out.println("Unsynchronized seats sold (race condition): " + unsafeScreen.bookingsMade);

        // 2. Synchronized TODO 1 version (exact expected output: Seats sold: 10)
        SeatBooking screen = new SeatBooking(10);
        Thread[] users = new Thread[100];
        for (int i = 0; i < users.length; i++) {
            final int seat = i % 10; // 10 users compete for every seat
            users[i] = new Thread(() -> screen.book(seat));
            users[i].start();
        }
        for (Thread t : users) t.join();
        System.out.println("Seats sold: " + screen.bookingsMade);
    }
}

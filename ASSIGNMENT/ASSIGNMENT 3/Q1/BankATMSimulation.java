import java.io.*;
import java.util.*;

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String msg) { super(msg); }
}

class InvalidAmountException extends IllegalArgumentException {
    public InvalidAmountException(String msg) { super(msg); }
}

public class BankATMSimulation {
    private static final Map<String, Double> ACCOUNTS = new HashMap<>();
    static {
        ACCOUNTS.put("ACC1001", 15000.0);
        ACCOUNTS.put("ACC1002", 2500.0);
    }

    public static String processWithdrawal(String accNo, String rawAmount, String cardReaderPath) {
        BufferedReader cardReader = null;
        StringBuilder trace = new StringBuilder();
        try {
            // Failure Point 1: Card reader hardware/config file missing (Checked IOException)
            File f = new File(cardReaderPath);
            if (!f.exists()) {
                throw new FileNotFoundException("Card reader device file missing: " + cardReaderPath);
            }
            cardReader = new BufferedReader(new FileReader(f));
            String readerStatus = cardReader.readLine();
            trace.append("[Reader=").append(readerStatus).append("] ");

            // Failure Point 2: Non-numeric input (Unchecked NumberFormatException)
            double amount = Double.parseDouble(rawAmount.trim());

            // Failure Point 3: Zero or negative withdrawal (Unchecked InvalidAmountException)
            if (amount <= 0) {
                throw new InvalidAmountException("Amount must be positive: " + amount);
            }

            // Failure Point 4: Unknown account (Unchecked NullPointerException / NoSuchElementException)
            Double balance = ACCOUNTS.get(accNo);
            if (balance == null) {
                throw new NoSuchElementException("Account not found: " + accNo);
            }

            // Failure Point 5: Balance insufficient (Checked InsufficientFundsException)
            if (amount > balance) {
                throw new InsufficientFundsException("Requested " + amount + " exceeds balance " + balance);
            }

            ACCOUNTS.put(accNo, balance - amount);
            trace.append("DISPENSED ").append(amount).append(" | New Balance=").append(ACCOUNTS.get(accNo));
            return trace.toString();

        } catch (FileNotFoundException e) {
            trace.append("CAUGHT [FileNotFoundException - Checked]: ").append(e.getMessage());
            return trace.toString();
        } catch (NumberFormatException | InvalidAmountException e) {
            // Multi-catch for input validation unchecked exceptions
            trace.append("CAUGHT [Multi-Catch Input Error - Unchecked]: ").append(e.getClass().getSimpleName()).append(" - ").append(e.getMessage());
            return trace.toString();
        } catch (InsufficientFundsException e) {
            trace.append("CAUGHT [InsufficientFundsException - Checked]: ").append(e.getMessage());
            return trace.toString();
        } catch (IOException | RuntimeException e) {
            trace.append("CAUGHT [Fallback Handler]: ").append(e.getClass().getSimpleName()).append(" - ").append(e.getMessage());
            return trace.toString();
        } finally {
            // Guaranteed cleanup: eject card & close card-reader stream
            if (cardReader != null) {
                try { cardReader.close(); } catch (IOException ignored) {}
            }
            System.out.println("  [FINALLY] Card ejected & session lock released for " + accNo);
        }
    }

    public static void main(String[] args) throws IOException {
        File tempReader = new File("card_reader.cfg");
        try (FileWriter fw = new FileWriter(tempReader)) { fw.write("READY"); }

        System.out.println("=== Q1: Bank ATM Simulation Test Cases ===");
        System.out.println("Test 1 (Valid): " + processWithdrawal("ACC1001", "3000", "card_reader.cfg"));
        System.out.println("Test 2 (Letters): " + processWithdrawal("ACC1001", "abc", "card_reader.cfg"));
        System.out.println("Test 3 (Negative): " + processWithdrawal("ACC1001", "-500", "card_reader.cfg"));
        System.out.println("Test 4 (Overdraft): " + processWithdrawal("ACC1002", "9000", "card_reader.cfg"));
        System.out.println("Test 5 (Missing Reader): " + processWithdrawal("ACC1001", "1000", "missing_reader.cfg"));
        System.out.println("Test 6 (Unknown Account): " + processWithdrawal("ACC9999", "1000", "card_reader.cfg"));
        tempReader.delete();
    }
}

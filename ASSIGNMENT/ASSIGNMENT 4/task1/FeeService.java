package task1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class FeeService {
    private static final String URL = "jdbc:mysql://localhost:3306/campus_fees";
    private static final String USER = "root";
    private static final String PWD = "password";

    public PaymentResult processPayment(String rawRegNo, String rawAmount) {
        // 1. Validate inputs before touching the database
        if (rawRegNo == null || !rawRegNo.trim().matches("^[A-Za-z0-9]{5,15}$")) {
            return new PaymentResult(false, "Invalid Registration Number (expected 5-15 alphanumeric chars).", null);
        }
        if (rawAmount == null || rawAmount.trim().isEmpty()) {
            return new PaymentResult(false, "Fee amount cannot be empty.", null);
        }

        double amount;
        try {
            amount = Double.parseDouble(rawAmount.trim());
        } catch (NumberFormatException ex) {
            return new PaymentResult(false, "Invalid amount: please enter a numeric value.", null);
        }

        if (amount <= 0 || amount > 500000) {
            return new PaymentResult(false, "Amount must be greater than 0 and at most 500,000.", null);
        }

        String regNo = rawRegNo.trim().toUpperCase();
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 2. Parameterized SQL + try-with-resources prevents SQL injection and leaks
        String sql = "INSERT INTO fees (txn_id, reg_no, amount) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PWD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txnId);
            ps.setString(2, regNo);
            ps.setDouble(3, amount);
            ps.executeUpdate();
            return new PaymentResult(true, String.format("Payment of INR %.2f confirmed for %s.", amount, regNo), txnId);
        } catch (SQLException ex) {
            return new PaymentResult(false, "Database error: " + ex.getMessage(), null);
        }
    }

    public String generateReceipt(String regNo, String amount, String txnId) {
        if (txnId == null || txnId.isEmpty()) {
            return "No completed transaction in this session. Please complete payment first.";
        }
        return String.format("=== OFFICIAL FEE RECEIPT ===\nTransaction ID : %s\nRegister No    : %s\nAmount Paid    : INR %s\nStatus         : PAID",
                txnId, regNo.trim().toUpperCase(), amount.trim());
    }
}

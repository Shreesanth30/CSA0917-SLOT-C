package task1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class FeeWindow extends JFrame {
    private final JTextField regNoField = new JTextField(15);
    private final JTextField amountField = new JTextField(15);
    private final JButton payButton = new JButton("Pay");
    private final JButton clearButton = new JButton("Clear");
    private final JButton receiptButton = new JButton("Receipt");
    private final JLabel statusLabel = new JLabel("Ready. Enter registration number and fee amount.");

    private final FeeService feeService;
    private String lastTransactionId = null;

    public FeeWindow(FeeService feeService) {
        super("Student Fee Payment Portal");
        this.feeService = feeService;
        buildUI();
        registerHandlers();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
        formPanel.add(new JLabel("Registration Number:"));
        formPanel.add(regNoField);
        formPanel.add(new JLabel("Fee Amount (INR):"));
        formPanel.add(amountField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        buttonPanel.add(payButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(receiptButton);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));
        statusLabel.setForeground(new Color(30, 60, 110));

        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void registerHandlers() {
        // Separate listener per action: O(1) dispatch, no fragile string '==' checks
        payButton.addActionListener(e -> handlePay());
        clearButton.addActionListener(e -> handleClear());
        receiptButton.addActionListener(e -> handleReceipt());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void handlePay() {
        String regNo = regNoField.getText();
        String amount = amountField.getText();

        payButton.setEnabled(false);
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setText("Processing payment with bank gateway...");

        // Offload database I/O off the Event Dispatch Thread
        new SwingWorker<PaymentResult, Void>() {
            @Override
            protected PaymentResult doInBackground() {
                return feeService.processPayment(regNo, amount);
            }

            @Override
            protected void done() {
                payButton.setEnabled(true);
                try {
                    PaymentResult res = get();
                    if (res.isSuccess()) {
                        lastTransactionId = res.getTransactionId();
                        statusLabel.setForeground(new Color(0, 120, 50));
                        statusLabel.setText(res.getMessage() + " [" + lastTransactionId + "]");
                    } else {
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText(res.getMessage());
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Unexpected error: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void handleClear() {
        regNoField.setText("");
        amountField.setText("");
        lastTransactionId = null;
        statusLabel.setForeground(new Color(30, 60, 110));
        statusLabel.setText("Fields cleared.");
    }

    private void handleReceipt() {
        String receiptText = feeService.generateReceipt(
                regNoField.getText(), amountField.getText(), lastTransactionId);
        JOptionPane.showMessageDialog(this, receiptText, "Fee Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow(new FeeService()).setVisible(true));
    }
}

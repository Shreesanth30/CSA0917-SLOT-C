package task6;

import javax.swing.SwingUtilities;

public class EventRegistrationApp {
    public static void main(String[] args) {
        RegistrationDAO dao = new RegistrationDAO();
        RegistrationService service = new RegistrationService(dao);
        SwingUtilities.invokeLater(() -> new EventRegistrationFrame(service).setVisible(true));
    }
}

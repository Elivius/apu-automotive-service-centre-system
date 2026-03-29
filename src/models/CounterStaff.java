package models;

/**
 * CounterStaff subclass - inherits from User.
 * Counter staff can manage customer accounts, manage appointments,
 * assign technicians, collect payments, and generate receipts.
 */
public class CounterStaff extends User {

    // ───── Constructor ─────
    public CounterStaff() {
        setRole("CounterStaff");
    }

    public CounterStaff(String userId, String username, String password, String name,
                        String email, String phone) {
        super(userId, username, password, name, email, phone, "CounterStaff");
    }

    // ───── Polymorphism: Override displayDashboard() ─────
    @Override
    public void displayDashboard() {
        javax.swing.SwingUtilities.invokeLater(() ->
            new ui.staff.StaffDashboard(this).setVisible(true));
    }

    // ───── Factory method to parse from file ─────
    public static CounterStaff fromFileString(String line) {
        String[] parts = line.split(utils.FileHandler.DELIMITER, -1);
        if (parts.length >= 7) {
            return new CounterStaff(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        return null;
    }
}

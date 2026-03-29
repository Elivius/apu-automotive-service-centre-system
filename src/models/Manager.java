package models;

/**
 * Manager subclass - inherits from User.
 * Managers can manage staff, set service prices, analyse reports,
 * and view all feedback/comments/service reviews.
 */
public class Manager extends User {

    // ───── Constructor ─────
    public Manager() {
        setRole("Manager");
    }

    public Manager(String userId, String username, String password, String name,
                   String email, String phone) {
        super(userId, username, password, name, email, phone, "Manager");
    }

    // ───── Polymorphism: Override displayDashboard() ─────
    @Override
    public void displayDashboard() {
        javax.swing.SwingUtilities.invokeLater(() ->
            new ui.manager.ManagerDashboard(this).setVisible(true));
    }

    // ───── Factory method to parse from file ─────
    public static Manager fromFileString(String line) {
        String[] parts = line.split(utils.FileHandler.DELIMITER, -1);
        if (parts.length >= 7) {
            return new Manager(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        return null;
    }
}

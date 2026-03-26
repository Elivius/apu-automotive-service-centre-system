package models;

/**
 * Customer subclass - inherits from User.
 * Customers can register, book appointments, check appointment status,
 * provide comments/reviews, view service histories, and view payment histories.
 */
public class Customer extends User {

    // ───── Constructor ─────
    public Customer() {
        setRole("Customer");
    }

    public Customer(String userId, String username, String password, String name,
                    String email, String phone) {
        super(userId, username, password, name, email, phone, "Customer"); // Throw to parent class constructor
    }

    // ───── Polymorphism: Override displayDashboard() ─────
    @Override
    public void displayDashboard() {
        javax.swing.SwingUtilities.invokeLater(() ->
            new ui.customer.CustomerDashboard(this).setVisible(true));
    }

    // ───── Factory method to parse from file ─────
    public static Customer fromFileString(String line) {
        String[] parts = line.split(utils.FileHandler.DELIMITER, -1);
        if (parts.length >= 7) {
            return new Customer(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]); // Like rebuilt the object (construct) from file data
        }
        return null;
    }
}

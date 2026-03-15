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
        System.out.println("═══════════════════════════════════════");
        System.out.println("         CUSTOMER DASHBOARD");
        System.out.println("═══════════════════════════════════════");
        System.out.println("  Welcome, " + getName());
        System.out.println("  1. Book Appointment");
        System.out.println("  2. Check Appointment Status");
        System.out.println("  3. View Service Histories");
        System.out.println("  4. View Payment Histories");
        System.out.println("  5. Edit Profile");
        System.out.println("  6. Logout");
        System.out.println("═══════════════════════════════════════");
    }

    // ───── Factory method to parse from file ─────
    public static Customer fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 7) {
            return new Customer(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]); // Like rebuilt the object (construct) from file data
        }
        return null;
    }
}

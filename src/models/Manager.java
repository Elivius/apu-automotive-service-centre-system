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
        System.out.println("═══════════════════════════════════════");
        System.out.println("         MANAGER DASHBOARD");
        System.out.println("═══════════════════════════════════════");
        System.out.println("  Welcome, " + getName());
        System.out.println("  1. Manage Staff (CRUD)");
        System.out.println("  2. Set Service Prices");
        System.out.println("  3. Analyse Reports");
        System.out.println("  4. View Feedbacks & Reviews");
        System.out.println("  5. Edit Profile");
        System.out.println("  6. Logout");
        System.out.println("═══════════════════════════════════════");
    }

    // ───── Factory method to parse from file ─────
    public static Manager fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 7) {
            return new Manager(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
        }
        return null;
    }
}

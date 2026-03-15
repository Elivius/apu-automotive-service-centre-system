package models;

/**
 * Technician subclass - inherits from User.
 * Technicians can view upcoming appointments, check details,
 * update appointment status, and provide feedback.
 *
 * Has an additional private attribute 'specialization' demonstrating
 * extended encapsulation in subclasses.
 */
public class Technician extends User {

    // ───── Additional Private Attribute (Encapsulation) ─────
    private String specialization;

    // ───── Constructors ─────
    public Technician() {
        setRole("Technician");
    }

    public Technician(String userId, String username, String password, String name,
                      String email, String phone, String specialization) {
        super(userId, username, password, name, email, phone, "Technician");
        this.specialization = specialization;
    }

    // ───── Polymorphism: Override displayDashboard() ─────
    @Override
    public void displayDashboard() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("        TECHNICIAN DASHBOARD");
        System.out.println("═══════════════════════════════════════");
        System.out.println("  Welcome, " + getName());
        if (this.specialization != null && !this.specialization.isEmpty()) {
            System.out.println("  Specialization: " + this.specialization);
        }
        System.out.println("  1. View Upcoming Appointments");
        System.out.println("  2. Update Appointment Status");
        System.out.println("  3. Provide Feedback");
        System.out.println("  4. Edit Profile");
        System.out.println("  5. Logout");
        System.out.println("═══════════════════════════════════════");
    }

    // ───── Getter & Setter (Encapsulation) ─────
    public String getSpecialization() {
        return this.specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // ───── Override toFileString to include specialization ─────
    @Override
    public String toFileString() {
        return super.toFileString() + "|" + safe(this.specialization);
    }

    // ───── Factory method to parse from file ─────
    public static Technician fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 7) {
            String specialization = parts.length >= 8 ? parts[7] : "";
            return new Technician(parts[0], parts[1], parts[2], parts[3],
                    parts[4], parts[5], specialization);
        }
        return null;
    }
}

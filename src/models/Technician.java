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
        javax.swing.SwingUtilities.invokeLater(() ->
            new ui.technician.TechnicianDashboard(this).setVisible(true));
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
        return super.toFileString() + utils.FileHandler.SEPARATOR + safe(this.specialization);
    }

    // ───── Factory method to parse from file ─────
    public static Technician fromFileString(String line) {
        String[] parts = line.split(utils.FileHandler.DELIMITER, -1);
        if (parts.length >= 7) {
            String specialization = parts.length >= 8 ? parts[7] : "";
            return new Technician(parts[0], parts[1], parts[2], parts[3],
                    parts[4], parts[5], specialization);
        }
        return null;
    }
}

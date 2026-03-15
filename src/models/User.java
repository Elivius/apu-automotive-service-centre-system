package models;

import utils.PasswordHasher;

/**
 * Abstract base class for all users in the APU Automotive Service Centre system.
 *
 * Demonstrates:
 * - Encapsulation: All attributes are private with public getters/setters.
 * - Abstraction: Cannot be instantiated directly; subclasses must implement displayDashboard().
 * - Polymorphism (Method Overloading): Two search() methods with different parameter types.
 */
public abstract class User {

    // ───── Private Attributes (Encapsulation) ─────
    private String userId;
    private String username;
    private String password; // Stored as SHA-256 hash
    private String name;
    private String email;
    private String phone;
    private String role;

    // ───── Constructors ─────
    public User() {
    }

    public User(String userId, String username, String password, String name,
                String email, String phone, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // ═══════════════════════════════════════════
    //  Polymorphism - Method Overriding
    // ═══════════════════════════════════════════

    /**
     * Displays the role-specific dashboard UI.
     * Each subclass must provide its own implementation.
     */
    public abstract void displayDashboard();

    // ═══════════════════════════════════════════
    //  Polymorphism - Method Overloading
    // ═══════════════════════════════════════════

    /**
     * Searches for a user by name (String).
     *
     * @param name the name to search for
     * @return true if the user's name matches (case-insensitive)
     */
    public boolean search(String name) {
        return this.name != null && this.name.equalsIgnoreCase(name);
    }

    /**
     * Searches for a user by ID number (int).
     * Extracts the numeric part from userId and compares.
     *
     * @param id the numeric ID to search for
     * @return true if the user's numeric ID matches
     */
    public boolean search(int id) {
        if (this.userId == null) return false;
        try {
            // Extract numeric portion from userId (e.g. "CUS0001" -> 1)
            String numericPart = this.userId.replaceAll("[^0-9]", "");
            return Integer.parseInt(numericPart) == id;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ═══════════════════════════════════════════
    //  GETTERS AND SETTERS (Encapsulation)
    // ═══════════════════════════════════════════

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        this.password = password;
    }

    /**
     * Hashes and sets the password using SHA-256.
     *
     * @param plainPassword the plain-text password to hash and store
     */
    public void setHashedPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        this.password = PasswordHasher.hash(plainPassword);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        if (email != null && !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ═══════════════════════════════════════════
    //  FILE SERIALIZATION
    // ═══════════════════════════════════════════

    /**
     * Converts the user to a pipe-delimited string for .txt file storage.
     * Format: userId:::username:::password:::name:::email:::phone:::role
     *
     * @return pipe-delimited string representation
     */
    public String toFileString() {
        return String.join(utils.FileHandler.SEPARATOR,
                safe(this.userId), safe(this.username), safe(this.password),
                safe(this.name), safe(this.email), safe(this.phone), safe(this.role));
    }

    /**
     * Returns a null-safe string (replaces null with empty string).
     */
    protected String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Print usefull data instead of the memory hashes
     */
    @Override
    public String toString() {
        return this.role + " [ID=" + this.userId + ", Name=" + this.name + ", Username=" + this.username + "]";
    }
}

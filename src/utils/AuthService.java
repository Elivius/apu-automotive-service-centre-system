package utils;

import java.util.List;
import models.User;
import models.Manager;
import models.CounterStaff;
import models.Technician;
import models.Customer;

/**
 * Service class for handling user authentication and role-based parsing.
 * Keeps FileHandler focused only on generic file reading/writing.
 */
public class AuthService {

    /**
     * Attempts to log in a user by verifying their username and password
     * against the users.txt file.
     *
     * @param username the entered username
     * @param password the entered plain-text password
     * @return the logged-in User object (Manager, Technician, etc.), or null if failed
     */
    public static User login(String username, String password) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.USERS_FILE);
        for (String line : lines) {
            String[] parts = line.split(FileHandler.DELIMITER, -1);
            if (parts.length >= 7) {
                String fileUsername = parts[1];
                String filePasswordHash = parts[2];

                // If username matches and the password hash matches
                if (fileUsername.equals(username) && PasswordHasher.verify(password, filePasswordHash)) {
                    // Polymorphism in action: parseUser returns the specific subclass
                    return parseUser(line);
                }
            }
        }
        return null; // Login failed
    }

    /**
     * Factory method that parses a line from users.txt and creates the
     * correct subclass object based on the "role" field.
     *
     * @param line a pipe-delimited string from users.txt
     * @return the instantiated subclass (e.g. Manager, Customer), as a generic User
     */
    private static User parseUser(String line) {
        String[] parts = line.split(FileHandler.DELIMITER, -1);
        if (parts.length < 7) return null;

        String role = parts[6];

        switch (role) {
            case "Manager":
                return Manager.fromFileString(line);
            case "CounterStaff":
                return CounterStaff.fromFileString(line);
            case "Technician":
                return Technician.fromFileString(line);
            case "Customer":
                return Customer.fromFileString(line);
            default:
                System.err.println("Unknown role found in file: " + role);
                return null;
        }
    }
}

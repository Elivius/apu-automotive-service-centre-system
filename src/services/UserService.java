package services;

import models.Manager;
import models.CounterStaff;
import models.Technician;
import models.Customer;
import models.User;
import utils.FileHandler;
import utils.PasswordHasher;

import java.util.List;

/**
 * Business logic class for managing Users.
 * Handles registration, saving, and querying users.
 */
public class UserService {

    /**
     * Registers a new user into the system (Customer, Manager, Technician, etc.).
     * Handles password hashing securely before saving.
     *
     * @param role The role of the user (e.g., "Customer", "Technician")
     */
    public static void registerUser(String username, String plainPassword, String name, String email, String phone, String role, String specialization) {
        // 1. Generate a new User ID based on the role prefix
        String prefix = role.equals("Customer") ? "CUS" : 
                        role.equals("Manager") ? "MGR" : 
                        role.equals("CounterStaff") ? "STF" : "TEC";
        String newId = FileHandler.getInstance().generateNextId(FileHandler.USERS_FILE, prefix);

        // 2. Create the specific User object based on the role
        User newUser = null;
        switch (role) {
            case "Manager":
                newUser = new Manager(newId, username, null, name, email, phone);
                break;
            case "CounterStaff":
                newUser = new CounterStaff(newId, username, null, name, email, phone);
                break;
            case "Technician":
                newUser = new Technician(newId, username, null, name, email, phone, specialization);
                break;
            case "Customer":
            default:
                newUser = new Customer(newId, username, null, name, email, phone);
                break;
        }

        // 3. Hash the password securely using our existing model logic
        newUser.setHashedPassword(plainPassword);

        // 4. Save to the database
        FileHandler.getInstance().appendLine(FileHandler.USERS_FILE, newUser.toFileString());
    }

    /**
     * Updates an existing user's information in the database.
     * Replaces the old record with the new updated User object's data.
     */
    public static void updateUser(User user) {
        if (user != null && user.getUserId() != null) {
            FileHandler.getInstance().updateLine(FileHandler.USERS_FILE, user.getUserId(), user.toFileString());
            NotificationService.push(user.getUserId(), "Your profile has been updated.");
        }

    }

    /**
     * Deletes a user from the database based on their user ID.
     */
    public static void deleteUser(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            FileHandler.getInstance().deleteLine(FileHandler.USERS_FILE, userId);
        }
    }

    /**
     * Attempts to log in a user by verifying their username and password
     * against the users.txt file.
     *
     * @param username the entered username
     * @param password the entered plain-text password
     * @return the logged-in User object (Manager, Technician, etc.), or null if failed
     */
    public static User loginUser(String username, String password) {
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
     * Finds a User by searching through the text file.
     * Demonstrates using the overloaded search(String) method.
     */
    public static User getUserByName(String name) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.USERS_FILE);
        
        // We use AuthService.parseUser to correctly build the subclass (Manager, Customer, etc.)
        for (String line : lines) {
            User obj = parseUser(line);
            if (obj != null && obj.search(name)) { // Uses the overloaded String search!
                return obj;
            }
        }
        return null;
    }

    /**
     * Finds a User by searching through the text file.
     * Demonstrates using the overloaded search(int) method.
     */
    public static User getUserById(int numericId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.USERS_FILE);
        
        for (String line : lines) {
            User obj = parseUser(line);
            if (obj != null && obj.search(numericId)) { // Uses the overloaded int search!
                return obj;
            }
        }
        return null;
    }

    /**
     * Factory method that parses a line from users.txt and creates the
     * correct subclass object based on the "role" field.
     *
     * @param line a pipe-delimited string from users.txt
     * @return the instantiated subclass (e.g. Manager, Customer), as a generic User
     */
    public static User parseUser(String line) {
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

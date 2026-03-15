package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing and verifying passwords using SHA-256.
 * Passwords are never stored in plain text.
 */
public class PasswordHasher {

    /**
     * Hashes a plain-text password using SHA-256.
     *
     * @param password the plain-text password to hash
     * @return the hexadecimal string representation of the hash
     */
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    /**
     * Verifies a plain-text password against a previously hashed password.
     *
     * @param password       the plain-text password to verify
     * @param hashedPassword the stored hash to compare against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verify(String password, String hashedPassword) {
        return hash(password).equals(hashedPassword);
    }
}

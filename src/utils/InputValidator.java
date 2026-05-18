package utils;

/**
 * Utility class for common input validation.
 * Centralizes validation rules so they are defined once and reused everywhere.
 */
public class InputValidator {

    private static final String EMAIL_REGEX = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
    private static final String PHONE_REGEX = "\\d+";

    /**
     * Validates whether the given string is a valid email format.
     *
     * @param email the email string to validate
     * @return true if the email matches the expected format
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    /**
     * Validates whether the given string contains only digits.
     *
     * @param phone the phone string to validate
     * @return true if the phone number contains only digits
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_REGEX);
    }
}

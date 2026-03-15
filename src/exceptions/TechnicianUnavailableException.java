package exceptions;

/**
 * Custom exception thrown when a technician is not available
 * for the requested appointment time slot.
 */
public class TechnicianUnavailableException extends Exception {

    public TechnicianUnavailableException(String message) {
        super(message);
    }

    public TechnicianUnavailableException(String technicianName, String dateTime) {
        super("Technician '" + technicianName + "' is unavailable at " + dateTime + ". Conflict detected.");
    }
}

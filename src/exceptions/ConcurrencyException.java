package exceptions;

/**
 * Custom exception thrown when an optimistic locking version mismatch is detected.
 * Used to prevent the "Lost Update" problem in concurrent file accesses.
 */
public class ConcurrencyException extends RuntimeException {

    public ConcurrencyException(String message) {
        super(message);
    }
}
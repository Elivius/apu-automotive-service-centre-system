package exceptions;

/**
 * Custom exception thrown when a payment cannot be processed
 * due to insufficient funds or an invalid payment amount.
 */
public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(double required, double available) {
        super("Insufficient funds: Required RM " + String.format("%.2f", required)
                + ", Available RM " + String.format("%.2f", available));
    }
}

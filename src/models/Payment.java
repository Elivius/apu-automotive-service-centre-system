package models;

import utils.DateUtils;

import java.time.LocalDateTime;

/**
 * Payment entity class.
 * Represents a payment transaction in the APU Automotive Service Centre.
 *
 * Supports both Online and Physical payment methods.
 * Online payments update status automatically; Physical payments default to "Pending"
 * until Counter Staff manually confirms.
 */
public class Payment {

    // ───── Public Constants ─────
    public static final String STATUS_PENDING   = "Pending";
    public static final String STATUS_PAID      = "Paid";

    public static final String METHOD_PHYSICAL  = "Physical";
    public static final String METHOD_ONLINE    = "Online";

    // ───── Private Attributes (Encapsulation) ─────
    private String paymentId;
    private String appointmentId;   // Links payment to an appointment
    private double amount;
    private String paymentMethod;   // "Online" or "Physical"
    private String paymentStatus;   // "Pending" or "Paid"
    private LocalDateTime dateTime;

    // ───── Constructors ─────
    public Payment() {
        this.paymentStatus = STATUS_PENDING;
    }

    public Payment(String paymentId, String appointmentId, double amount,
                   String paymentMethod, String paymentStatus, LocalDateTime dateTime) {
        this.paymentId = paymentId;
        this.appointmentId = appointmentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.dateTime = dateTime;
    }

    // ═══════════════════════════════════════════
    //  GETTERS AND SETTERS (Encapsulation)
    // ═══════════════════════════════════════════

    public String getPaymentId() {
        return this.paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getAppointmentId() {
        return this.appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative.");
        }
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return this.paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        if (paymentMethod != null && !paymentMethod.equals("Online") && !paymentMethod.equals("Physical")) {
            throw new IllegalArgumentException("Payment method must be 'Online' or 'Physical'.");
        }
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return this.paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    // ═══════════════════════════════════════════
    //  FILE SERIALIZATION
    // ═══════════════════════════════════════════

    /**
     * Converts the payment to a pipe-delimited string for .txt file storage.
     */
    public String toFileString() {
        return String.join(utils.FileHandler.SEPARATOR,
                safe(this.paymentId),
                safe(this.appointmentId),
                String.format("%.2f", this.amount),
                safe(this.paymentMethod),
                safe(this.paymentStatus),
                this.dateTime != null ? this.dateTime.format(DateUtils.FORMATTER) : "");
    }

    /**
     * Parses a Payment object from a pipe-delimited file line.
     */
    public static Payment fromFileString(String line) {
        String[] parts = line.split(utils.FileHandler.DELIMITER, -1);
        if (parts.length >= 6) {
            Payment payment = new Payment();
            payment.setPaymentId(parts[0]);
            payment.setAppointmentId(parts[1]);
            try {
                payment.amount = Double.parseDouble(parts[2]); // Direct to skip validation on load
            } catch (NumberFormatException e) {
                payment.amount = 0.0;
            }
            payment.paymentMethod = parts[3]; // Direct to skip validation on load
            payment.setPaymentStatus(parts[4]);
            if (!parts[5].isEmpty()) {
                payment.setDateTime(LocalDateTime.parse(parts[5], DateUtils.FORMATTER));
            }
            return payment;
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String toString() {
        return "Payment [ID=" + this.paymentId + ", Amount=RM " + String.format("%.2f", this.amount)
                + ", Method=" + this.paymentMethod + ", Status=" + this.paymentStatus + "]";
    }
}

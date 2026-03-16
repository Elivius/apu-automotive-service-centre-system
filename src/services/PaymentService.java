package services;

import models.Appointment;
import models.Payment;
import utils.FileHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic class for managing Payments.
 * Handles payment processing, receipt generation, and payment history retrieval.
 */
public class PaymentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Processes a payment for a given appointment.
     * - Online payments are automatically marked as "Paid".
     * - Physical payments default to "Pending" until Counter Staff confirms.
     *
     * @param appointmentId the ID of the appointment being paid for
     * @param amount        the payment amount in RM
     * @param paymentMethod "Online" or "Physical"
     */
    public static void processPayment(String appointmentId, double amount, String paymentMethod) {
        // Generate a new Payment ID (e.g., PAY0001)
        String newId = FileHandler.getInstance().generateNextId(FileHandler.PAYMENTS_FILE, "PAY");

        // Determine payment status based on method
        String status = paymentMethod.equals("Online") ? "Paid" : "Pending";

        // Create the Payment object
        Payment payment = new Payment(newId, appointmentId, amount, paymentMethod, status, LocalDateTime.now());

        // Save to the database
        FileHandler.getInstance().appendLine(FileHandler.PAYMENTS_FILE, payment.toFileString());
    }

    /**
     * Marks a physical payment as "Paid".
     * Used by Counter Staff when a customer pays in-person.
     *
     * @param paymentId the ID of the payment to confirm
     */
    public static void confirmPhysicalPayment(String paymentId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.PAYMENTS_FILE);
        for (String line : lines) {
            Payment payment = Payment.fromFileString(line);
            if (payment != null && payment.getPaymentId().equals(paymentId)) {
                payment.setPaymentStatus("Paid");
                FileHandler.getInstance().updateLine(FileHandler.PAYMENTS_FILE, paymentId, payment.toFileString());
                return;
            }
        }
    }

    /**
     * Retrieves the payment history for a specific customer.
     * Matches payments by cross-referencing appointment IDs.
     *
     * @param customerId the customer's ID
     * @return a list of Payment objects belonging to this customer
     */
    public static List<Payment> getPaymentHistory(String customerId) {
        // Get all the appointment of the customer
        // Becuase PAYMENT_FILE does not store the customer id
        // e.g. PAY0001:::APT0001:::150.00:::Online:::Paid:::2026-03-20 10:00
        List<String> appointmentLines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        List<String> customerAppointmentIds = appointmentLines.stream()
                .map(Appointment::fromFileString)
                .filter(apt -> apt != null && apt.getCustomerId().equals(customerId))
                .map(Appointment::getAppointmentId) // Convert back to List<String> since we will need the appointmentId only for filtering Payment
                .collect(Collectors.toList());

        // Get all the payment of the customer by filtering the appointment id
        List<String> paymentLines = FileHandler.getInstance().readAllLines(FileHandler.PAYMENTS_FILE);
        return paymentLines.stream()
                .map(Payment::fromFileString)
                .filter(p -> p != null && customerAppointmentIds.contains(p.getAppointmentId()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the service price from the service_prices.txt file.
     *
     * @param serviceType "Normal" or "Major"
     * @return the price as a double, or 0.0 if not found
     */
    public static double getServicePrice(String serviceType) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.SERVICE_PRICES_FILE);
        for (String line : lines) {
            String[] parts = line.split(FileHandler.DELIMITER, -1);
            if (parts.length >= 2 && parts[0].equals(serviceType)) {
                try {
                    return Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }
        return 0.0;
    }

    /**
     * Sets the service price for a specific service type.
     * Will create a new line if the service type does not exist.
     * 
     * @param serviceType "Normal" or "Major"
     * @param price       the price to set
     */
    public static void setServicePrice(String serviceType, double price) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.SERVICE_PRICES_FILE);
        for (String line : lines) {
            String[] parts = line.split(FileHandler.DELIMITER, -1);
            if (parts.length >= 2 && parts[0].equals(serviceType)) {
                parts[1] = String.valueOf(price);
                FileHandler.getInstance().updateLine(FileHandler.SERVICE_PRICES_FILE, serviceType, String.join(FileHandler.DELIMITER, parts));
                return;
            }
        }

        FileHandler.getInstance().appendLine(FileHandler.SERVICE_PRICES_FILE, serviceType + FileHandler.DELIMITER + price);
    }

    /**
     * Auto-generates a professionally formatted receipt as a .txt file.
     * Fulfills the SPEC.md "Auto-Generating Receipts to Text" requirement.
     *
     * @param payment     the Payment object
     * @param appointment the related Appointment object
     * @return the file path of the generated receipt
     */
    public static String generateReceipt(Payment payment, Appointment appointment) {
        String receiptFileName = "data" + java.io.File.separator + "receipts" + java.io.File.separator
                + "receipt_" + payment.getPaymentId() + ".txt";

        // Ensure the receipts directory exists
        java.io.File receiptDir = new java.io.File("data" + java.io.File.separator + "receipts");
        if (!receiptDir.exists()) {
            receiptDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(receiptFileName))) {
            writer.write("═══════════════════════════════════════════"); writer.newLine();
            writer.write("      APU AUTOMOTIVE SERVICE CENTRE");        writer.newLine();
            writer.write("            OFFICIAL RECEIPT");                writer.newLine();
            writer.write("═══════════════════════════════════════════"); writer.newLine();
            writer.newLine();
            writer.write("  Transaction ID  : " + payment.getPaymentId());                                  writer.newLine();
            writer.write("  Date            : " + (payment.getDateTime() != null ? payment.getDateTime().format(FORMATTER) : "N/A")); writer.newLine();
            writer.newLine();
            writer.write("───────────────────────────────────────────"); writer.newLine();
            writer.write("  APPOINTMENT DETAILS");                       writer.newLine();
            writer.write("───────────────────────────────────────────"); writer.newLine();
            writer.write("  Appointment ID  : " + appointment.getAppointmentId());                           writer.newLine();
            writer.write("  Customer ID     : " + appointment.getCustomerId());                              writer.newLine();
            writer.write("  Technician ID   : " + appointment.getTechnicianId());                            writer.newLine();
            writer.write("  Service Type    : " + appointment.getServiceType());                             writer.newLine();
            writer.write("  Service Date    : " + (appointment.getDateTime() != null ? appointment.getDateTime().format(FORMATTER) : "N/A")); writer.newLine();
            writer.newLine();
            writer.write("───────────────────────────────────────────"); writer.newLine();
            writer.write("  PAYMENT DETAILS");                           writer.newLine();
            writer.write("───────────────────────────────────────────"); writer.newLine();
            writer.write("  Payment Method  : " + payment.getPaymentMethod());                               writer.newLine();
            writer.write("  Payment Status  : " + payment.getPaymentStatus());                               writer.newLine();
            writer.newLine();
            writer.write("  TOTAL AMOUNT    : RM " + String.format("%.2f", payment.getAmount()));            writer.newLine();
            writer.newLine();
            writer.write("═══════════════════════════════════════════"); writer.newLine();
            writer.write("        Thank you for your business!");        writer.newLine();
            writer.write("═══════════════════════════════════════════"); writer.newLine();
        } catch (IOException e) {
            System.err.println("Error generating receipt: " + e.getMessage());
        }

        return receiptFileName;
    }

    /**
     * Finds a specific payment by its ID.
     * Used when Counter Staff needs to view or confirm a specific payment.
     *
     * @param paymentId the payment ID to search for
     * @return the Payment object, or null if not found
     */
    public static Payment getPaymentById(String paymentId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.PAYMENTS_FILE);
        return lines.stream()
                .map(Payment::fromFileString)
                .filter(payment -> payment != null && payment.getPaymentId().equals(paymentId))
                .findFirst()
                .orElse(null);
    }
}

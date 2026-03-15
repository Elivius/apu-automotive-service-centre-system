package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Appointment entity class.
 * Represents a service appointment in the APU Automotive Service Centre.
 *
 * Demonstrates Aggregation — references Customer and Technician by their IDs,
 * showing how entities relate to each other.
 *
 * Uses java.time.LocalDateTime (modern Java Time API) instead of legacy Date.
 */
public class Appointment {

    // ───── Date Format ─────
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ───── Private Attributes (Encapsulation) ─────
    private String appointmentId;
    private String customerId;      // Aggregation: references Customer
    private String technicianId;    // Aggregation: references Technician
    private String serviceType;     // "Normal" or "Major"
    private String status;          // "Pending", "Assigned", "Completed", "Declined"
    private LocalDateTime dateTime;
    private LocalDateTime endDateTime;
    private String comments;        // Customer's comments
    private String feedback;        // Technician's feedback
    private String serviceReview;   // Customer's service review

    // ───── Constructors ─────
    public Appointment() {
        this.status = "Pending";
    }

    public Appointment(String appointmentId, String customerId, String technicianId,
                       String serviceType, String status, LocalDateTime dateTime,
                       LocalDateTime endDateTime, String comments, String feedback,
                       String serviceReview) {
        this.appointmentId = appointmentId;
        this.customerId = customerId;
        this.technicianId = technicianId;
        this.serviceType = serviceType;
        this.status = status;
        this.dateTime = dateTime;
        this.endDateTime = endDateTime;
        this.comments = comments;
        this.feedback = feedback;
        this.serviceReview = serviceReview;
    }

    // ═══════════════════════════════════════════
    //  GETTERS AND SETTERS (Encapsulation)
    // ═══════════════════════════════════════════

    public String getAppointmentId() {
        return this.appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getTechnicianId() {
        return this.technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(String serviceType) {
        if (serviceType != null && !serviceType.equals("Normal") && !serviceType.equals("Major")) {
            throw new IllegalArgumentException("Service type must be 'Normal' or 'Major'.");
        }
        this.serviceType = serviceType;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime getEndDateTime() {
        return this.endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    /**
     * Automatically calculates and sets the end time based on service type.
     * Normal service = 1 hour, Major service = 3 hours.
     */
    public void calculateEndDateTime() {
        if (dateTime != null && serviceType != null) {
            int hours = serviceType.equals("Major") ? 3 : 1;
            this.endDateTime = dateTime.plusHours(hours);
        }
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getFeedback() {
        return this.feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getServiceReview() {
        return this.serviceReview;
    }

    public void setServiceReview(String serviceReview) {
        this.serviceReview = serviceReview;
    }

    // ═══════════════════════════════════════════
    //  FILE SERIALIZATION
    // ═══════════════════════════════════════════

    /**
     * Converts the appointment to a pipe-delimited string for .txt file storage.
     */
    public String toFileString() {
        return String.join("|",
                safe(appointmentId),
                safe(customerId),
                safe(technicianId),
                safe(serviceType),
                safe(status),
                dateTime != null ? dateTime.format(FORMATTER) : "",
                endDateTime != null ? endDateTime.format(FORMATTER) : "",
                safe(comments),
                safe(feedback),
                safe(serviceReview));
    }

    /**
     * Parses an Appointment object from a pipe-delimited file line.
     */
    public static Appointment fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length >= 10) {
            Appointment apt = new Appointment();
            apt.setAppointmentId(parts[0]);
            apt.setCustomerId(parts[1]);
            apt.setTechnicianId(parts[2]);
            apt.serviceType = parts[3]; // Use direct assignment to skip validation on load
            apt.setStatus(parts[4]);
            if (!parts[5].isEmpty()) {
                apt.setDateTime(LocalDateTime.parse(parts[5], FORMATTER));
            }
            if (!parts[6].isEmpty()) {
                apt.setEndDateTime(LocalDateTime.parse(parts[6], FORMATTER));
            }
            apt.setComments(parts[7]);
            apt.setFeedback(parts[8]);
            apt.setServiceReview(parts[9]);
            return apt;
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public String toString() {
        return "Appointment [ID=" + appointmentId + ", Service=" + serviceType
                + ", Status=" + status + ", Date=" + (dateTime != null ? dateTime.format(FORMATTER) : "N/A") + "]";
    }
}

package services;

import models.Appointment;
import utils.FileHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic class for managing Feedback, Comments, and Service Reviews.
 * Separates comment/review logic from Appointment scheduling logic.
 *
 * Three types of text entries:
 * 1. Comments   — written by the Customer BEFORE the service (to explain the issue)
 * 2. Feedback   — written by the Technician AFTER the service (to explain what was done)
 * 3. Service Review — written by the Customer AFTER the service (to rate the experience)
 */
public class FeedbackService {

    /**
     * Allows a Customer to add comments to their appointment
     * (e.g., "My car makes a weird noise when turning").
     *
     * @param appointmentId the appointment to comment on
     * @param comments      the customer's comments
     */
    public static void submitCustomerComments(Appointment appointment, String comments) {
        if (appointment != null && appointment.getAppointmentId() != null) {
            appointment.setComments(comments);
            FileHandler.getInstance().updateLine(FileHandler.APPOINTMENTS_FILE, appointment.getAppointmentId(), appointment.toFileString());
            
            // Push a notification for the assigned technician and counter staff
            NotificationService.push(appointment.getTechnicianId(), "Customer added comments to appointment " + appointment.getAppointmentId());
            NotificationService.push("CounterStaff", "Customer added comments to appointment " + appointment.getAppointmentId());
        }
    }

    /**
     * Allows a Technician to provide feedback after completing a service
     * (e.g., "Replaced brake pads and topped up brake fluid").
     *
     * @param appointmentId the appointment to provide feedback for
     * @param feedback      the technician's feedback
     */
    public static void submitTechnicianFeedback(Appointment appointment, String feedback) {
        if (appointment != null && appointment.getAppointmentId() != null) {
            appointment.setFeedback(feedback);
            FileHandler.getInstance().updateLine(FileHandler.APPOINTMENTS_FILE, appointment.getAppointmentId(), appointment.toFileString());
            
            // Push a notification for the customer
            NotificationService.push(appointment.getCustomerId(), "Technician submitted feedback for appointment " + appointment.getAppointmentId());
        }
    }

    /**
     * Allows a Customer to provide a service review after the appointment is completed
     * (e.g., "Great service! Very professional.").
     *
     * @param appointmentId the appointment to review
     * @param review        the customer's service review
     */
    public static void submitServiceReview(Appointment appointment, String review) {
        if (appointment != null && appointment.getAppointmentId() != null) {
            appointment.setServiceReview(review);
            FileHandler.getInstance().updateLine(FileHandler.APPOINTMENTS_FILE, appointment.getAppointmentId(), appointment.toFileString());
        }
    }

    /**
     * Retrieves all feedback, comments, and reviews across all appointments.
     * Used by the Manager to view all feedback in the system.
     *
     * @return a list of all Appointments that have at least one feedback entry
     */
    public static List<Appointment> getAllFeedback() {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        return lines.stream()
                .map(Appointment::fromFileString)
                .filter(apt -> apt != null)
                .filter(apt -> (apt.getComments() != null && !apt.getComments().isEmpty()) ||
                               (apt.getFeedback() != null && !apt.getFeedback().isEmpty()) ||
                               (apt.getServiceReview() != null && !apt.getServiceReview().isEmpty()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all feedback for a specific customer's appointments.
     * Used by the Customer to view technician feedback on their past services.
     *
     * @param customerId the customer's ID
     * @return a list of Appointments with feedback for this customer
     */
    public static List<Appointment> getFeedbackForCustomer(String customerId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        return lines.stream()
                .map(Appointment::fromFileString)
                .filter(apt -> apt != null && apt.getCustomerId().equals(customerId))
                .filter(apt -> (apt.getFeedback() != null && !apt.getFeedback().isEmpty()))
                .collect(Collectors.toList());
    }

}

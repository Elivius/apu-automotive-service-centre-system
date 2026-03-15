package services;

import exceptions.TechnicianUnavailableException;
import models.Appointment;
import utils.FileHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic class for managing Appointments.
 * Handles the full appointment lifecycle:
 * 1. Customer books (Pending, no technician)
 * 2. Counter Staff assigns a technician (Collision-Aware Scheduling)
 * 3. Technician completes and provides feedback
 */
public class AppointmentService {

    /**
     * Allows a Customer to book a new appointment.
     * The appointment starts as "Pending" with no technician assigned.
     * A notification is pushed to Counter Staff to assign it.
     *
     * @param customerId  the customer's ID
     * @param serviceType "Normal" or "Major"
     * @param dateTime    the requested date and time
     * @param comments    optional comments from the customer
     */
    public static void bookAppointment(String customerId, String serviceType, LocalDateTime dateTime, String comments) {
        // 1. Generate a new Appointment ID
        String newId = FileHandler.getInstance().generateNextId(FileHandler.APPOINTMENTS_FILE, "APT");

        // 2. Create a Pending appointment with no technician assigned yet
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(newId);
        appointment.setCustomerId(customerId);
        appointment.setTechnicianId("");          // No technician yet — Counter Staff will assign
        appointment.setServiceType(serviceType);
        appointment.setStatus("Pending");
        appointment.setDateTime(dateTime);
        appointment.calculateEndDateTime();        // Auto-calculate end time (1hr Normal / 3hr Major)
        appointment.setComments(comments != null ? comments : "");
        appointment.setFeedback("");
        appointment.setServiceReview("");

        // 3. Save to the database
        FileHandler.getInstance().appendLine(FileHandler.APPOINTMENTS_FILE, appointment.toFileString());

        // 4. Push notification to Counter Staff
        NotificationService.push("CounterStaff", "New appointment " + newId + " from customer " + customerId + " is waiting for assignment.");
    }

    /**
     * Allows Counter Staff to assign a technician to a pending appointment.
     * Contains Collision-Aware Scheduling logic to prevent double-booking.
     *
     * @param appointmentId the appointment to assign
     * @param technicianId  the technician to assign
     * @throws TechnicianUnavailableException if the technician has a time conflict
     */
    public static void assignAppointment(String appointmentId, String technicianId) throws TechnicianUnavailableException {
        // Find target appointment
        Appointment targetAppointment = findAppointmentById(appointmentId);

        if (targetAppointment == null) {
            System.err.println("Appointment not found: " + appointmentId);
            return;
        }

        // Load all existing appointments for collision checking
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        List<Appointment> allExistingAppointments = lines.stream()
                .map(Appointment::fromFileString)
                .filter(apt -> apt != null)
                .collect(Collectors.toList());

        // Collision-Aware Scheduling Logic
        for (Appointment existingAppointment : allExistingAppointments) {
            // Only check if it's the same technician and the appointment is active
            if (existingAppointment.getTechnicianId().equals(technicianId) &&
                !existingAppointment.getStatus().equals("Declined") &&
                !existingAppointment.getStatus().equals("Completed")) {

                LocalDateTime newStart = targetAppointment.getDateTime();
                LocalDateTime newEnd = targetAppointment.getEndDateTime();
                LocalDateTime existStart = existingAppointment.getDateTime();
                LocalDateTime existEnd = existingAppointment.getEndDateTime();

                // Check for overlapping time blocks
                boolean isOverlapping = newStart.isBefore(existEnd) && newEnd.isAfter(existStart);

                if (isOverlapping) {
                    throw new TechnicianUnavailableException("Technician " + technicianId +
                            " is already booked from " + existStart.toLocalTime() + " to " + existEnd.toLocalTime() + ".");
                }
            }
        }

        // No collision — assign the technician and update status
        targetAppointment.setTechnicianId(technicianId);
        targetAppointment.setStatus("Assigned to " + technicianId);
        FileHandler.getInstance().updateLine(FileHandler.APPOINTMENTS_FILE, appointmentId, targetAppointment.toFileString());

        // Push notifications to both the customer and the technician
        NotificationService.push(targetAppointment.getCustomerId(), "Your appointment " + appointmentId + " has been assigned to technician " + technicianId + ".");
        NotificationService.push(technicianId, "You have been assigned to appointment " + appointmentId + ".");
    }

    /**
     * Updates an existing appointment's information.
     * Pushes notifications to the customer and technician about the change.
     */
    public static void updateAppointment(Appointment appointment) {
        if (appointment != null && appointment.getAppointmentId() != null) {
            FileHandler.getInstance().updateLine(FileHandler.APPOINTMENTS_FILE, appointment.getAppointmentId(), appointment.toFileString());

            // Notify both customer and technician about the update
            NotificationService.push(appointment.getCustomerId(), "Appointment " + appointment.getAppointmentId() + " has been updated.");
            if (appointment.getTechnicianId() != null && !appointment.getTechnicianId().isEmpty()) {
                NotificationService.push(appointment.getTechnicianId(), "Appointment " + appointment.getAppointmentId() + " has been updated.");
            }
        }
    }

    /**
     * Retrieves all appointments for a specific customer.
     * Used by the Customer to check their appointment statuses.
     */
    public static List<Appointment> getAllAppointmentsForCustomer(String customerId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        return lines.stream()
                .map(Appointment::fromFileString)
                .filter(apt -> apt != null && apt.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all appointments assigned to a specific technician.
     * Used by the Technician to view their upcoming jobs.
     */
    public static List<Appointment> getAllAppointmentsForTechnician(String technicianId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        return lines.stream()
                .map(Appointment::fromFileString)
                .filter(apt -> apt != null && apt.getTechnicianId().equals(technicianId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all appointments in the system.
     * Used by Counter Staff and Manager to view all appointment statuses.
     */
    public static List<Appointment> getAllAppointments() {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        return lines.stream()
                .map(Appointment::fromFileString)
                .filter(apt -> apt != null)
                .collect(Collectors.toList());
    }

    /**
     * Allows a Technician to mark their appointment as "Completed".
     */
    public static void completeAppointment(String appointmentId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        for (String line : lines) {
            Appointment apt = Appointment.fromFileString(line);
            if (apt != null && apt.getAppointmentId().equals(appointmentId)) {
                apt.setStatus("Completed");
                FileHandler.getInstance().updateLine(FileHandler.APPOINTMENTS_FILE, appointmentId, apt.toFileString());

                // Notify the customer
                NotificationService.push(apt.getCustomerId(), "Your appointment " + appointmentId + " has been completed.");
                return;
            }
        }
    }

    /**
     * Finds a specific appointment by its ID.
     * Used when a Technician checks details or Counter Staff updates a specific appointment.
     *
     * @param appointmentId the appointment ID to search for
     * @return the Appointment object, or null if not found
     */
    public static Appointment findAppointmentById(String appointmentId) {
        List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
        for (String line : lines) {
            Appointment apt = Appointment.fromFileString(line);
            if (apt != null && apt.getAppointmentId().equals(appointmentId)) {
                return apt;
            }
        }
        return null;
    }
}

import models.*;
import services.*;
import utils.FileHandler;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manual Test Runner to verify backend logic without needing a GUI.
 * You can run this main class in Eclipse to see the system in action.
 */
public class SystemTest {

    public static void main(String[] args) {
        System.out.println("=== APU Automotive Service Centre System Test ===\n");

        try {
            // 0. Clean start (Optional: comment out if you want to keep existing data)
            // FileHandler.getInstance().writeAllLines(FileHandler.USERS_FILE, new java.util.ArrayList<>());
            // FileHandler.getInstance().writeAllLines(FileHandler.APPOINTMENTS_FILE, new java.util.ArrayList<>());

            // 1. TEST USER REGISTRATION
            System.out.println("[TEST 1] Registering a new Customer and Technician...");
            UserService.registerUser("cus01", "password123", "John Doe", "john@email.com", "0123456789", "Customer", null);
            UserService.registerUser("tec01", "techpass", "Expert Ahmad", "ahmad@email.com", "0987654321", "Technician", "Engine");
            System.out.println("✅ Registration complete.\n");

            // 2. TEST APPOINTMENT BOOKING
            System.out.println("[TEST 2] Customer booking an appointment...");
            LocalDateTime apptTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            AppointmentService.bookAppointment("cus01", "Normal", apptTime, "Brakes feel squeaky");
            AppointmentService.bookAppointment("cus01", "Major", apptTime, "Steering feel squeaky");
            
            // Get the ID of the new appointment
            List<Appointment> apt = AppointmentService.getAllAppointmentsForCustomer("cus01");
            String apptId = apt.get(0).getAppointmentId();
            System.out.println("✅ Appointment booked with ID: " + apptId + "\n");

            // 3. TEST TECHNICIAN ASSIGNMENT & COLLISION
            System.out.println("[TEST 3] Assigning technician to appointment...");
            AppointmentService.assignAppointment(apt.get(0), "tec01");
            System.out.println("✅ Technician 'tec01' assigned successfully.");

            System.out.println("--- Testing Collision Detection ---");
            try {
                // Try to book another appointment at the SAME TIME for the SAME TECHNICIAN
                AppointmentService.assignAppointment(apt.get(1), "tec01"); 
            } catch (Exception e) {
                System.out.println("✅ Collision detection works! Error: " + e.getMessage() + "\n");
            }

            // 4. TEST PAYMENT
            System.out.println("[TEST 4] Processing payment...");
            PaymentService.processPayment(apptId, 150.00, "Online");
            System.out.println("✅ Payment processed for " + apptId + ".\n");

            // 5. TEST NOTIFICATIONS
            System.out.println("[TEST 5] Checking notifications for Customer...");
            List<Notification> notis = NotificationService.getNotificationsForUser("cus01", "Customer");
            System.out.println("Notifications for cus01:");
            for (Notification n : notis) {
                System.out.println(" - [" + n.getDateTime() + "] " + n.getMessage());
            }
            System.out.println("✅ Notifications retrieved successfully.\n");

            System.out.println("=== ALL TESTS PASSED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.err.println("❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

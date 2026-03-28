# Roles and Functionalities (Code-Based)

Based on the implemented source code (specifically traversing `src/ui/*` namespaces), here map out the exact functionalities accessible to each system role.

## 1. Shared Functionality (Available from `User` Dashboards)
These functionalities are available in the base Sidebar pattern shared across all dashboards (`CustomerDashboard`, `StaffDashboard`, `TechnicianDashboard`, `ManagerDashboard`):
- **Authenticate:** Validates session via `LoginFrame`.
- **Edit Profile:** Opens the `EditProfileFrame` to modify user details.
- **Logout:** Provides a confirmation dialog before returning to `LoginFrame`.
- **Toast Notifications:** The UI uses `ToastNotification` internally to present pop-up updates across all roles dynamically.

## 2. Customer (`CustomerDashboard`)
Available Panels:
- **My Appointments (`MyAppointmentsPanel`):** View a detailed table of scheduled or active appointments.
- **Book Appointment (`BookAppointmentPanel`):** Form interface to schedule new vehicle services. Prompts for payment type selection (Online vs Physical) and comments.
- **Service History (`ServiceHistoryPanel`):** Review previous, closed appointments along with feedback/notes left by Technicians.
- **Payment History (`PaymentHistoryPanel`):** View past transactions and fetch issued receipts.

## 3. Counter Staff (`StaffDashboard`)
Available Panels:
- **Manage Customers (`ManageCustomersPanel`):** View, add, edit, or delete customer records.
- **Manage Appointments (`ManageAppointmentsPanel`):** 
  - *Search/Filter:* Use the search bar to filter the appointments table dynamically.
  - *New Appointment:* Form to create appointments on behalf of customers.
  - *Assign Technician:* Select a technician from a dropdown. Validates constraints to avoid scheduling conflicts (throws `TechnicianUnavailableException` upon overlap).
  - *Decline:* Reject/Cancel an appointment requiring validation.
- **Collect Payment (`CollectPaymentPanel`):** Process offline/physical payments and transition the appointment status to "Paid".

## 4. Technician (`TechnicianDashboard`)
Available Panels:
- **My Appointments (`TechMyAppointmentsPanel`):** Table showing *only* the appointments assigned to the logged-in technician.
- **Submit Context/Feedback (`AppointmentDetailFrame`):** When clicking specific appointments, the technician can read customer comments, transition the job state to "Completed," and write final service feedback into the system.

## 5. Manager (`ManagerDashboard`)
Available Panels:
- **Reports (`ReportsPanel`):** View aggregated system, service, and financial reporting metrics.
- **Manage Staff (`ManageStaffPanel`):** View, add, update, and remove system staff accounts (managers, technicians, and counter staff).
- **Service Prices (`ServicePricesPanel`):** Mutate the master price list for "Normal Services" and "Major Services".
- **All Feedback (`AllFeedbackPanel`):** View a global, read-only table aggregating feedback and reviews corresponding to all system appointments.

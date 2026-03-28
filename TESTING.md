# System Testing Guide (Code-Based UI)

This document formalizes step-by-step Quality Assurance test cases explicitly traced from the GUI implementation present in `src/ui/`.

## 1. Global Component Tests (All Roles)

### Login Action Validation (`LoginFrame`)
- **Action:** Open application, enter user credentials (Staff, Manager, Customer, Tech), click `Login`.
- **Validation:** Assert the system returns the corresponding `User` subclass and instantiates the proper Dashboard (e.g., `ManagerDashboard` vs `CustomerDashboard`).

### Profile Update Workflow (`EditProfileFrame`)
- **Action:** Click "✏️ Edit Profile" across any role's sidebar. Change user properties like name, contact info, and save.
- **Validation:** Ensure the filesystem (or connected database) reflects changes accurately, and the sidebar dynamically updates the `lblName` text instantly.

### Dynamic Toast Events (`ToastNotification`)
- **Action:** Induce an event that triggers a system-wide broadcast (like booking a new appointment as a Customer).
- **Validation:** Switch active user to Staff. The `ToastNotification` component should elegantly pop a small window containing the unread ping from `notification.txt`.

---

## 2. Customer Component Tests (`CustomerDashboard`)

### Booking Engine (`BookAppointmentPanel`)
- **Action:** Fill out the 'New Appointment' form. Submit required text blocks (Comments) and drop-down menus (Service Type). Choose `Online Payment`. Repeat steps using `Physical Payment`.
- **Validation:**
  - Online Payment creates an `Appointment` marked "Paid", persisting it, and updating the grid view.
  - Physical Payment creates an `Appointment` flagged "Pending Payment". Both scenarios notify the counter staff in real-time.

### Reviewing Active History (`MyAppointmentsPanel`)
- **Action:** Open the 'My Appointments' panel.
- **Validation:** Assert the Java Swing `JTable` exclusively lists appointments owned by the current Customer's ID.

### Retrieving Service Feedback (`ServiceHistoryPanel`)
- **Action:** Navigate to "Service History" and select a completed appointment row.
- **Validation:** Assert the detail view fully renders the final technician notes and completion timestamp. Validate the "Provide Review" feature allows a customer to submit a service review back to the Manager's panel.

---

## 3. Counter Staff Component Tests (`StaffDashboard`)

### Table Search & Filter (`ManageAppointmentsPanel`)
- **Action:** In 'Manage Appointments', type random characters into the `tfSearch` bar.
- **Validation:** Assert the `TableRowSorter` correctly isolates rows by applying a Regex match across the Appointment ID, Customer ID, and Status text fields dynamically without lag.

### Scheduling Collision Resistance (`ManageAppointmentsPanel`)
- **Action:** Select an active appointment. Click `Assign Technician`. Pick 'TechA'. Next, pick a secondary overlapping appointment for the exact same timeframe and attempt assigning to 'TechA'.
- **Validation:** Assert the system traps the constraint through `TechnicianUnavailableException` correctly throwing `⚠ Conflict Detected` via `JOptionPane`.

### Approving Cash Transactions (`CollectPaymentPanel`)
- **Action:** Navigate to the 'Collect Payment' tabular view. Click a pending Physical Payment row, and fire the `Collect` operation.
- **Validation:** Validate the receipt is uniquely generated within the `FileHandler` implementation, and the `Appointment` status explicitly updates to 'Paid' instantly.

---

## 4. Technician Component Tests (`TechnicianDashboard`)

### Exclusive Job Routing (`TechMyAppointmentsPanel`)
- **Action:** Login as a technician, navigate the 'My Appointments' tab.
- **Validation:** Validate the Swing JTable precisely intercepts the loaded list of appointments and filters strictly for states matching `Assigned` containing the logged-in technician's ID.

### Marking Complete & Handover (`AppointmentDetailFrame`)
- **Action:** Double click an active assignment. Read customer comments. Click the actionable `Complete Job` flag and write final remarks into the `JTextArea`.
- **Validation:** Verify the `AppointmentService` is instructed to finalize the job, setting it to `Completed` globally, locking the UI from further edits, and migrating the entry over to the Service History table for the originating customer.

---

## 5. Manager Component Tests (`ManagerDashboard`)

### Admin Staff Bootstrapping (`ManageStaffPanel`)
- **Action:** Add a completely fresh Counter Staff or Technician account through the interface. Provide credentials and role definitions.
- **Validation:** Test the new account credentials against the `LoginFrame` immediately. They should gain 100% respective UI access.

### Global Prices Application (`ServicePricesPanel`)
- **Action:** Change the Base Price mapping for "Major Services" from `$50.00` to `$99.00` and save changes.
- **Validation:** Impersonate a Customer and trigger `BookAppointmentPanel`. Validate the UI correctly quotes `$99.00` immediately for Major Services.

### Reporting & Transparency (`ReportsPanel` / `AllFeedbackPanel`)
- **Action:** Traverse into `ReportsPanel` and compile visual statistics. Traverse into `AllFeedbackPanel` to view the master comments list.
- **Validation:** The JTable model must contain a 1:1 match of all combined comments, final feedback, and final user reviews generated across the entire lifetime of the system.

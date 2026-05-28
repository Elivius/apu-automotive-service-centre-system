# APU Automotive Service Centre (APU-ASC) - Roles and Functionalities

## All Users
* **Login**: Access the system with SHA-256 hashed password authentication.
* **Edit Personal Profile**: Update personal details (name, email, phone, password) with validation.
* **Notification System**: Real-time bell icon with unread count badge on every dashboard. Supports direct (user-specific), role-based, and broadcast ("ALL") notifications. Per-user read tracking ensures one user's dismissal does not hide notifications from others.
* **Logout**: Securely exit the system and return to the login screen.

## Customer
* **Register**: Sign up for a new account via the registration form (with username, email, and phone uniqueness validation).
* **Book Appointment**: Schedule a new service appointment by selecting a service type (Normal/Major), date, time slot, and payment method (Online/Physical).
  * **Payment Processing**:
    * **Online Payment**: Payment status is automatically set to "Confirmed".
    * **Physical Payment**: Payment status defaults to "Pending". Counter Staff must collect and confirm the payment manually.
  * **Provide Comments**: Add pre-service comments describing the vehicle's situation (visible to Counter Staff and Technicians).
  * **✨ Kelwin AI Pre-Diagnosis**: AI-powered symptom analysis that recommends a service type (Normal/Major) and identifies possible vehicle issues based on comments.
* **My Appointments**: View all personal appointments with smart search and filtering.
  * **Active Appointments (Pending/Assigned)**: View appointment status and update comments.
  * **Completed Appointments**:
    * **View Technician Feedback**: Read the technician's post-service diagnosis and feedback (read-only).
    * **Provide Service Review**: Write and submit a service review for the completed appointment.
  * **Submit Comments/Reviews**: Save updated comments or reviews via the Submit button.
* **Payment History**: View all past payment transactions with details (amount, method, status, date).

## Counter Staff
* **Manage Customers (CRUD)**: Create, Read, Update, and Delete customer accounts.
  * **View Customer Appointments**: Drill down into a customer's individual appointments to view details, assign technicians, collect payments, or decline appointments.
* **Manage Appointments**:
  * **Create New Appointments**: Book appointments on behalf of customers with date/time pickers and service type selection.
  * **Assign Technician**: Assign a technician to an appointment with **collision-aware scheduling** (prevents double-booking by checking time overlaps — 1 hour for Normal, 3 hours for Major).
  * **Edit Service Type**: Change the service type (Normal ↔ Major) of an active appointment, which automatically recalculates the end time and updates the payment amount.
  * **Decline Appointments**: Decline an appointment (irreversible action with confirmation dialog).
  * **✨ Kelwin AI Technician Matching**: AI-powered recommendation that matches the best technician based on customer comments and technician specializations.
  * **Smart Search & Filtering**: Real-time text search across all columns + status dropdown filter using `TableRowSorter` with `RowFilter`.
* **Collect Payment**: View and manage pending physical payments.
  * **Confirm Physical Payment**: Process pending physical payments and mark them as "Confirmed".
  * **Generate Receipts**: Auto-generate a formatted `.txt` receipt file saved to `data/receipts/`.

## Technicians
* **My Appointments**: View all individually assigned appointments with search and filtering.
  * **View Appointment Details**: Open a detailed view showing customer ID, service type, scheduled times, status, and customer comments.
  * **Read Customer Comments**: Review the customer's pre-service comments describing their vehicle's situation.
  * **Provide Feedback**: Write and save post-service feedback and diagnosis for the appointment.
  * **✨ Kelwin AI Diagnostic Guide**: AI-generated step-by-step diagnostic checklist tailored to the service type and customer symptoms.
  * **✨ Kelwin AI Polish Feedback**: AI-powered feedback polisher that rewrites raw technician feedback into professional, customer-friendly language (with side-by-side comparison and accept/reject).
  * **Mark as Completed**: Update the appointment status to "Completed" (requires feedback to be written first).

## Managers
* **Manage Staff (CRUD)**: Create, Read, Update, and Delete accounts for Managers, Counter Staff, and Technicians (with specialization field for technicians).
  * **Smart Search**: Real-time filtering across the staff table.
* **Service Prices**: View and update the pricing for Normal and Major services.
* **Reports & Analytics**: Visual analytics dashboard with custom-drawn charts using `Graphics2D`:
  * **Bar Chart**: Appointment counts by status (Pending, Assigned, Completed, Declined).
  * **Pie Charts**: Service type breakdown (Normal vs Major) and payment method breakdown (Online vs Physical).
  * **Line Chart**: Monthly revenue trend over the last 6 months with gradient shading.
  * **Export CSV**: Export comprehensive management report data as a `.csv` file.
* **All Feedback**: View all technician feedback, customer comments, and customer service reviews across all appointments.
  * **✨ Kelwin AI Sentiment Analysis**: AI-powered sentiment analysis of all feedback and reviews, generating a detailed report with exportable HTML output.
  * **Export HTML**: Export the AI sentiment report as a styled `.html` file.
  * **Smart Search**: Real-time filtering across the feedback table.
* **Audit Log**: View a comprehensive, timestamped log of all significant user actions (LOGIN, REGISTER, BOOK_APPOINTMENT, ASSIGN, COMPLETE, DECLINE, PAYMENT, etc.).
  * **Smart Search**: Real-time filtering across the audit log table.

## UML Use Case Diagram
![UML Use Case Diagram](<UML Diagram/v1 UML Use Case Diagram.png>)
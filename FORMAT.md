# APU-ASC Data Format Documentation

This document describes the structure of the `.txt` files used as the database for the APU Automotive Service Centre system.

All files use a **Pipe-Delimited (`:::`)** format for fields.

## File Structure

### 1. `users.txt`
Stores all system users (Customers, Technicians, Counter Staff, Managers).
**Format:** `userId:::username:::password:::name:::email:::phone:::role:::specialization`

| Field | Description | Example |
| :--- | :--- | :--- |
| `userId` | Unique ID starting with role prefix | `CUS0001`, `TEC0002`, `STF0003`, `MGR0004` |
| `username` | Unique login username | `john_doe` |
| `password` | SHA-256 hashed password | `5e884898...` |
| `name` | Full name | `John Doe` |
| `email` | Contact email | `john@example.com` |
| `phone` | Contact number | `0123456789` |
| `role` | User role | `Customer`, `Technician`, `CounterStaff`, `Manager` |
| `specialization` | (Only for Technician) Technician's specialization | `Engine`, `Brakes`, `Electrical` |

---

### 2. `appointments.txt`
Stores all service booking records.
**Format:** `appointmentId:::customerId:::technicianId:::serviceType:::status:::dateTime:::endDateTime:::comments:::feedback:::serviceReview`

| Field | Description | Example |
| :--- | :--- | :--- |
| `appointmentId` | Unique ID starting with `APT` | `APT0001` |
| `customerId` | ID of the customer | `CUS0001` |
| `technicianId` | ID of assigned technician (optional) | `TEC0001` |
| `serviceType` | Type of service requested | `Normal`, `Major` |
| `status` | Current status | `Pending`, `Assigned`, `Completed`, `Declined` |
| `dateTime` | Scheduled start time (yyyy-MM-dd HH:mm) | `2023-12-01 10:00` |
| `endDateTime` | Scheduled end time (yyyy-MM-dd HH:mm) | `2023-12-01 11:00` |
| `comments` | Customer's initial notes | `Brakes squeaking` |
| `feedback` | Technician's internal notes | `Replaced pads` |
| `serviceReview` | Customer's final review | `Excellent!` |

---

### 3. `notifications.txt`
Stores alerts and messages for users.
**Format:** `notificationId:::targetUserId:::dateTime:::message:::isRead`

| Field | Description | Example |
| :--- | :--- | :--- |
| `notificationId` | Unique ID starting with `NTF` | `NTF0001` |
| `targetUserId` | Recipient ID, `ALL`, or `CounterStaff` | `CUS0001`, `ALL` |
| `dateTime` | Time sent (yyyy-MM-dd HH:mm) | `2023-11-30 09:00` |
| `message` | Notification content | `Service completed!` |
| `isRead` | Read status | `true` or `false` |

---

### 4. `payments.txt`
Stores all financial transactions.
**Format:** `paymentId:::appointmentId:::amount:::paymentMethod:::paymentStatus:::dateTime`

| Field | Description | Example |
| :--- | :--- | :--- |
| `paymentId` | Unique ID starting with `PAY` | `PAY0001` |
| `appointmentId` | Linked appointment ID | `APT0001` |
| `amount` | Transaction amount (RM) | `150.00` |
| `paymentMethod` | Method used | `Online`, `Physical` |
| `paymentStatus` | Current status | `Pending`, `Paid` |
| `dateTime` | Transaction time (yyyy-MM-dd HH:mm) | `2023-12-01 11:00` |

---

### 5. `service_prices.txt`
Stores the base prices for different service types.
**Format:** `serviceType:::price`

| Field | Description | Example |
| :--- | :--- | :--- |
| `serviceType` | Name of the service | `Normal`, `Major` |
| `price` | Base cost in RM | `130.0` |

### 6. `audit_log.txt`
Stores system-wide audit logs of all user actions.
**Format:** `timestamp:::userId:::action:::details`

| Field | Description | Example |
| :--- | :--- | :--- |
| `timestamp` | Time of the action (yyyy-MM-dd HH:mm) | `2026-04-10 11:32` |
| `userId` | User who performed the action (or `SYSTEM`, `UNKNOWN`) | `CUS0001` |
| `action` | Action type performed | `LOGIN_SUCCESS`, `BOOK_APPOINTMENT` |
| `details` | Contextual details about the action | `APT0003 \| Normal` |

#### Available Audit Actions:
* **Account:** `REGISTER`, `LOGIN_SUCCESS`, `LOGIN_FAILED`, `UPDATE_USER`, `DELETE_USER`
* **Appointments:** `BOOK_APPOINTMENT`, `ASSIGN_APPOINTMENT`, `UPDATE_APPOINTMENT`, `DECLINE_APPOINTMENT`, `COMPLETE_APPOINTMENT`
* **Payments:** `PROCESS_PAYMENT`, `CONFIRM_PAYMENT`, `DECLINE_PAYMENT`, `UPDATE_SERVICE_PRICE`, `GENERATE_RECEIPT`
* **Feedback:** `SUBMIT_COMMENTS`, `SUBMIT_FEEDBACK`, `SUBMIT_REVIEW`

---

## ID Format Summary

All IDs follow a prefix followed by a zero-padded 4-digit number (e.g., `0001`).

| Entity | ID Prefix | Example |
| :--- | :--- | :--- |
| **Customer** | `CUS` | `CUS0001` |
| **Technician** | `TEC` | `TEC0001` |
| **Counter Staff** | `STF` | `STF0001` |
| **Manager** | `MGR` | `MGR0001` |
| **Appointment** | `APT` | `APT0001` |
| **Payment** | `PAY` | `PAY0001` |
| **Notification** | `NTF` | `NTF0001` |

# APU Automotive Service Centre System (APU-ASC) — Program Documentation

---

## Introduction

The APU Automotive Service Centre (APU-ASC) system is a Java-based desktop application designed to digitise and streamline the end-to-end operations of an automotive service centre. The system replaces manual, paper-based workflows with a centralised platform that manages the full service lifecycle — from appointment booking and technician assignment, through payment processing and receipt generation, to feedback collection and management reporting.

### System Purpose

The core business problem is coordination: a service centre must manage interactions between four distinct user roles, each with different responsibilities and access levels. Without a unified system, appointment schedules conflict, payment records are lost, and customer feedback never reaches management. The APU-ASC system solves this by providing a single application where all four roles operate on a shared data layer with role-based access control.

### User Roles and Access Rights

The system supports **four types of end users**, each with a dedicated dashboard and tailored functionalities:

| Role | Key Responsibilities |
|------|---------------------|
| **Manager** | Create/Read/Update/Delete managers, counter staff, and technicians; set service prices (Normal / Major); view all feedback, comments, and service reviews; analyse reports with visual charts |
| **Counter Staff** | Edit personal profile; full CRUD on customer records; create and assign new appointments (with collision-aware scheduling); collect payments and generate receipts |
| **Technician** | Edit personal profile; check comments and details of assigned appointments; update appointment status to "Completed"; provide post-service feedback |
| **Customer** | Edit personal profile; access individual service and payment histories; access feedback on individual appointments; provide pre-service comments and post-service reviews |

### Technology Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Java (JDK 11+) |
| **GUI Framework** | Java Swing (`javax.swing`, `java.awt`) |
| **Data Persistence** | Flat-file `.txt` storage with pipe-delimited (`:::`) format |
| **Security** | SHA-256 password hashing (`java.security.MessageDigest`) |
| **AI Integration** | Google Gemini REST API via `java.net.http.HttpClient` |
| **Date/Time** | `java.time.LocalDateTime` with `DateTimeFormatter` |

### Architecture Overview

The codebase follows a clean **three-tier layered architecture** that separates concerns across distinct packages:

```
src/
├── Main.java                    ← Application entry point (EDT launch)
├── models/                      ← Domain entities (User, Appointment, Payment, etc.)
├── services/                    ← Business logic layer (AppointmentService, PaymentService, etc.)
├── ui/                          ← Presentation layer (Java Swing dashboards and panels)
│   ├── customer/                   ├── 4 customer screens
│   ├── staff/                      ├── 4 counter staff screens
│   ├── technician/                 ├── 3 technician screens
│   └── manager/                    └── 6 manager screens
├── utils/                       ← Cross-cutting utilities (FileHandler, AuditLogger, etc.)
└── exceptions/                  ← Custom exception classes
```

- **Models** define the data structure and validation rules for each entity
- **Services** contain all business logic, completely decoupled from the UI
- **UI** handles only presentation and user interaction, delegating all logic to services
- **Utils** provide shared infrastructure (file I/O, logging, hashing, validation)

### Data Strategy

All data is persisted in `.txt` files within the `data/` directory using a pipe-delimited (`:::`) format. The system deliberately avoids database tools (MySQL, Oracle, SQLite) as per the assignment specification. Instead, data integrity is enforced through:

- A **Singleton `FileHandler`** with `synchronized` methods for thread-safe I/O
- **Optimistic Locking** with version fields to prevent the "Lost Update" problem
- **Normalized data design** with cross-file relational joins using `HashMap` lookups

The following sections document the Object-Oriented concepts applied, the additional features implemented beyond the specification, and the system's limitations with a concluding assessment.

---

## Section 1: Description and Justification of Object-Oriented Concepts

This section documents the Object-Oriented (OO) programming concepts that have been systematically incorporated into the APU-ASC system. Each concept is described, justified with its purpose in the solution, and supported with direct implementation evidence from the source code.

---

### 1.1 Classes and Objects

**Description:**
Every real-world entity in the APU-ASC system is modelled as its own Java class. Each class serves as a blueprint that defines the entity's attributes (data) and behaviours (methods). When the system needs to work with a specific entity — for example, a customer who has just logged in — it creates an **object** (an instance) of that class to hold the live session data.

**Justification:**
Defining distinct classes ensures a clean separation of concerns: appointment logic lives in `Appointment.java`, payment logic in `Payment.java`, and user identity logic across the `User` hierarchy. This modularity makes the system easier to maintain, test, and extend — if a new entity (e.g., a "Vehicle" record) were needed in the future, a new class could be introduced without modifying existing ones.

**Implementation Evidence:**

The system defines **8 model classes** and **7 service classes**, each encapsulating a distinct domain responsibility:

| Class | Package | Purpose |
|-------|---------|---------|
| `User` | `models` | Abstract base class for all system users |
| `Customer` | `models` | Customer entity — booking, reviews, payment history |
| `Technician` | `models` | Technician entity — with specialization attribute |
| `CounterStaff` | `models` | Counter Staff entity — appointment & payment management |
| `Manager` | `models` | Manager entity — reports, staff CRUD, pricing |
| `Appointment` | `models` | Service booking record with lifecycle states |
| `Payment` | `models` | Financial transaction record |
| `Notification` | `models` | System alert/message entity |

**Code Example — Object instantiation during appointment booking (`AppointmentService.java`):**

```java
// Create a Pending appointment with no technician assigned yet
Appointment appointment = new Appointment();          // Object created from Class
appointment.setAppointmentId(newId);
appointment.setCustomerId(customerId);
appointment.setServiceType(serviceType);
appointment.setStatus(Appointment.STATUS_PENDING);
appointment.setDateTime(dateTime);
appointment.calculateEndDateTime();                   // Object behaviour
```

**Code Example — Object instantiation during user registration (`UserService.java`):**

```java
// Create the specific User object based on the role
User newUser = null;
switch (role) {
    case "Manager":
        newUser = new Manager(newId, username, null, name, email, phone);
        break;
    case "Technician":
        newUser = new Technician(newId, username, null, name, email, phone, specialization);
        break;
    case "Customer":
    default:
        newUser = new Customer(newId, username, null, name, email, phone);
        break;
}
```

---

### 1.2 Encapsulation

**Description:**
Encapsulation is the practice of declaring all class attributes as `private` and controlling access through `public` getter and setter methods. This enforces data integrity by preventing external code from directly modifying an object's internal state without validation.

**Justification:**
In a service centre system, critical data — service prices, payment amounts, user passwords — must never be set to invalid values. Encapsulation allows the system to embed validation rules directly inside the setters, ensuring that business constraints are enforced at the model level regardless of which UI or service calls the setter.

**Implementation Evidence:**

All model classes use `private` attributes with `public` getters and setters. Several setters contain embedded validation logic:

**Code Example — `Payment.java` setter with validation:**

```java
// ───── Private Attributes (Encapsulation) ─────
private String paymentId;
private String appointmentId;
private double amount;
private String paymentMethod;
private String paymentStatus;
private LocalDateTime dateTime;

// Setter with validation logic
public void setAmount(double amount) {
    if (amount < 0) {
        throw new IllegalArgumentException("Payment amount cannot be negative.");
    }
    this.amount = amount;
}

public void setPaymentMethod(String paymentMethod) {
    if (paymentMethod != null && !paymentMethod.equals("Online") && !paymentMethod.equals("Physical")) {
        throw new IllegalArgumentException("Payment method must be 'Online' or 'Physical'.");
    }
    this.paymentMethod = paymentMethod;
}
```

**Code Example — `User.java` setter with validation and password hashing:**

```java
public void setEmail(String email) {
    if (email != null && !InputValidator.isValidEmail(email)) {
        throw new IllegalArgumentException("Invalid email format.");
    }
    this.email = email;
}

public void setHashedPassword(String plainPassword) {
    if (plainPassword == null || plainPassword.trim().isEmpty()) {
        throw new IllegalArgumentException("Password cannot be empty.");
    }
    if (plainPassword.length() < 6) {
        throw new IllegalArgumentException("Password must be at least 6 characters.");
    }
    this.password = PasswordHasher.hash(plainPassword);
}
```

**Code Example — `Appointment.java` setter with validation:**

```java
public void setServiceType(String serviceType) {
    if (serviceType != null && !serviceType.equals("Normal") && !serviceType.equals("Major")) {
        throw new IllegalArgumentException("Service type must be 'Normal' or 'Major'.");
    }
    this.serviceType = serviceType;
}
```

---

### 1.3 Inheritance

**Description:**
Inheritance allows a subclass to inherit attributes and methods from a parent class, establishing an "is-a" relationship. In the APU-ASC system, the `User` class serves as the base class, and `Customer`, `Technician`, `CounterStaff`, and `Manager` are its subclasses.

**Justification:**
All users share common attributes (userId, username, password, name, email, phone, role) and common behaviours (search, toFileString, toString). Inheritance eliminates code duplication — these shared members are defined once in `User` and automatically available in every subclass. Role-specific extensions (e.g., a Technician's `specialization` field) are added only in the relevant subclass.

**Implementation Evidence:**

**Class Hierarchy:**

```
User (Abstract Base Class)
├── Customer
├── Technician       ← extends User with additional 'specialization' attribute
├── CounterStaff
└── Manager
```

**Code Example — `Customer.java` inheriting from `User`:**

```java
public class Customer extends User {

    public Customer(String userId, String username, String password, String name,
                    String email, String phone) {
        super(userId, username, password, name, email, phone, "Customer"); // Calls parent constructor
    }
}
```

**Code Example — `Technician.java` extending the base class with an additional attribute:**

```java
public class Technician extends User {

    // ───── Additional Private Attribute (Encapsulation) ─────
    private String specialization;

    public Technician(String userId, String username, String password, String name,
                      String email, String phone, String specialization) {
        super(userId, username, password, name, email, phone, "Technician");
        this.specialization = specialization;   // Extended attribute
    }

    // ───── Override toFileString to include specialization ─────
    @Override
    public String toFileString() {
        return super.toFileString() + utils.FileHandler.SEPARATOR + safe(this.specialization);
    }
}
```

---

### 1.4 Abstraction

**Description:**
Abstraction hides implementation complexity by defining an interface or abstract class that specifies *what* must be done without dictating *how*. In the APU-ASC system, the `User` class is declared as `abstract`, meaning it cannot be instantiated directly — only its concrete subclasses (Customer, Manager, etc.) can exist as objects.

**Justification:**
A generic "User" object has no meaningful behaviour on its own — it doesn't know which dashboard to display. Making `User` abstract forces the system to always create a role-specific subclass, which guarantees that every user object in memory has a concrete `displayDashboard()` implementation. This eliminates the possibility of a "typeless" user breaking the system.

**Implementation Evidence:**

**Code Example — `User.java` abstract class with abstract method:**

```java
public abstract class User {

    // Cannot be instantiated: new User() would cause a compile error

    /**
     * Displays the role-specific dashboard UI.
     * Each subclass must provide its own implementation.
     */
    public abstract void displayDashboard();

    // ... shared attributes and methods inherited by all subclasses
}
```

---

### 1.5 Polymorphism

Polymorphism is demonstrated in **two forms** within the APU-ASC system: **Method Overriding** and **Method Overloading**.

#### 1.5.1 Polymorphism — Method Overriding

**Description:**
Method Overriding allows a subclass to provide its own specific implementation of a method that is already defined in its parent class. At runtime, Java dynamically dispatches the correct version of the method based on the actual object type — not the declared variable type.

**Justification:**
The system uses a single `User` variable to hold the logged-in user. When `user.displayDashboard()` is called after login, polymorphism automatically routes the call to the correct role-specific dashboard (CustomerDashboard, ManagerDashboard, etc.) without any `if-else` or `switch` statement. This makes the login flow clean, extensible, and maintainable — adding a new role requires only creating a new subclass, not modifying the login logic.

**Implementation Evidence — Each subclass overrides `displayDashboard()`:**

```java
// Customer.java
@Override
public void displayDashboard() {
    javax.swing.SwingUtilities.invokeLater(() ->
        new ui.customer.CustomerDashboard(this).setVisible(true));
}

// Manager.java
@Override
public void displayDashboard() {
    javax.swing.SwingUtilities.invokeLater(() ->
        new ui.manager.ManagerDashboard(this).setVisible(true));
}

// Technician.java
@Override
public void displayDashboard() {
    javax.swing.SwingUtilities.invokeLater(() ->
        new ui.technician.TechnicianDashboard(this).setVisible(true));
}

// CounterStaff.java
@Override
public void displayDashboard() {
    javax.swing.SwingUtilities.invokeLater(() ->
        new ui.staff.StaffDashboard(this).setVisible(true));
}
```

**Runtime polymorphism in action during login (`UserService.java`):**

```java
User loggedInUser = parseUser(line);  // Returns Manager, Customer, Technician, etc.
// ... later in LoginFrame:
loggedInUser.displayDashboard();      // Polymorphism: correct dashboard launches automatically
```

#### 1.5.2 Polymorphism — Method Overloading

**Description:**
Method Overloading allows multiple methods with the same name but different parameter signatures to coexist within the same class. The compiler selects the correct version based on the number or type of arguments provided.

**Justification:**
The `UserService` class provides two overloaded `registerUser()` methods — one with 6 parameters (for non-technician roles) and one with 7 parameters (adding a `specialization` field for technicians). This allows calling code to use a simpler signature when the extra parameter is irrelevant, while the full signature is available when needed. The shorter method delegates to the longer one with a default empty value, keeping the business logic in a single place.

**Implementation Evidence — `UserService.java` overloaded `registerUser()` methods:**

```java
/**
 * Overloaded method for non-technician roles (Customer, Manager, CounterStaff).
 * Automatically sets specialization to an empty string.
 */
public static void registerUser(String username, String plainPassword,
        String name, String email, String phone, String role) {
    registerUser(username, plainPassword, name, email, phone, role, "");
}

/**
 * Registers a new user into the system (Customer, Manager, Technician, etc.).
 * Handles password hashing securely before saving.
 */
public static void registerUser(String username, String plainPassword,
        String name, String email, String phone, String role, String specialization) {
    // Validates uniqueness, generates ID, creates User object, hashes password, saves to file
    // ... (full implementation handles all registration logic)
}
```

**Both overloaded methods actively used across the UI layer:**

```java
// RegisterFrame.java & ManageCustomersPanel.java — uses 6-param overload (no specialization needed)
UserService.registerUser(username, password, name, email, phone, "Customer");

// ManageStaffPanel.java — uses 7-param overload for Technicians (specialization required)
UserService.registerUser(username, password, name, email, phone, role, specialization);

// ManageStaffPanel.java — uses 6-param overload for Manager/CounterStaff
UserService.registerUser(username, password, name, email, phone, role);
```

**Additional overloading — `NotificationService.java`:**

```java
// Overload 1: push notification by userId string
public static void push(String targetUserId, String message) { ... }

// Overload 2: push notification by User object (extracts userId automatically)
public static void push(models.User user, String message) {
    if (user != null && user.getUserId() != null) {
        push(user.getUserId(), message);   // Delegates to Overload 1
    }
}
```

---

### 1.6 Aggregation / Composition

**Description:**
Aggregation represents a "has-a" relationship where one class contains a reference to another. The contained object can exist independently of the container. In the APU-ASC system, the `Appointment` class holds references to both a `Customer` and a `Technician` through their IDs, illustrating how entities relate to each other beyond simple data fields.

**Justification:**
An appointment is meaningless without a customer — and optionally a technician. By storing `customerId` and `technicianId` as foreign-key-style references, the `Appointment` class models the real-world business relationship: an appointment *aggregates* a customer and a technician. This design allows appointments, customers, and technicians to be managed independently while maintaining their logical connection.

**Implementation Evidence — `Appointment.java`:**

```java
public class Appointment {

    private String appointmentId;
    private String customerId;      // Aggregation: references Customer
    private String technicianId;    // Aggregation: references Technician
    private String serviceType;
    private String status;
    private LocalDateTime dateTime;
    private LocalDateTime endDateTime;
    // ...
}
```

**The aggregated relationship is resolved at runtime in the Service and UI layers:**

```java
// AppointmentService resolves the technician object by ID
AppointmentService.assignAppointment(targetAppointment, technicianId, staffId);

// PaymentService resolves appointments to payments via appointmentId
Map<String, Payment> allPayments = PaymentService.getAllPaymentsMapByAppointment();
```

---

### 1.7 Design Patterns — Singleton Pattern

**Description:**
The Singleton pattern restricts a class to a single instance, providing a global point of access. In the APU-ASC system, the `FileHandler` class implements the Singleton pattern: its constructor is `private`, and the sole instance is obtained through the `synchronized` static method `getInstance()`.

**Justification:**
Since all data is persisted in flat `.txt` files, multiple parts of the application (AppointmentService, PaymentService, NotificationService, etc.) need to read and write to the same files. Without a Singleton, two service methods running simultaneously could open the same file, causing data corruption or file-locking crashes. The Singleton guarantees that all file I/O flows through a single `FileHandler` object, and the `synchronized` keyword on every method forces threads to take turns — preventing physical file corruption.

**Implementation Evidence — `FileHandler.java`:**

```java
public class FileHandler {

    // ───── Singleton Instance ─────
    private static FileHandler instance;

    // ───── Private Constructor (Singleton) ─────
    private FileHandler() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) { dataDir.mkdirs(); }
        createFileIfNotExists(USERS_FILE);
        createFileIfNotExists(APPOINTMENTS_FILE);
        createFileIfNotExists(PAYMENTS_FILE);
        // ... creates all data files on first launch
    }

    /**
     * Returns the single instance of FileHandler.
     */
    public static synchronized FileHandler getInstance() {
        if (instance == null) {
            instance = new FileHandler();
        }
        return instance;
    }

    // All methods are synchronized — sharing the same object lock
    public synchronized List<String> readAllLines(String filePath) { ... }
    public synchronized void writeAllLines(String filePath, List<String> lines) { ... }
    public synchronized void appendLine(String filePath, String line) { ... }
    public synchronized void updateLine(String filePath, String key, String newLine) { ... }
    public synchronized boolean updateLineOptimistic(...) { ... }
}
```

**All services access the Singleton consistently:**

```java
FileHandler.getInstance().appendLine(FileHandler.APPOINTMENTS_FILE, appointment.toFileString());
FileHandler.getInstance().updateLine(FileHandler.USERS_FILE, user.getUserId(), user.toFileString());
FileHandler.getInstance().readAllLines(FileHandler.PAYMENTS_FILE);
```

---

### Summary of OO Concepts

| # | OO Concept | Where Implemented | Key Evidence |
|---|------------|-------------------|--------------|
| 1 | **Classes and Objects** | All 8 model classes, 7 service classes | `new Appointment()`, `new Customer(...)` |
| 2 | **Encapsulation** | All models — `private` fields, validated setters | `setAmount()` rejects negatives, `setHashedPassword()` enforces min length |
| 3 | **Inheritance** | `Customer`, `Technician`, `CounterStaff`, `Manager` extend `User` | `super(...)` constructor calls, `Technician` adds `specialization` |
| 4 | **Abstraction** | `User` is `abstract` with `abstract displayDashboard()` | Cannot instantiate `new User()` directly |
| 5 | **Polymorphism (Overriding)** | `displayDashboard()` overridden in all 4 subclasses | `loggedInUser.displayDashboard()` routes to correct UI |
| 6 | **Polymorphism (Overloading)** | `UserService.registerUser()` (6-param / 7-param), `NotificationService.push(...)` | Two `registerUser()` signatures, two `push()` signatures |
| 7 | **Aggregation** | `Appointment` holds `customerId` and `technicianId` | Foreign-key references resolved in services |
| 8 | **Singleton Pattern** | `FileHandler` — private constructor + `getInstance()` | All services call `FileHandler.getInstance()` |

---

## Section 2: Additional Features

Beyond the core functionalities required by the assignment, the APU-ASC system incorporates the following advanced features to enhance reliability, security, user experience, and operational intelligence.

---

### 2.1 Collision-Aware Scheduling (Smart Technician Assignment)

**What it does:**
When Counter Staff assigns a technician to an appointment, the system automatically checks the technician's entire existing schedule for time conflicts. If the new appointment's time window overlaps with any of the technician's active bookings (accounting for 1 hour for Normal service, 3 hours for Major service), the system throws a `TechnicianUnavailableException` and blocks the assignment.

**Why it matters:**
This prevents double-booking errors that would otherwise result in two customers expecting the same technician at the same time — a critical business integrity safeguard.

**Code Evidence — `AppointmentService.java`:**

```java
for (Appointment existingAppointment : allExistingAppointments) {
    if (existingAppointment.getTechnicianId().equals(technicianId) &&
        !existingAppointment.getStatus().equals("Declined") &&
        !existingAppointment.getStatus().equals("Completed")) {

        LocalDateTime newStart = targetAppointment.getDateTime();
        LocalDateTime newEnd = targetAppointment.getEndDateTime();
        LocalDateTime existStart = existingAppointment.getDateTime();
        LocalDateTime existEnd = existingAppointment.getEndDateTime();

        boolean isOverlapping = newStart.isBefore(existEnd) && newEnd.isAfter(existStart);
        if (isOverlapping) {
            throw new TechnicianUnavailableException(
                "Technician " + technicianId + " is already booked from "
                + existStart.toLocalTime() + " to " + existEnd.toLocalTime() + ".");
        }
    }
}
```

---

### 2.2 Custom Exception Handling

**What it does:**
The system defines two domain-specific custom exceptions that extend Java's standard exception hierarchy:

| Exception | Extends | Purpose |
|-----------|---------|---------|
| `TechnicianUnavailableException` | `Exception` (Checked) | Thrown when a scheduling conflict is detected |
| `ConcurrencyException` | `RuntimeException` (Unchecked) | Thrown when an optimistic locking version mismatch is detected |

**Why it matters:**
Custom exceptions replace generic error messages with specific, actionable error types. This allows the UI layer to catch each exception type individually and present the user with a clear, context-aware error message rather than a cryptic stack trace.

**Code Evidence — `TechnicianUnavailableException.java`:**

```java
public class TechnicianUnavailableException extends Exception {
    public TechnicianUnavailableException(String message) {
        super(message);
    }
    public TechnicianUnavailableException(String technicianName, String dateTime) {
        super("Technician '" + technicianName + "' is unavailable at " + dateTime + ". Conflict detected.");
    }
}
```

---

### 2.3 Optimistic Locking (Concurrent Data Integrity)

**What it does:**
Every `Appointment` record carries a `version` field. When a user edits a record, the system captures the expected version, increments it, and calls `updateLineOptimistic()`. If two staff members load the same appointment simultaneously and both attempt to save, only the first save succeeds — the second detects that the on-disk version has already changed and throws a `ConcurrencyException`, preventing silent data loss (the "Lost Update" problem).

**Why it matters:**
In a multi-user environment, this is a critical safety mechanism. Without it, Staff B's save would silently overwrite Staff A's assignment — and nobody would ever know data was lost.

**Code Evidence — `FileHandler.java`:**

```java
public synchronized boolean updateLineOptimistic(String filePath, String key, String newLine,
                                                  int expectedVersion, int versionIndex) {
    List<String> lines = readAllLines(filePath);
    for (int i = 0; i < lines.size(); i++) {
        if (lines.get(i).startsWith(key + SEPARATOR)) {
            String[] parts = lines.get(i).split(DELIMITER, -1);
            int currentVersion = 1;
            if (parts.length > versionIndex) {
                currentVersion = Integer.parseInt(parts[versionIndex]);
            }
            if (currentVersion == expectedVersion) {
                lines.set(i, newLine);
                foundAndUpdated = true;
            } else {
                throw new ConcurrencyException("Data has been modified by another user.");
            }
            break;
        }
    }
    if (foundAndUpdated) { writeAllLines(filePath, lines); return true; }
    return false;
}
```

---

### 2.4 Password Hashing (SHA-256 Security)

**What it does:**
All user passwords are hashed using the `SHA-256` algorithm via `java.security.MessageDigest` before being written to `users.txt`. The system never stores or transmits plain-text passwords. During login, the entered password is hashed and compared to the stored hash.

**Why it matters:**
If the `users.txt` file were ever compromised, attackers would see only irreversible hash strings — not actual passwords. This is a fundamental security best practice.

**Code Evidence — `PasswordHasher.java`:**

```java
public static String hash(String password) {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] hashBytes = md.digest(password.getBytes());
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
    }
    return hexString.toString();
}

public static boolean verify(String password, String hashedPassword) {
    return hash(password).equals(hashedPassword);
}
```

---

### 2.5 Java Time API (`java.time.LocalDateTime`)

**What it does:**
All date-time operations use the modern `java.time.LocalDateTime` and `java.time.format.DateTimeFormatter` APIs instead of the legacy `java.util.Date` class. This includes appointment scheduling, end-time calculation, payment timestamps, notification timestamps, and audit logging.

**Why it matters:**
The modern Java Time API is immutable, thread-safe, and provides intuitive methods like `plusHours()`, `isBefore()`, and `isAfter()` for precise time arithmetic — essential for the collision-aware scheduling algorithm.

**Code Evidence — `Appointment.java`:**

```java
public void calculateEndDateTime() {
    if (this.dateTime != null && this.serviceType != null) {
        int hours = this.serviceType.equals("Major") ? 3 : 1;
        this.endDateTime = this.dateTime.plusHours(hours);
    }
}
```

---

### 2.6 Streams and Lambda Expressions

**What it does:**
The service layer uses Java Streams (`.stream().filter().map().collect()`) and lambda expressions extensively for data querying, filtering, and transformation. This replaces verbose `for` loops with concise, declarative pipeline expressions.

**Why it matters:**
Streams produce cleaner, more readable code and express the developer's intent more clearly — "filter all appointments for this customer" is immediately understandable.

**Code Evidence — `AppointmentService.java`:**

```java
public static List<Appointment> getAllAppointmentsForCustomer(String customerId) {
    List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.APPOINTMENTS_FILE);
    return lines.stream()
            .map(Appointment::fromFileString)                              // Method reference
            .filter(apt -> apt != null && apt.getCustomerId().equals(customerId)) // Lambda
            .collect(Collectors.toList());
}
```

**Code Evidence — `UserService.java` (filtering by type with `instanceof`):**

```java
public static List<Technician> getAllTechnicians() {
    List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.USERS_FILE);
    return lines.stream()
            .map(UserService::parseUser)
            .filter(user -> user instanceof Technician)
            .map(user -> (Technician) user)
            .collect(Collectors.toList());
}
```

---

### 2.7 Auto-Generating Receipts to Text File

**What it does:**
The `PaymentService.generateReceipt()` method automatically produces a professionally formatted `.txt` receipt file saved to `data/receipts/`. Each receipt contains a unique transaction ID, date, itemized appointment details, payment breakdown, and a branded header/footer.

**Why it matters:**
This provides an auditable, printable transaction record that Counter Staff can issue to customers, fulfilling a real-world business requirement.

**Code Evidence — `PaymentService.java`:**

```java
public static String generateReceipt(Payment payment, Appointment appointment) {
    String receiptFileName = "data/receipts/receipt_" + payment.getPaymentId() + ".txt";
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(receiptFileName))) {
        writer.write("═══════════════════════════════════════════");
        writer.write("      APU AUTOMOTIVE SERVICE CENTRE");
        writer.write("            OFFICIAL RECEIPT");
        writer.write("  Transaction ID  : " + payment.getPaymentId());
        writer.write("  TOTAL AMOUNT    : RM " + String.format("%.2f", payment.getAmount()));
        writer.write("        Thank you for your business!");
    }
    return receiptFileName;
}
```

---

### 2.8 Visual Analytics Dashboard (Graphics2D Charts)

**What it does:**
The Manager's `ReportsPanel` renders three types of custom-drawn charts directly onto `JPanel` using Java's built-in `Graphics2D` engine:
1. **Bar Chart** — Appointment counts by status (Pending, Assigned, Completed, Declined)
2. **Pie Chart** — Service type breakdown (Normal vs Major) and Payment method breakdown (Online vs Physical)
3. **Line Chart** — Monthly revenue trend over the last 6 months with gradient shading

**Why it matters:**
Visual analytics allow managers to make data-driven decisions at a glance — identifying bottlenecks (high pending counts), revenue trends, and service mix without manually counting records.

**Code Evidence — `ReportsPanel.java` (Bar Chart rendering):**

```java
static class ChartPanel extends JPanel {
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < labels.length; i++) {
            int barH = (int) (chartH * values[i] / (double) max);
            g2.setColor(colours[i]);
            g2.fillRoundRect(x, y, barW, barH, 6, 6);
        }
    }
}
```

---

### 2.9 Real-Time Notification System

**What it does:**
The system implements a complete notification pipeline: when an event occurs (appointment booked, technician assigned, payment confirmed), a notification is pushed to the relevant user(s) via `NotificationService.push()`. Each dashboard displays a bell icon with an unread count badge. Clicking the bell opens a `NotificationPanel` popup with dismissible notification cards.

The notification system supports three targeting modes:
- **Direct** — to a specific user ID (e.g., `CUS0001`)
- **Role-based** — to all users of a role (e.g., `CounterStaff`)
- **Broadcast** — to everyone (`ALL`)

Per-user read tracking is handled separately for role/broadcast notifications using `notification_reads.txt`, so one user's read action does not affect others.

**Code Evidence — `NotificationService.java`:**

```java
public static void push(String targetUserId, String message) {
    String newId = FileHandler.getInstance().generateNextId(FileHandler.NOTIFICATIONS_FILE, "NTF");
    Notification notification = new Notification(newId, targetUserId, message, LocalDateTime.now(), false);
    FileHandler.getInstance().appendLine(FileHandler.NOTIFICATIONS_FILE, notification.toFileString());
}
```

---

### 2.10 Comprehensive Audit Logging

**What it does:**
The `AuditLogger` utility records every significant user action to `data/audit_log.txt` with a timestamp, user ID, action type, and contextual details. Logged actions include: `LOGIN_SUCCESS`, `LOGIN_FAILED`, `REGISTER`, `BOOK_APPOINTMENT`, `ASSIGN_APPOINTMENT`, `COMPLETE_APPOINTMENT`, `DECLINE_APPOINTMENT`, `PROCESS_PAYMENT`, `CONFIRM_PAYMENT`, `GENERATE_RECEIPT`, `SUBMIT_FEEDBACK`, `SUBMIT_REVIEW`, `UPDATE_USER`, `DELETE_USER`, and `UPDATE_SERVICE_PRICE`.

**Why it matters:**
The audit log provides a complete, tamper-evident trail of who did what, and when — essential for accountability, dispute resolution, and regulatory compliance.

**Code Evidence — `AuditLogger.java`:**

```java
public static void log(String userId, String action, String details) {
    String timestamp = LocalDateTime.now().format(DateUtils.FORMATTER);
    String entry = timestamp + FileHandler.SEPARATOR + nullSafe(userId)
                 + FileHandler.SEPARATOR + nullSafe(action)
                 + FileHandler.SEPARATOR + nullSafe(details);
    FileHandler.getInstance().appendLine(FileHandler.AUDIT_LOG_FILE, entry);
}
```

---

### 2.11 Smart Search and Filtering (Dynamic JTable Filtering)

**What it does:**
The Counter Staff's `ManageAppointmentsPanel` implements a real-time search bar using Java Swing's `TableRowSorter` with `RowFilter`. As the user types, the `JTable` instantly filters to show only matching rows — scanning across Appointment ID, Customer ID, Technician ID, and Status columns simultaneously. No page reload or button click is required.

**Why it matters:**
When managing hundreds of appointments, instant filtering dramatically improves operational efficiency — staff can locate any record in under a second.

**Code Evidence — `ManageAppointmentsPanel.java` (Real-time filtering logic):**

```java
private void filterTable() {
    String text = tfSearch.getText().trim();
    String status = statusFilterHolder[0];

    List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

    // Text search filter (searches across ALL columns including name/phone/ID)
    if (!text.isEmpty()) {
        filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
    }

    // Status dropdown filter (column index 6 = "Status")
    if (status != null && !"All Status".equals(status)) {
        filters.add(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(status) + "$", 6));
    }

    // Apply combined filters instantly
    if (filters.isEmpty()) {
        sorter.setRowFilter(null);
    } else {
        sorter.setRowFilter(RowFilter.andFilter(filters));
    }
}
```

---

### 2.12 Gemini AI Integration ("Kelwin AI")

**What it does:**
The system integrates with the **Google Gemini AI API** through a custom `GeminiService` class, providing four AI-powered capabilities:

1. **Symptom Analysis** — Analyses customer comments to recommend a service type (Normal/Major) and identify possible vehicle issues
2. **Diagnostic Checklist** — Generates a professional step-by-step diagnostic checklist tailored to the appointment's service type and customer symptoms
3. **Feedback Polishing** — Rewrites technician's raw feedback into polished, customer-friendly language
4. **Technician Matching** — Recommends the best-fit technician based on their specialization and the customer's described symptoms
5. **Sentiment Analysis** — Analyzes all feedback/reviews across appointments and provides the Manager with an overall sentiment report, key themes, and actionable insights

The implementation includes robust error handling with **multi-model fallback** (tries multiple Gemini models sequentially), JSON escaping, and a zero-dependency Markdown-to-HTML converter for rendering AI responses in Swing components.

**Code Evidence — `GeminiService.java`:**

```java
private static String callGeminiAPI(String prompt) {
    String[] models = {"gemini-3.1-flash-lite", "gemini-3.5-flash", "gemini-2.5-flash-lite"};
    for (String model : models) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlStr))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(20))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseGeminiResponse(response.body());
            }
        } catch (Exception e) { lastError = e.getMessage(); }
    }
}
```

---

### 2.13 Centralised UI Design System (`UITheme`)

**What it does:**
The entire application's visual identity is managed through a centralised `UITheme` class (625 lines) that defines:
- A complete **dark-mode colour palette** (17 named colours)
- **Font system** (5 font tiers: Title, Header, Body, Small, Button)
- **Factory methods** for buttons (accent, secondary, danger, warning, AI), text fields, panels, tables, avatars, sidebar buttons, and custom vector icons
- **Gradient-painted components** using `Graphics2D` for premium rounded buttons and card panels with drop shadows
- **Zebra-striped table rendering** with automatic status-based colour coding (green for Completed, red for Declined, amber for Pending, blue for Assigned)

**Why it matters:**
Centralising all visual styles in a single class ensures pixel-perfect consistency across 25+ screens and eliminates scattered inline styling. Changing the application's entire colour scheme requires modifying only one file.

---

### 2.14 File Export Utility (CSV, HTML, TXT)

**What it does:**
The system provides a generic, reusable export utility that allows any tabular data or generated report to be saved directly to the user's local disk through a native `JFileChooser` dialog. The Manager's Reports panel, for example, uses this to export comprehensive management reports containing appointment summaries and revenue data as `.csv` or `.html` files.

**Why it matters:**
Rather than hardcoding export logic into every panel, the `ExportUtils` class centralizes this functionality. It handles file selection, write streams, and success/error dialogs, making it easy to export any generated string (HTML, CSV, or raw text) anywhere in the application.

**Code Evidence — `ExportUtils.java`:**

```java
public class ExportUtils {
    /**
     * Prompts the user to save a string of content (HTML, CSV, TXT) to a file.
     */
    public static void exportStringToFile(Component parent, String defaultFileName, String content) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Export As...");
        fileChooser.setSelectedFile(new File(defaultFileName));
        
        int userSelection = fileChooser.showSaveDialog(parent);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(fileToSave)) {
                fw.write(content);
                JOptionPane.showMessageDialog(parent, 
                    "Export successful!\nSaved to: " + fileToSave.getAbsolutePath(), 
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                // handles IO exception
            }
        }
    }
}
```

---

### 2.15 Cross-File Relational Data Joining (Normalized Architecture)

**What it does:**
The system's data is **normalized** across separate `.txt` files — `appointments.txt` stores only `customerId` (not the customer's name or phone), and `payments.txt` stores only `appointmentId` (not the service type or customer). When the UI needs to display a rich table combining data from multiple files, the system performs **in-memory relational joins** by building `HashMap` lookup tables and resolving foreign keys at runtime — exactly like a `SQL JOIN` across database tables, but implemented manually over flat files.

**Why it matters:**
This design avoids data duplication (a customer's name is stored in only one place — `users.txt`). If a customer updates their profile, every appointment table automatically reflects the new name on the next refresh, because the system always resolves the latest data from the source file. Without this pattern, the system would need to store redundant copies of customer names in every file, leading to stale data and synchronisation bugs.

**Code Evidence — `ManageAppointmentsPanel.java` (`refresh()` method):**

```java
void refresh() {
    appointments = AppointmentService.getAllAppointments();

    // Build a lookup map: customerId -> Customer for fast name/phone resolution
    // Data from: users.txt
    Map<String, Customer> customerMap = UserService.getAllCustomersMap();

    // Build a lookup map: appointmentId -> Payment for payment status resolution
    // Data from: payments.txt
    paymentMap = PaymentService.getAllPaymentsMapByAppointment();

    for (Appointment apt : appointments) {
        // Cross-file JOIN #1: appointments.txt ↔ users.txt (via customerId)
        User cust = customerMap.get(apt.getCustomerId());
        String custName  = cust != null ? cust.getName()  : "";
        String custPhone = cust != null ? cust.getPhone() : "";

        // Cross-file JOIN #2: appointments.txt ↔ payments.txt (via appointmentId)
        Payment payment = paymentMap.get(apt.getAppointmentId());
        String payStatus = payment != null ? payment.getPaymentStatus() : "No Payment";

        // The table row now combines data from 3 separate files:
        tableModel.addRow(new Object[]{
            apt.getAppointmentId(), apt.getCustomerId(),
            custName, custPhone,         // ← from users.txt
            apt.getTechnicianId(),       // ← from appointments.txt
            apt.getServiceType(),
            apt.getStatus(),
            apt.getDateTime(),
            apt.getEndDateTime(),
            apt.getComments(),
            payStatus                    // ← from payments.txt
        });
    }
}
```

**Code Evidence — `UserService.java` (HashMap factory for O(1) lookups):**

```java
public static Map<String, Customer> getAllCustomersMap() {
    List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.USERS_FILE);
    return lines.stream()
            .map(UserService::parseUser)
            .filter(user -> user instanceof Customer)
            .map(user -> (Customer) user)
            .collect(Collectors.toMap(Customer::getUserId, c -> c, (a, b) -> b));
}
```

**Code Evidence — `PaymentService.java` (HashMap factory for O(1) lookups):**

```java
public static Map<String, Payment> getAllPaymentsMapByAppointment() {
    List<String> lines = FileHandler.getInstance().readAllLines(FileHandler.PAYMENTS_FILE);
    return lines.stream()
            .map(Payment::fromFileString)
            .filter(payment -> payment != null)
            .collect(Collectors.toMap(
                    Payment::getAppointmentId,             // Key: appointmentId
                    payment -> payment,                    // Value: Payment object
                    (existing, replacement) -> replacement // Merge: keep latest
            ));
}
```

This pattern is used across multiple panels: `ManageAppointmentsPanel`, `CollectPaymentPanel`, and `ReportsPanel` — wherever the UI needs to display combined data from different data sources.

---

### Summary of Additional Features

| # | Feature | Key Technology |
|---|---------|---------------|
| 1 | Collision-Aware Scheduling | `LocalDateTime` interval overlap detection |
| 2 | Custom Exception Handling | `TechnicianUnavailableException`, `InsufficientFundsException`, `ConcurrencyException` |
| 3 | Optimistic Locking | Version-based conflict detection in `FileHandler` |
| 4 | Password Hashing | `java.security.MessageDigest` SHA-256 |
| 5 | Java Time API | `java.time.LocalDateTime`, `DateTimeFormatter` |
| 6 | Streams & Lambdas | `.stream().filter().map().collect()` pipelines |
| 7 | Auto-Generated Receipts | `BufferedWriter` formatted `.txt` file output |
| 8 | Visual Analytics Dashboard | `Graphics2D` bar, pie, and line charts |
| 9 | Real-Time Notifications | Push/read/dismiss notification pipeline with per-user tracking |
| 10 | Audit Logging | Append-only timestamped action log |
| 11 | Smart Search & Filtering | `TableRowSorter` + `RowFilter` on `JTable` |
| 12 | Gemini AI Integration | `java.net.http.HttpClient` REST API with multi-model fallback |
| 13 | Centralised UI Theme | 625-line `UITheme` design system with `Graphics2D` |
| 14 | CSV Report Export | `ExportUtils` with `JFileChooser` |
| 15 | Cross-File Relational Joins | `HashMap`-based normalized data resolution across `.txt` files |

---

## Section 3: Limitations and Conclusion

### 3.1 Limitations

Despite the system's comprehensive feature set, there are several limitations that should be acknowledged for transparency and to inform future development:

#### 3.1.1 Flat-File Database Persistence
The system stores all data in pipe-delimited `.txt` files rather than a relational database (e.g., MySQL, SQLite). While the Singleton + Optimistic Locking architecture mitigates data corruption and lost updates, flat files have inherent scalability limitations:
- **No indexing** — Every query performs a linear scan of the entire file. As data grows to thousands of records, read operations will slow progressively.
- **No transaction support** — Operations like "book appointment + create payment" are not atomic; a crash between the two writes could leave the data in an inconsistent state.
- **No relational integrity** — There is no foreign-key enforcement at the storage level. If a user is deleted while they still have active appointments, orphaned records may remain.

#### 3.1.2 Single-Machine Deployment
The application runs as a standalone Java Swing desktop application. It does not support networked multi-user access across different machines. Two staff members would need to use the same physical computer (or share the `data/` directory over a network drive) to benefit from the concurrency protections.

#### 3.1.3 Limited Authentication Security
While passwords are hashed with SHA-256, the system does not implement:
- **Salting** — Identical passwords produce identical hashes, making the system vulnerable to rainbow table attacks.
- **Session timeout** — Once logged in, the session remains active indefinitely until the user manually logs out.
- **Account lockout** — There is no mechanism to lock an account after repeated failed login attempts.

#### 3.1.4 No Image or Document Attachments
Customers cannot upload photos of their vehicle issues, and technicians cannot attach diagnostic reports. All communication is text-based through the comments, feedback, and review fields.

#### 3.1.5 AI Feature Dependency on External API
The Gemini AI features require an active internet connection and a valid API key. If the API is unavailable, rate-limited, or deprecated, these features gracefully degrade but become non-functional. The core system operations (booking, assignment, payment) remain fully operational without AI.

---

### 3.2 Conclusion

The APU Automotive Service Centre System (APU-ASC) successfully delivers a complete, production-quality service management platform that fulfils all core functional requirements while incorporating a significant portfolio of advanced features that go well beyond the minimum specification.

**From an Object-Oriented perspective**, the system demonstrates a mature application of **eight OO concepts**: Classes & Objects, Encapsulation with validation logic, Inheritance through a well-structured `User` hierarchy, Abstraction via the abstract `User` class, both forms of Polymorphism (Method Overriding for dashboard routing and Method Overloading for flexible search), Aggregation through entity cross-referencing, and the Singleton design pattern for thread-safe file management.

**From a systems architecture perspective**, the codebase follows a clean **three-tier layered architecture**:
- **Models Layer** (`models/`) — Encapsulated domain entities with serialisation logic
- **Service Layer** (`services/`) — Business logic completely separated from UI concerns
- **UI Layer** (`ui/`) — Java Swing presentation with a centralised `UITheme` design system

This separation of concerns ensures that the business rules (e.g., collision detection, payment processing) are reusable and testable independently of the GUI.

**From a feature perspective**, the system goes significantly beyond the assignment scope with collision-aware scheduling, optimistic locking for data integrity, SHA-256 password hashing, custom exception handling, a full audit logging subsystem, real-time notifications, visual analytics dashboards using `Graphics2D`, auto-generated receipt files, dynamic table search/filtering, CSV report export, and a Gemini AI integration providing symptom analysis, diagnostic checklists, feedback polishing, technician matching, and sentiment analysis.

The system is robust, well-documented, and architecturally sound — representing a holistic demonstration of Object-Oriented programming principles applied to a real-world business problem.

---

*End of Program Documentation.*

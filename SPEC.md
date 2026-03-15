# APU Automotive Service Centre (APU-ASC) - Advanced Features & OOP Blueprint

## Part 1: Advanced "Wow Factor" Features
These features are designed to secure the highest distinction marks by fulfilling the "Performance result of all operations", "User input validation", and "Additional features" grading criteria.

### 1. Robust Logic & "Smart" Features
* **Collision-Aware Scheduling**: Implement logic that validates a technician’s schedule before allowing an assignment. If a technician is booked during the required hours (1 hour for normal, 3 hours for major), the system should "gray out" their name or display a "Conflict Detected" warning.
* **Auto-Generating Receipts to Text**: Create a `ReceiptGenerator` class that automatically produces a professionally formatted `.txt` file containing a unique transaction ID, date, and itemized costs.
* **Smart Search & Filtering**: Implement a `RowFilter` for your `JTable` in the Manager or Counter Staff views, allowing staff to type a name and instantly see matching records filter on the screen without reloading the text file.
* **Robust Data Parsing**: Avoid messy string splitting by using clean CSV parsing logic or a lightweight library like **Gson** to store your `.txt` data in JSON format, guaranteeing error-free file operations.

### 2. "Cool" GUI Enhancements (Java Swing)
Since the project is restricted to `java.awt` and `java.swing` packages, advanced graphical components will make the application stand out:
* **Visual Analytics Dashboard**: Use Java's built-in `Graphics2D` class (or the **JFreeChart** library, if permitted) to draw simple bar or pie charts directly onto a `JPanel` for the Manager's "Analyse reports" requirement. This fulfills the rubric's reward for using diagrams and charts.
* **Real-Time "Toast" Notifications**: Create a self-fading "Toast" notification window using a `javax.swing.Timer` or a background thread to quietly poll your `notifications.txt` file. This provides live updates (e.g., when a new appointment is assigned) without requiring a "Refresh" button or using intrusive `JOptionPane` popups.

### 3. Modern Java Logic (The "Pro" Touch)
Demonstrating modern Java satisfies the requirement to show more than three advanced Object-Oriented (OO) concepts:
* **Custom Exception Handling**: Create custom exceptions like `TechnicianUnavailableException` or `InsufficientFundsException` to showcase a deep understanding of Java's error handling.
* **Java Time API (`java.time`)**: Use `LocalDateTime` and `Duration` instead of the outdated `Date` class to precisely calculate service end times.
* **Streams and Lambda Expressions**: Use `.stream().filter()` logic when searching through data loaded from text files to make the code cleaner.
* **Advanced OO Design Patterns**: Implement the **Singleton Pattern** for your File Handling class to prevent file-locking crashes, and use the **Factory Pattern** to instantiate your User objects.
* **Password Hashing**: Use `java.security.MessageDigest` to hash passwords (e.g., SHA-256) before saving them to the text file.

> **Crucial Advice for your Final Report:** You **must** explicitly document these features in your report under a section titled *"Description and Evidence of Additional Features"* to claim the specific marks allocated for them.

---

## Part 2: Core Object-Oriented (OO) Concepts
To hit the distinction tier, you must effectively implement and document these core concepts.

### 1. Classes and Objects
* **Implementation**: Every entity in the system—Manager, CounterStaff, Technician, Customer, Appointment, and Payment—should be defined as its own **Class**.
* **Usage**: When a user logs in, instantiate an **Object** of that specific class to hold their session data.

### 2. Inheritance
* **Implementation**: Create a base class called `User` that contains shared attributes like username, password, name, and email. 
* **Usage**: Have `Manager`, `CounterStaff`, `Technician`, and `Customer` inherit from `User` to avoid repeating code and demonstrate a professional hierarchy.

### 3. Encapsulation
* **Implementation**: Set all class attributes (e.g., `servicePrice` or `customerID`) to **private**.
* **Usage**: Use **public Getter and Setter** methods. For example, the setter for `setServicePrices` can include validation logic to ensure the price is not negative before saving it to the `.txt` file.

### 4. Polymorphism
* **Method Overriding**: Create an abstract method or an interface called `displayDashboard()`. Each user type provides its own version of the dashboard, so calling `user.displayDashboard()` automatically triggers the correct UI based on the object type.
* **Method Overloading**: Implement two `search()` methods: one that takes a `String` (name) and one that takes an `int` (id), allowing users to search by either parameter.

### 5. Advanced Concepts (The "Distinction" Boost)
* **Abstraction**: Make the `User` class an Abstract class so that a generic user cannot be created, ensuring only specific roles like `Customer` or `Manager` can exist.
* **Aggregation/Composition**: Design the `Appointment` class to "contain" a `Customer` object and a `Technician` object, illustrating how entities relate to each other beyond just using IDs.
* **Design Patterns (Singleton)**: Use a **Singleton** for your file handler class to ensure only one part of your program writes to a `.txt` file at a time, preventing data loss.
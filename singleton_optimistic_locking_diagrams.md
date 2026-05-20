# Singleton + Optimistic Locking Architecture

## 1. Singleton Pattern — Class Diagram

Shows how `FileHandler` enforces a single shared instance across the entire application.

```mermaid
classDiagram
    class FileHandler {
        -static FileHandler instance
        -FileHandler()
        +static synchronized getInstance() FileHandler
        +synchronized readAllLines(filePath) List~String~
        +synchronized writeAllLines(filePath, lines) void
        +synchronized appendLine(filePath, line) void
        +synchronized updateLine(filePath, key, newLine) void
        +synchronized updateLineOptimistic(filePath, key, newLine, expectedVersion, versionIndex) boolean
        +synchronized deleteLine(filePath, key) void
        +synchronized findLineByKey(filePath, key) String
        +synchronized generateNextId(filePath, prefix) String
        -createFileIfNotExists(filePath) void
    }

    class AppointmentService {
        +static bookAppointment(apt) void
        +static assignAppointment(apt, techId) void
        +static completeAppointment(apt) void
        +static declineAppointment(apt) void
    }

    class FeedbackService {
        +static submitTechnicianFeedback(apt, feedback) void
        +static submitServiceReview(apt, review) void
        +static submitCustomerComments(apt, comments) void
    }

    class NotificationService {
        +static markAsRead(notification, userId, role) void
    }

    AppointmentService --> FileHandler : uses getInstance()
    FeedbackService --> FileHandler : uses getInstance()
    NotificationService --> FileHandler : uses getInstance()

    note for FileHandler "Private constructor + static synchronized getInstance()\n= Only ONE instance exists in memory.\nAll synchronized methods share the SAME lock."
```

---

## 2. Singleton Access — All Services Share One Lock

```mermaid
flowchart TB
    subgraph Services["Service Layer"]
        AS["AppointmentService"]
        FS["FeedbackService"]
        NS["NotificationService"]
    end

    subgraph Singleton["FileHandler (Singleton)"]
        GI["getInstance()"]
        LOCK["🔒 Object Monitor Lock"]
        subgraph Methods["synchronized Methods"]
            RL["readAllLines()"]
            WL["writeAllLines()"]
            AL["appendLine()"]
            UL["updateLine()"]
            ULO["updateLineOptimistic()"]
        end
    end

    subgraph Files["Data Files (data/)"]
        F1["appointments.txt"]
        F2["users.txt"]
        F3["payments.txt"]
        F4["notifications.txt"]
    end

    AS -->|"getInstance()"| GI
    FS -->|"getInstance()"| GI
    NS -->|"getInstance()"| GI
    GI --> LOCK
    LOCK --> Methods
    RL --> Files
    WL --> Files
    AL --> Files
    UL --> Files
    ULO --> Files
```

---

## 3. Optimistic Locking — Success Scenario (Sequence Diagram)

Two users load the same appointment, but **User A saves first** and succeeds. User B also succeeds because they save a different appointment.

```mermaid
sequenceDiagram
    participant UA as 👤 Counter Staff A
    participant App as Application<br/>(AppointmentService)
    participant FH as FileHandler<br/>(Singleton)
    participant DB as 📁 appointments.txt

    Note over DB: APT0001 version = 1

    UA->>App: assignAppointment(apt, "TEC001")
    App->>App: expectedVersion = apt.getVersion() → 1
    App->>App: apt.setVersion(2)
    App->>FH: updateLineOptimistic(key, newLine, expectedVersion=1, index=10)
    
    FH->>DB: readAllLines()
    DB-->>FH: [APT0001:::...:::1]
    FH->>FH: Parse version from file → currentVersion = 1
    FH->>FH: Check: currentVersion(1) == expectedVersion(1)? ✅ YES
    FH->>DB: writeAllLines() with version = 2
    DB-->>FH: ✅ Write successful
    FH-->>App: return true
    App-->>UA: ✅ "Assigned to TEC001 successfully."

    Note over DB: APT0001 version = 2
```

---

## 4. Optimistic Locking — Conflict Scenario (Sequence Diagram)

Two users load the same appointment **at the same time**. User A saves first. When User B tries to save, the version has changed — **conflict detected!**

```mermaid
sequenceDiagram
    participant UA as 👤 Counter Staff A
    participant UB as 👤 Counter Staff B
    participant App as Application<br/>(AppointmentService)
    participant FH as FileHandler<br/>(Singleton)
    participant DB as 📁 appointments.txt

    Note over DB: APT0001 version = 1

    UA->>App: Load appointment APT0001
    App->>FH: readAllLines()
    FH->>DB: read file
    DB-->>FH: [APT0001:::...:::1]
    FH-->>App: return lines
    App-->>UA: apt.version = 1

    UB->>App: Load appointment APT0001
    App->>FH: readAllLines()
    FH->>DB: read file
    DB-->>FH: [APT0001:::...:::1]
    FH-->>App: return lines
    App-->>UB: apt.version = 1

    Note over UA,UB: Both users now have version = 1 in memory

    rect rgb(40, 120, 40)
        Note right of UA: Staff A saves FIRST
        UA->>App: assignAppointment(apt, "TEC001")
        App->>App: expectedVersion = 1, setVersion(2)
        App->>FH: updateLineOptimistic(..., expectedVersion=1)
        FH->>FH: currentVersion(1) == expectedVersion(1)? ✅
        FH->>DB: Write with version = 2
        FH-->>App: return true
        App-->>UA: ✅ Success!
    end

    Note over DB: APT0001 version = 2

    rect rgb(120, 40, 40)
        Note right of UB: Staff B saves SECOND
        UB->>App: assignAppointment(apt, "TEC002")
        App->>App: expectedVersion = 1, setVersion(2)
        App->>FH: updateLineOptimistic(..., expectedVersion=1)
        FH->>FH: currentVersion(2) ≠ expectedVersion(1)? ❌
        FH-->>App: throw ConcurrencyException
        App-->>UB: ❌ "Data has been modified by another user."
    end
```

---

## 5. updateLineOptimistic — Internal Flowchart

```mermaid
flowchart TD
    START(["updateLineOptimistic(filePath, key, newLine, expectedVersion, versionIndex)"])
    
    READ["Read all lines from file"]
    LOOP{"For each line:<br/>Does line start with key?"}
    NEXT["Move to next line"]
    SPLIT["Split line by ':::'"]
    
    CHECK_LENGTH{"parts.length > versionIndex?"}
    PARSE["Parse version from parts[versionIndex]"]
    DEFAULT["Use default: currentVersion = 1<br/>(old record without version field)"]
    
    COMPARE{"currentVersion == expectedVersion?"}
    
    SUCCESS["Replace line in list:<br/>lines.set(i, newLine)<br/>foundAndUpdated = true"]
    FAIL["throw ConcurrencyException<br/>'Data has been modified<br/>by another user'"]
    
    WRITE["Write all lines back to file"]
    RETURN_TRUE(["return true ✅"])
    RETURN_FALSE(["return false<br/>(key not found)"])

    START --> READ
    READ --> LOOP
    LOOP -->|"No match"| NEXT
    NEXT --> LOOP
    LOOP -->|"Match found!"| SPLIT
    SPLIT --> CHECK_LENGTH
    CHECK_LENGTH -->|"Yes"| PARSE
    CHECK_LENGTH -->|"No"| DEFAULT
    PARSE --> COMPARE
    DEFAULT --> COMPARE
    COMPARE -->|"✅ Versions match"| SUCCESS
    COMPARE -->|"❌ Versions differ"| FAIL
    SUCCESS --> WRITE
    WRITE --> RETURN_TRUE
    LOOP -->|"No more lines"| RETURN_FALSE

    style SUCCESS fill:#2d6a2d,color:#fff
    style FAIL fill:#8b2020,color:#fff
    style RETURN_TRUE fill:#2d6a2d,color:#fff
    style RETURN_FALSE fill:#555,color:#fff
```

---

## Summary

| Design Pattern | Where | Purpose |
|---|---|---|
| **Singleton** | `FileHandler.getInstance()` | Guarantees only ONE object manages all file I/O, so all `synchronized` methods share the same lock |
| **Coarse-Grained Locking** | All `synchronized` methods in `FileHandler` | Prevents physical file corruption by forcing threads to take turns at the I/O level |
| **Optimistic Locking** | `updateLineOptimistic()` | Detects when two users try to edit the same record, preventing the "Lost Update" problem without blocking users from working in parallel |

---

## 6. Why Do We Need These? (The Problem & The Solution)

Understanding *why* these patterns are used is just as important as knowing how they work. Here is a breakdown of what issues they solve and what happens if you skip them.

### 6.1. The Singleton Pattern & Synchronization
**The Issue it Solves:** Physical File Corruption when multiple parts of the app try to use the hard drive at the same time.

**What Happens If We Don't Have It? (Likely Crash or Corruption):**
If two different parts of your system try to save to `appointments.txt` at the exact same millisecond:
* **The Crash:** Java might throw an error and crash (e.g., "The process cannot access the file because it is being used by another process").
* **The Corruption:** Sometimes it doesn't crash, but both saves write to the file at the exact same time. The text file ends up looking like gibberish (e.g., half of one line merged with another). Your system is now permanently broken because it cannot read the data files anymore.

**How It Solves the Issue (The "Exact Same Microsecond" Scenario):** 
* **Singleton:** Ensures there is only *one* `FileHandler` object. All threads are forced to use this single object.
* **`synchronized` (The Lock):** If User A and User B click "Save" at the **exact same microsecond**, they both arrive at the `FileHandler` at the same time. The `synchronized` keyword acts as a physical lock on the door. It forces a tiny race at the CPU level. 
  * User A wins the lock by a nanosecond and goes inside to save.
  * User B does **not** crash. Java simply forces User B to wait outside the locked door.
  * When User A finishes and exits, the door unlocks. User B is then allowed inside to do their save safely.

### 6.2. Optimistic Locking
**The Issue it Solves:** The "Lost Update" Problem (Silent Data Loss). This happens when two users open the same record, both make changes, and both save.

**What Happens If We Don't Have It? (Silent Data Loss):**
This is extremely dangerous because the system *looks* like it's working perfectly fine and never crashes.
* Staff A and Staff B load Appointment #001 at the same time.
* Staff A assigns it to Technician X and saves.
* A second later, Staff B assigns it to Technician Y and saves.
* **Result:** Staff B just silently wiped out Staff A's work. The system continues running happily, but your business data is now wrong. You won't know there is a problem until an angry customer complains.

**How It Solves the Issue (The "Exact Same Microsecond" Scenario):** 
It adds a `version` number to every line in the text file (e.g., `APT001:::...:::version=1`).
If Staff A and Staff B loaded **version 1** on their screens, and both click "Save" at the **exact same microsecond**:
1. Because `updateLineOptimistic` is a `synchronized` method, they are forced to line up at the locked door (as explained in 6.1). 
2. **Staff A wins the lock:** Staff A goes inside, sees the file is still at version 1, successfully saves their changes, updates the file to **version 2**, and leaves.
3. **Staff B's turn:** The door unlocks and Staff B goes inside. Staff B says, *"I want to update version 1."*
4. The system checks the file and says: *"Wait, the file is already at version 2 now! If I let you save, you will overwrite Staff A's work."* 
5. Instead of overwriting the data, it stops Staff B and shows an error: *"Data has been modified by another user."*

*(Notice how Singleton prevents them from crashing the file, and Optimistic Locking prevents them from deleting each other's data!)*

# NU Attendance System
**Bluetooth-driven automated classroom attendance for FAST-NU**

The **NU Attendance System** is a modern web application designed for FAST-NU to streamline and automate classroom attendance using student device Bluetooth MAC address verification and local location checks. It provides teachers with a real-time live feed of incoming students and automates the record-keeping process.

---

## ✨ High-Level Features

*   **🔒 Proximity-Based Verification (No Proxies):** Prevents proxy attendance by restricting check-ins based on local network/Bluetooth proximity. **Students who are far away or outside the classroom cannot mark their attendance.**
*   **⚡ Real-Time Live Feed:** When a student pings/checks in from their device, their name and roll number instantly appear on the teacher's screen in real time using WebSockets.
*   **📊 Excel Report Generation:** Generate and download comprehensive attendance history reports in `.xlsx` format (powered by SheetJS) with a single click. The spreadsheet lists all students, dates, and summarizes effective presents/absents/lates.
*   **⚙️ Manual Overrides:** Teachers have full control and can manually toggle student status (Present, Absent, Late) in case of device issues or special circumstances.
*   **🌓 Dual-Theme Interface:** Includes a fully responsive UI with a premium dark/light mode toggle.

---

## 🛠️ Technologies Used

### Backend
*   **Java 17+**
*   **Spring Boot** (Core Framework)
*   **Spring Security** with **JWT** (Stateless authentication)
*   **Spring Data JPA** (Database integration)
*   **Spring WebSockets (STOMP / SockJS)** (Real-time live feeds)

### Frontend
*   **HTML5 & CSS3** (Vanilla responsive SPA UI with custom dark mode theme)
*   **JavaScript (ES6)** (Dynamic DOM manipulation & WebSocket clients)
*   **SheetJS (XLSX)** (For exporting attendance statistics into Excel sheets)
*   **FingerprintJS** (For unique client device identification)

### Networking & Tunneling
*   **ngrok** (Exposes the local web server to external networks for mobile access)

---

## 📋 Prerequisites
Before running the system, make sure you have the following installed and configured:

1. **Java Development Kit (JDK 17 or higher)**
   * Verify using: `java -version`
2. **Apache Maven (3.6 or higher)**
   * Verify using: `mvn -version`
3. **Microsoft SQL Server (Local DB Instance)**
   * Ensure that your SQL Server instance (e.g. `SQLEXPRESS` or `SQLEXPRESS01`) is running.
   * Create a blank database named **`nuattendance`**.
   * Make sure your SQL Server connection details match the settings in [application.properties](file:///c:/Users/Faisal%20Butt/Desktop/nu-attendance-system/attendance-system/src/main/resources/application.properties):
     ```properties
     spring.datasource.url=jdbc:sqlserver://localhost;databaseName=nuattendance;trustServerCertificate=true;
     spring.datasource.username=nu_user
     spring.datasource.password=password123
     ```
4. **ngrok Account (Required for Mobile Access)**
   * Sign up at [ngrok.com](https://ngrok.com) to get a free account.
   * Retrieve your **Authtoken** from your ngrok dashboard.

---

## 🚀 Step-by-Step Execution Guide

To run and test the complete system, you will need to open **two separate terminal windows**.

### 💻 Terminal 1: Start the Spring Boot Web Server
This terminal runs the main application backend and local frontend for the teacher.

1. Open your first terminal and navigate to the project directory:
   ```powershell
   cd nu-attendance-system
   cd attendance-system
   ```
2. Build and launch the Spring Boot app:
   ```powershell
   mvn spring-boot:run
   ```
3. Wait for the application to start. You will see a message like `Started AttendanceApplication in ... seconds` in the log.
4. **Teacher Interface:** On the teacher's laptop, open your web browser and go to:
   **`http://localhost:8080`**

---

### 📱 Terminal 2: Expose to the Internet via ngrok
This terminal runs the ngrok tunnel so that students can access the portal and mark their attendance using their own mobile phones in the classroom.

1. Open your second terminal and navigate to the same project directory (where `ngrok.exe` is located):
   ```powershell
   cd nu-attendance-system
   cd attendance-system
   ```
2. **(One-Time Setup)** Add your ngrok authtoken to the configuration:
   ```powershell
   .\ngrok.exe config add-authtoken <YOUR_NGROK_AUTHTOKEN>
   ```
3. Start the HTTP tunnel on port `8080`:
   ```powershell
   .\ngrok.exe http 8080
   ```
4. Find the public URL generated in the ngrok output (for example, `https://fax-rake-askew.ngrok-free.dev` or `https://a1b2-34-56-78.ngrok-free.app`).
5. **Student Interface:** On the students' mobile phones, open their browser and go to the generated **ngrok URL** (e.g., `https://fax-rake-askew.ngrok-free.dev`).

---

## 🔑 Sample Demo Logins

You can log into the system using the following seeded credentials:

| Role | Username | Password | Notes / Interface |
| :--- | :--- | :--- | :--- |
| 🧑‍🏫 **Teacher** | `zeeshanrana` | `password123` | Log in on the **Teacher's Laptop** (`http://localhost:8080`). |
| 🧑‍🎓 **Student (BSE-4A)** | `24l-3068` | `pass123` | Log in on a **Student's Phone** (via the public ngrok URL). |
| 🧑‍🎓 **Student (BSE-4A)** | `24l-3052` | `pass123` | Log in on a **Student's Phone** (via the public ngrok URL). |

---

## 🛠️ How the System Works

### Architecture
```
Browser (Teacher)          Spring Boot (Java 21)          Browser (Student)
     |                           |                               |
     |-- POST /session/start --> |                               |
     |                           |-- Init attendance records     |
     |<-- sessionId + WS sub --- |                               |
     |                           |                               |
     |    [WS /topic/bluetooth/sessionId]                       |
     |                           |<-- POST /student/ping --------|
     |                           |-- Mark present in DB          |
     |<-- WS event (name/roll) --|                               |
     |                           |                               |
     |-- POST /session/submit -> |                               |
     |<-- Summary (present/absent)|                              |
```

### Bluetooth Flow (How Device → Student Mapping Works)

**The problem:** Web browsers can't read raw Bluetooth MAC addresses.

**The solution implemented:**
1. **Device Registration** — Student visits "My Device" tab, enters their MAC address once. This is stored in DB: `MAC → Roll Number`.
2. **Teacher starts BT session** — Server marks all section students as ABSENT, opens WebSocket channel.
3. **Student presses "Mark Present"** — Their JWT-authenticated request hits `/api/student/attendance/ping`. The server knows WHO they are (from JWT), looks up their MAC, marks them present, and broadcasts their name via WebSocket to the teacher's screen in real-time.
4. **Teacher sees names appear live** — Each student who pings shows up on the teacher's live feed.
5. **Teacher submits** — Final attendance locked.

**For real Bluetooth (beyond web):** You'd add a Python/Node scanner on the teacher's laptop:
```python
# bluetooth_scanner.py (requires bluetoothctl / pybluez)
import bluetooth, requests

TOKEN = "teacher_jwt_here"
SESSION_ID = 123

nearby = bluetooth.discover_devices(lookup_names=True, duration=8)
for addr, name in nearby:
    requests.post(f"http://localhost:8080/api/teacher/bluetooth/detect",
                  json={"mac": addr, "sessionId": SESSION_ID},
                  headers={"Authorization": f"Bearer {TOKEN}"})
```
This would auto-detect phones/laptops without students pressing anything.

---

## 📌 API Reference

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/teacher/login` | Teacher login → JWT |
| POST | `/api/auth/student/login` | Student login → JWT |
| POST | `/api/auth/student/register-device` | Register BT MAC |
| GET  | `/api/auth/me` | Get current user info |

### Teacher
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/teacher/session/start` | Start session |
| GET  | `/api/teacher/sessions` | My sessions |
| GET  | `/api/teacher/students/{section}` | Students by section |
| POST | `/api/teacher/attendance/manual` | Submit manual attendance |
| POST | `/api/teacher/session/{id}/submit` | Finalize session |
| GET  | `/api/teacher/session/{id}/summary` | Get summary |

### Student
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/student/attendance/ping` | Bluetooth ping (mark present) |
| GET  | `/api/student/attendance/history` | My attendance history |
| GET  | `/api/student/sessions/active` | Active sessions for my section |

### WebSocket
- Endpoint: `ws://localhost:8080/ws` (SockJS)
- Subscribe: `/topic/bluetooth/{sessionId}` → receives `BluetoothDetectedEvent` when a student pings

---

## 📂 Project Structure
```
attendance-system/
├── pom.xml
└── src/main/
    ├── java/pk/edu/nu/attendance/
    │   ├── AttendanceApplication.java      # Entry point
    │   ├── config/
    │   │   ├── JwtUtil.java                # JWT generate/validate
    │   │   ├── SecurityConfig.java         # Spring Security + CORS + JWT filter
    │   │   └── WebSocketConfig.java        # STOMP WebSocket
    │   ├── controller/
    │   │   ├── AuthController.java         # Login + device registration
    │   │   ├── TeacherController.java      # All teacher endpoints
    │   │   └── StudentController.java      # All student endpoints
    │   ├── dto/
    │   │   └── Dtos.java                   # All request/response DTOs
    │   ├── model/
    │   │   ├── User.java                   # Teacher & Student entity
    │   │   ├── Session.java                # Attendance session
    │   │   └── AttendanceRecord.java       # Per-student per-session record
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── SessionRepository.java
    │   │   └── AttendanceRecordRepository.java
    │   └── service/
    │       ├── AttendanceService.java      # All business logic
    │       └── DataInitializer.java        # Seeds demo data on startup
    └── resources/
        ├── application.properties
        └── static/
            └── index.html                  # Complete SPA frontend
```

---

## 📡 Extending to Real Bluetooth

To add native Bluetooth scanning (without student interaction):

### Option A: Desktop Agent (Python)
Install on teacher's laptop. Scans BT, POSTs found MACs to the API.
```bash
pip install pybluez requests
python bluetooth_agent.py --session-id 1 --token "jwt..."
```

### Option B: Android App (Kotlin)
Use `BluetoothAdapter.startDiscovery()`, send discovered MACs to the server. Teachers use a tablet in class.

### Option C: Raspberry Pi Scanner
Cheap Pi Zero W mounted in classroom. Auto-scans on session start. Fully automated — no student interaction.

---

## 🗄️ Database
H2 in-memory (resets on restart). For persistence, change `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nuattendance
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```
Add PostgreSQL driver to `pom.xml`.


# ProxiClass:Automated-Attendance-System
**Range-driven automated classroom attendance for FAST-NU**

The **ProxiClass** is a modern web application designed for FAST-NU to streamline and local location coordinates (both x,y) checks. It provides teachers with a real-time live feed of incoming students and automates the record-keeping process (screenshots attached at end) .

---

## ✨ High-Level Features

*   **🔒 Proximity-Based Verification (No Proxies):** Prevents proxy attendance by restricting check-ins based on local network proximity. **Students who are far away or outside the classroom cannot mark their attendance.**
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

<img width="1600" height="734" alt="image" src="https://github.com/user-attachments/assets/e483b9e0-7a89-46af-ba12-e2292d9c844c" />

<img width="1600" height="691" alt="image" src="https://github.com/user-attachments/assets/2b0d43de-a1f1-42cc-9ce8-052650849575" />

<img width="1600" height="733" alt="image" src="https://github.com/user-attachments/assets/67ccc844-792a-46d0-ab30-1a96eeb093b7" />

<img width="1600" height="720" alt="image" src="https://github.com/user-attachments/assets/b394d11e-a129-4708-8f6d-3bc2cf01d1b1" />

<img width="1600" height="729" alt="image" src="https://github.com/user-attachments/assets/89d56b81-09b9-4603-b163-b820aa7f2c02" />

<img width="1600" height="669" alt="image" src="https://github.com/user-attachments/assets/5cfec98e-daee-45b8-a658-825dab3d1264" />

<img width="1001" height="814" alt="image" src="https://github.com/user-attachments/assets/c1ebb9a0-a553-4d91-8786-5f2d677aeffd" />

Students easily marking themselves present on phones within two steps (range based detection):

<img width="250" height="550" alt="image" src="https://github.com/user-attachments/assets/dfaecd7d-ed6b-4394-8965-9ca26b403d2b" />

<img width="250" height="550" alt="image" src="https://github.com/user-attachments/assets/7071f124-5ba7-43d1-882c-075d83b3126c" />


{Future Addition of automate classroom attendance using student device Bluetooth MAC address verification welcomed}









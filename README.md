# NU Attendance System
**Bluetooth-driven automated classroom attendance for FAST-NU**

---

## Quick Start (5 minutes)

### Prerequisites
- Java 17+ (OpenJDK or Oracle) — `java -version`
- Maven 3.6+ — `mvn -version`
- Any modern browser

### Run
```bash
cd attendance-system
mvn spring-boot:run
```
Open: **http://localhost:8080**

---

## Demo Credentials

### Teacher Logins (flex.nu.edu.pk portal)
| Username   | Password     | Name              |
|------------|-------------|-------------------|
| dr.ahmed   | password123  | Dr. Ahmed Khan    |
| ms.fatima  | password123  | Ms. Fatima Malik  |
| prof.ali   | password123  | Prof. Ali Hassan  |

### Student Logins (flexstudent.nu.edu.pk portal)
| Username   | Password | Roll No.  | Section |
|------------|----------|-----------|---------|
| 22f-3001   | pass123  | 22F-3001  | CS-A    |
| 22f-3002   | pass123  | 22F-3002  | CS-A    |
| ... (3001–3015 all work) |

---

## How the System Works

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

## API Reference

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

## Project Structure
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

## Extending to Real Bluetooth

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

## Database
H2 in-memory (resets on restart). For persistence, change `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nuattendance
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
```
Add PostgreSQL driver to `pom.xml`.

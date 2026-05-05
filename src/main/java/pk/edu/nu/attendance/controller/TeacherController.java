package pk.edu.nu.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pk.edu.nu.attendance.dto.Dtos;
import pk.edu.nu.attendance.model.User;
import pk.edu.nu.attendance.repository.UserRepository;
import pk.edu.nu.attendance.service.AttendanceService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserRepository userRepository;

    private User getTeacher(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
    }

    // Start a new session
    @PostMapping("/session/start")
    public ResponseEntity<?> startSession(@RequestBody Dtos.CreateSessionRequest req,
                                          Principal principal) {
        try {
            User teacher = getTeacher(principal);
            Dtos.SessionDto session = attendanceService.createSession(
                teacher, req.getCourseName(), req.getSection(), req.getAttendanceMode()
            );
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get my sessions
    @GetMapping("/sessions")
    public ResponseEntity<?> getMySessions(Principal principal) {
        User teacher = getTeacher(principal);
        return ResponseEntity.ok(attendanceService.getTeacherSessions(teacher));
    }

    // Get active sessions
    @GetMapping("/sessions/active")
    public ResponseEntity<?> getActiveSessions() {
        return ResponseEntity.ok(attendanceService.getActiveSessions());
    }

    // Get students by section
    @GetMapping("/students/{section}")
    public ResponseEntity<?> getStudents(@PathVariable String section) {
        return ResponseEntity.ok(attendanceService.getStudentsBySection(section));
    }

    // Get all students
    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents() {
        return ResponseEntity.ok(attendanceService.getAllStudents());
    }

    // Submit manual attendance
    @PostMapping("/attendance/manual")
    public ResponseEntity<?> submitManual(@RequestBody Dtos.ManualMarkRequest req) {
        try {
            return ResponseEntity.ok(attendanceService.submitManualAttendance(
                req.getSessionId(), req.getPresentStudentIds(), req.getLateStudentIds()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Finalize/submit session
    @PostMapping("/session/{sessionId}/submit")
    public ResponseEntity<?> submitSession(@PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(attendanceService.submitSession(sessionId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get session summary
    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<?> getSessionSummary(@PathVariable Long sessionId) {
        try {
            return ResponseEntity.ok(attendanceService.getAttendanceSummary(sessionId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get course summary
    @GetMapping("/course/{courseName}/section/{section}/summary")
    public ResponseEntity<?> getCourseSummary(@PathVariable String courseName, @PathVariable String section, Principal principal) {
        try {
            User teacher = getTeacher(principal);
            return ResponseEntity.ok(attendanceService.getTeacherCourseSummary(teacher, courseName, section));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Automated device detection from Bluetooth scan
    @PostMapping("/session/{sessionId}/detect")
    public ResponseEntity<?> detectDevice(@PathVariable Long sessionId, @RequestBody Map<String, String> body) {
        try {
            String deviceId = body.get("deviceId");
            return ResponseEntity.ok(attendanceService.markDevicePresent(sessionId, deviceId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

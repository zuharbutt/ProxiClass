package pk.edu.nu.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pk.edu.nu.attendance.dto.Dtos;
import pk.edu.nu.attendance.model.User;
import pk.edu.nu.attendance.repository.UserRepository;
import pk.edu.nu.attendance.service.AttendanceService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UserRepository userRepository;

    private User getStudent(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    // Student marks themselves present via Bluetooth ping
    @PostMapping("/attendance/ping")
    public ResponseEntity<?> bluetoothPing(@RequestBody(required = false) Dtos.PingRequest req,
                                           Principal principal) {
        try {
            User student = getStudent(principal);
            Double lat = req != null ? req.getStudentLat() : null;
            Double lng = req != null ? req.getStudentLng() : null;
            Map<String, Object> result = attendanceService.studentBluetoothPing(student, lat, lng);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get my attendance history
    @GetMapping("/attendance/history")
    public ResponseEntity<?> getHistory(Principal principal) {
        User student = getStudent(principal);
        return ResponseEntity.ok(attendanceService.getStudentAttendanceHistory(student));
    }

    // Get my course summaries
    @GetMapping("/course/summary")
    public ResponseEntity<?> getCourseSummary(Principal principal) {
        User student = getStudent(principal);
        return ResponseEntity.ok(attendanceService.getStudentCourseSummary(student));
    }

    // Get active sessions for my section
    @GetMapping("/sessions/active")
    public ResponseEntity<?> getActiveSessions(Principal principal) {
        User student = getStudent(principal);
        return ResponseEntity.ok(attendanceService.getActiveSessions().stream()
            .filter(s -> s.getSection().equals(student.getSection()))
            .toList());
    }
}

package pk.edu.nu.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pk.edu.nu.attendance.config.JwtUtil;
import pk.edu.nu.attendance.dto.Dtos;
import pk.edu.nu.attendance.model.User;
import pk.edu.nu.attendance.repository.UserRepository;
import pk.edu.nu.attendance.service.AttendanceService;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/teacher/login")
    public ResponseEntity<?> teacherLogin(@RequestBody Dtos.LoginRequest req) {
        Optional<User> userOpt = userRepository.findByUsername(req.getUsername());
        if (userOpt.isEmpty() || userOpt.get().getRole() != User.Role.TEACHER) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid teacher credentials"));
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new Dtos.LoginResponse(
            token, "TEACHER", user.getFullName(), user.getUsername(), null
        ));
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(@RequestBody Dtos.LoginRequest req) {
        Optional<User> userOpt = userRepository.findByUsername(req.getUsername());
        if (userOpt.isEmpty() || userOpt.get().getRole() != User.Role.STUDENT) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid student credentials"));
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return ResponseEntity.ok(new Dtos.LoginResponse(
            token, "STUDENT", user.getFullName(), user.getUsername(), user.getRollNumber()
        ));
    }

    @PostMapping("/student/register-device")
    public ResponseEntity<?> registerDevice(@RequestBody Dtos.RegisterDeviceRequest req,
                                            Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User student = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(attendanceService.registerDevice(student, req.getBluetoothMac()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(Map.of(
            "username", user.getUsername(),
            "fullName", user.getFullName(),
            "role", user.getRole().name(),
            "rollNumber", user.getRollNumber() != null ? user.getRollNumber() : "",
            "section", user.getSection() != null ? user.getSection() : "",
            "bluetoothMac", user.getBluetoothMac() != null ? user.getBluetoothMac() : ""
        ));
    }
}

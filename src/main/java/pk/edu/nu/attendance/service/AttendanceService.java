package pk.edu.nu.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import pk.edu.nu.attendance.dto.Dtos;
import pk.edu.nu.attendance.model.*;
import pk.edu.nu.attendance.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class AttendanceService {


    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Create a new attendance session
    public Dtos.SessionDto createSession(User teacher, String courseName, String section, String mode, Double teacherLat, Double teacherLng) {
        Session session = new Session();
        session.setCourseName(courseName);
        session.setSection(section);
        session.setTeacher(teacher);
        session.setStartTime(LocalDateTime.now());
        session.setStatus(Session.SessionStatus.ACTIVE);
        session.setAttendanceMode(Session.AttendanceMode.valueOf(mode));
        session.setTeacherLat(teacherLat);
        session.setTeacherLng(teacherLng);
        Session saved = sessionRepository.save(session);

        // Pre-populate all students in section as ABSENT
        List<User> students = userRepository.findByRoleAndSection(User.Role.STUDENT, section);
        for (User student : students) {
            AttendanceRecord record = new AttendanceRecord();
            record.setSession(saved);
            record.setStudent(student);
            record.setStatus(AttendanceRecord.AttendanceStatus.ABSENT);
            record.setMarkMethod(AttendanceRecord.MarkMethod.valueOf(mode));
            record.setMarkedAt(LocalDateTime.now());
            attendanceRecordRepository.save(record);
        }

        return toSessionDto(saved);
    }

    // Student is in class — mark present based on login identity + GPS proximity check
    public Map<String, Object> studentBluetoothPing(User student, Double studentLat, Double studentLng) {
        // 1. Find active session for this student's section
        List<Session> activeSessions = sessionRepository.findByStatus(Session.SessionStatus.ACTIVE);
        Session session = activeSessions.stream()
            .filter(s -> s.getSection().equals(student.getSection()))
            .findFirst()
            .orElse(null);

        if (session == null) {
            return Map.of("success", false, "message", "No active session for your section");
        }

        // 2. STEP 1: Student must always share their location — no exceptions
        if (studentLat == null || studentLng == null) {
            return Map.of("success", false,
                "message", "📍 Location access required. Please allow location in your browser to mark attendance.");
        }

        // 3. STEP 2: If teacher stored their location, enforce the 50 m proximity rule
        if (session.getTeacherLat() != null && session.getTeacherLng() != null) {
            double distance = haversineDistance(
                session.getTeacherLat(), session.getTeacherLng(),
                studentLat, studentLng
            );
            if (distance > 50.0) {
                return Map.of("success", false,
                    "message", String.format("❌ Too far — you are %.0f m away. Must be within 50 m of classroom.", distance),
                    "distance", Math.round(distance));
            }
        }
        // If teacher did not share location → proximity check skipped (graceful degradation)

        // 3. Already marked present
        Optional<AttendanceRecord> recordOpt = attendanceRecordRepository.findBySessionAndStudent(session, student);
        if (recordOpt.isPresent() && recordOpt.get().getStatus() == AttendanceRecord.AttendanceStatus.PRESENT) {
            return Map.of("success", true, "message", "You are already marked PRESENT", "courseName", session.getCourseName());
        }

        // 4. Mark present
        AttendanceRecord record = recordOpt.orElseGet(() -> {
            AttendanceRecord r = new AttendanceRecord();
            r.setSession(session);
            r.setStudent(student);
            return r;
        });
        record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
        record.setMarkMethod(AttendanceRecord.MarkMethod.BLUETOOTH);
        record.setMarkedAt(LocalDateTime.now());
        record.setDetectedMac("APP-LOGIN");
        attendanceRecordRepository.save(record);

        // 5. Broadcast to teacher dashboard
        Dtos.BluetoothDetectedEvent event = new Dtos.BluetoothDetectedEvent(
            student.getRollNumber(), student.getFullName(), "App Login",
            LocalDateTime.now().format(FORMATTER)
        );
        messagingTemplate.convertAndSend("/topic/bluetooth/" + session.getId(), event);

        return Map.of("success", true, "message", "You are now marked PRESENT", "courseName", session.getCourseName());
    }

    /** Haversine formula — returns distance in metres between two GPS coordinates */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // Earth radius in metres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // Get active sessions
    public List<Dtos.SessionDto> getActiveSessions() {
        return sessionRepository.findByStatus(Session.SessionStatus.ACTIVE)
                .stream().map(this::toSessionDto).collect(Collectors.toList());
    }

    // Get sessions by teacher
    public List<Dtos.SessionDto> getTeacherSessions(User teacher) {
        return sessionRepository.findByTeacher(teacher)
                .stream().map(this::toSessionDto).collect(Collectors.toList());
    }

    // Get all students in a section
    public List<Dtos.StudentInfoDto> getStudentsBySection(String section) {
        return userRepository.findByRoleAndSection(User.Role.STUDENT, section)
                .stream()
                .sorted(Comparator.comparing(User::getRollNumber))
                .map(this::toStudentDto)
                .collect(Collectors.toList());
    }

    // Get all students
    public List<Dtos.StudentInfoDto> getAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT)
                .stream()
                .sorted(Comparator.comparing(User::getRollNumber))
                .map(this::toStudentDto)
                .collect(Collectors.toList());
    }

    // Manual attendance submission
    public Dtos.AttendanceSummaryDto submitManualAttendance(Long sessionId, List<Long> presentIds, List<Long> lateIds) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<AttendanceRecord> records = attendanceRecordRepository.findBySession(session);
        for (AttendanceRecord record : records) {
            Long sId = record.getStudent().getId();
            if (presentIds != null && presentIds.contains(sId)) {
                record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            } else if (lateIds != null && lateIds.contains(sId)) {
                record.setStatus(AttendanceRecord.AttendanceStatus.LATE);
            } else {
                record.setStatus(AttendanceRecord.AttendanceStatus.ABSENT);
            }
            record.setMarkedAt(LocalDateTime.now());
            record.setMarkMethod(AttendanceRecord.MarkMethod.MANUAL);
            attendanceRecordRepository.save(record);
        }

        return getAttendanceSummary(sessionId);
    }

    // Submit/finalize session
    public Dtos.AttendanceSummaryDto submitSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus(Session.SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
        return getAttendanceSummary(sessionId);
    }


    // Get attendance summary for a session
    public Dtos.AttendanceSummaryDto getAttendanceSummary(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<AttendanceRecord> records = attendanceRecordRepository.findBySession(session);
        List<User> allStudents = userRepository.findByRoleAndSection(User.Role.STUDENT, session.getSection());

        // Build record list, ensuring all students are included
        Map<Long, AttendanceRecord> recordMap = new HashMap<>();
        for (AttendanceRecord r : records) {
            recordMap.put(r.getStudent().getId(), r);
        }

        List<Dtos.StudentMarkEntry> entries = allStudents.stream()
            .sorted(Comparator.comparing(User::getRollNumber))
            .map(s -> {
                AttendanceRecord r = recordMap.get(s.getId());
                String status = r != null ? r.getStatus().name() : "ABSENT";
                return new Dtos.StudentMarkEntry(s.getId(), s.getRollNumber(), s.getFullName(), status);
            })
            .collect(Collectors.toList());

        long presentCount = entries.stream().filter(e -> "PRESENT".equals(e.getStatus())).count();
        long lateCount = entries.stream().filter(e -> "LATE".equals(e.getStatus())).count();

        Dtos.AttendanceSummaryDto summary = new Dtos.AttendanceSummaryDto();
        summary.setSessionId(sessionId);
        summary.setCourseName(session.getCourseName());
        summary.setSection(session.getSection());
        summary.setTotalStudents(allStudents.size());
        summary.setPresentCount((int) presentCount);
        summary.setLateCount((int) lateCount);
        summary.setAbsentCount(allStudents.size() - (int) presentCount - (int) lateCount);
        summary.setRecords(entries);
        return summary;
    }

    // Student's own attendance history
    public List<Map<String, Object>> getStudentAttendanceHistory(User student) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByStudent(student);
        return records.stream()
            .sorted(Comparator.comparing(r -> r.getSession().getStartTime(), Comparator.reverseOrder()))
            .map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("sessionId", r.getSession().getId());
                m.put("courseName", r.getSession().getCourseName());
                m.put("section", r.getSession().getSection());
                m.put("date", r.getSession().getStartTime().format(FORMATTER));
                m.put("status", r.getStatus().name());
                m.put("method", r.getMarkMethod() != null ? r.getMarkMethod().name() : "N/A");
                return m;
            })
            .collect(Collectors.toList());
    }

    // Register/update student's Bluetooth MAC
    public Map<String, Object> registerDevice(User student, String mac) {
        student.setBluetoothMac(mac);
        userRepository.save(student);
        return Map.of("success", true, "message", "Device registered successfully", "mac", mac);
    }

    public List<Dtos.CourseSummaryEntry> getTeacherCourseSummary(User teacher, String courseName, String section) {
        List<Session> sessions = sessionRepository.findByTeacher(teacher).stream()
                .filter(s -> s.getCourseName().equalsIgnoreCase(courseName) && s.getSection().equalsIgnoreCase(section))
                .collect(Collectors.toList());

        List<User> students = userRepository.findByRoleAndSection(User.Role.STUDENT, section);

        List<Dtos.CourseSummaryEntry> summaries = new ArrayList<>();
        for (User student : students) {
            int rawPresents = 0;
            int rawAbsents = 0;
            int rawLates = 0;

            for (Session session : sessions) {
                Optional<AttendanceRecord> recordOpt = attendanceRecordRepository.findBySessionAndStudent(session, student);
                if (recordOpt.isPresent()) {
                    AttendanceRecord.AttendanceStatus status = recordOpt.get().getStatus();
                    if (status == AttendanceRecord.AttendanceStatus.PRESENT) rawPresents++;
                    else if (status == AttendanceRecord.AttendanceStatus.ABSENT) rawAbsents++;
                    else if (status == AttendanceRecord.AttendanceStatus.LATE) rawLates++;
                } else {
                    rawAbsents++; // if missing, counted as absent
                }
            }

            int effectiveLates = rawLates % 2;
            int effectiveAbsents = rawAbsents + (rawLates / 2);
            
            summaries.add(new Dtos.CourseSummaryEntry(
                student.getId(), student.getRollNumber(), student.getFullName(),
                rawPresents, rawAbsents, rawLates,
                rawPresents, effectiveAbsents, effectiveLates,
                sessions.size()
            ));
        }
        return summaries;
    }

    public List<Map<String, Object>> getStudentCourseSummary(User student) {
        List<AttendanceRecord> records = attendanceRecordRepository.findByStudent(student);
        Map<String, List<AttendanceRecord>> byCourse = records.stream()
            .collect(Collectors.groupingBy(r -> r.getSession().getCourseName() + "|" + r.getSession().getTeacher().getFullName()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<AttendanceRecord>> entry : byCourse.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String courseName = parts[0];
            String instructor = parts.length > 1 ? parts[1] : "";

            int rawPresents = 0, rawAbsents = 0, rawLates = 0;
            for (AttendanceRecord r : entry.getValue()) {
                if (r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT) rawPresents++;
                else if (r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT) rawAbsents++;
                else if (r.getStatus() == AttendanceRecord.AttendanceStatus.LATE) rawLates++;
            }

            int effectiveLates = rawLates % 2;
            int effectiveAbsents = rawAbsents + (rawLates / 2);

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("courseName", courseName);
            map.put("instructor", instructor);
            map.put("presents", rawPresents);
            map.put("absents", effectiveAbsents);
            map.put("lates", effectiveLates);
            map.put("total", entry.getValue().size());
            result.add(map);
        }
        return result;
    }

    private Dtos.SessionDto toSessionDto(Session s) {
        long presentCount = attendanceRecordRepository.countBySessionAndStatus(
            s, AttendanceRecord.AttendanceStatus.PRESENT
        );
        List<User> students = userRepository.findByRoleAndSection(User.Role.STUDENT, s.getSection());

        Dtos.SessionDto dto = new Dtos.SessionDto();
        dto.setId(s.getId());
        dto.setCourseName(s.getCourseName());
        dto.setSection(s.getSection());
        dto.setTeacherName(s.getTeacher().getFullName());
        dto.setStatus(s.getStatus().name());
        dto.setAttendanceMode(s.getAttendanceMode().name());
        dto.setStartTime(s.getStartTime().format(FORMATTER));
        dto.setPresentCount((int) presentCount);
        dto.setTotalStudents(students.size());
        return dto;
    }

    private Dtos.StudentInfoDto toStudentDto(User u) {
        return new Dtos.StudentInfoDto(
            u.getId(), u.getFullName(), u.getRollNumber(),
            u.getSection(), u.getDepartment(), u.getBluetoothMac()
        );
    }
}

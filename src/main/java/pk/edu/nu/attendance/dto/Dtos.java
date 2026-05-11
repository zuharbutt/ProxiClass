package pk.edu.nu.attendance.dto;

import pk.edu.nu.attendance.model.User;
import java.util.List;

public class Dtos {

    public static class LoginRequest {
        private String username;
        private String password;
        public LoginRequest() {}
        public LoginRequest(String username, String password) { this.username = username; this.password = password; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginResponse {
        private String token;
        private String role;
        private String fullName;
        private String username;
        private String rollNumber;
        public LoginResponse() {}
        public LoginResponse(String token, String role, String fullName, String username, String rollNumber) {
            this.token = token; this.role = role; this.fullName = fullName; this.username = username; this.rollNumber = rollNumber;
        }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    }

    public static class RegisterDeviceRequest {
        private String bluetoothMac;
        public RegisterDeviceRequest() {}
        public RegisterDeviceRequest(String bluetoothMac) { this.bluetoothMac = bluetoothMac; }
        public String getBluetoothMac() { return bluetoothMac; }
        public void setBluetoothMac(String bluetoothMac) { this.bluetoothMac = bluetoothMac; }
    }

    public static class StudentInfoDto {
        private Long id;
        private String fullName;
        private String rollNumber;
        private String section;
        private String department;
        private String bluetoothMac;
        public StudentInfoDto() {}
        public StudentInfoDto(Long id, String fullName, String rollNumber, String section, String department, String bluetoothMac) {
            this.id = id; this.fullName = fullName; this.rollNumber = rollNumber; this.section = section; this.department = department; this.bluetoothMac = bluetoothMac;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getBluetoothMac() { return bluetoothMac; }
        public void setBluetoothMac(String bluetoothMac) { this.bluetoothMac = bluetoothMac; }
    }

    public static class CreateSessionRequest {
        private String courseName;
        private String section;
        private String attendanceMode;
        private Double teacherLat;
        private Double teacherLng;
        private Double teacherAlt;
        public CreateSessionRequest() {}
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
        public String getAttendanceMode() { return attendanceMode; }
        public void setAttendanceMode(String attendanceMode) { this.attendanceMode = attendanceMode; }
        public Double getTeacherLat() { return teacherLat; }
        public void setTeacherLat(Double teacherLat) { this.teacherLat = teacherLat; }
        public Double getTeacherLng() { return teacherLng; }
        public void setTeacherLng(Double teacherLng) { this.teacherLng = teacherLng; }
        public Double getTeacherAlt() { return teacherAlt; }
        public void setTeacherAlt(Double teacherAlt) { this.teacherAlt = teacherAlt; }
    }

    public static class PingRequest {
        private Double studentLat;
        private Double studentLng;
        private Double studentAlt;
        private Double studentAccuracy;
        public PingRequest() {}
        public Double getStudentLat() { return studentLat; }
        public void setStudentLat(Double studentLat) { this.studentLat = studentLat; }
        public Double getStudentLng() { return studentLng; }
        public void setStudentLng(Double studentLng) { this.studentLng = studentLng; }
        public Double getStudentAlt() { return studentAlt; }
        public void setStudentAlt(Double studentAlt) { this.studentAlt = studentAlt; }
        public Double getStudentAccuracy() { return studentAccuracy; }
        public void setStudentAccuracy(Double studentAccuracy) { this.studentAccuracy = studentAccuracy; }
    }

    public static class SessionDto {
        private Long id;
        private String courseName;
        private String section;
        private String teacherName;
        private String status;
        private String attendanceMode;
        private String startTime;
        private int presentCount;
        private int totalStudents;
        public SessionDto() {}
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAttendanceMode() { return attendanceMode; }
        public void setAttendanceMode(String attendanceMode) { this.attendanceMode = attendanceMode; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public int getPresentCount() { return presentCount; }
        public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class ManualMarkRequest {
        private Long sessionId;
        private List<Long> presentStudentIds;
        private List<Long> lateStudentIds;
        public ManualMarkRequest() {}
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public List<Long> getPresentStudentIds() { return presentStudentIds; }
        public void setPresentStudentIds(List<Long> presentStudentIds) { this.presentStudentIds = presentStudentIds; }
        public List<Long> getLateStudentIds() { return lateStudentIds; }
        public void setLateStudentIds(List<Long> lateStudentIds) { this.lateStudentIds = lateStudentIds; }
    }

    public static class StudentMarkEntry {
        private Long studentId;
        private String rollNumber;
        private String fullName;
        private String status;
        public StudentMarkEntry() {}
        public StudentMarkEntry(Long studentId, String rollNumber, String fullName, String status) {
            this.studentId = studentId; this.rollNumber = rollNumber; this.fullName = fullName; this.status = status;
        }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class BluetoothPingRequest {
        private String sessionId;
        public BluetoothPingRequest() {}
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }

    public static class BluetoothDetectedEvent {
        private String rollNumber;
        private String fullName;
        private String bluetoothMac;
        private String detectedAt;
        public BluetoothDetectedEvent() {}
        public BluetoothDetectedEvent(String rollNumber, String fullName, String bluetoothMac, String detectedAt) {
            this.rollNumber = rollNumber; this.fullName = fullName; this.bluetoothMac = bluetoothMac; this.detectedAt = detectedAt;
        }
        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getBluetoothMac() { return bluetoothMac; }
        public void setBluetoothMac(String bluetoothMac) { this.bluetoothMac = bluetoothMac; }
        public String getDetectedAt() { return detectedAt; }
        public void setDetectedAt(String detectedAt) { this.detectedAt = detectedAt; }
    }

    public static class AttendanceSummaryDto {
        private Long sessionId;
        private String courseName;
        private String section;
        private int totalStudents;
        private int presentCount;
        private int lateCount;
        private int absentCount;
        private List<StudentMarkEntry> records;
        public AttendanceSummaryDto() {}
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getSection() { return section; }
        public void setSection(String section) { this.section = section; }
        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
        public int getPresentCount() { return presentCount; }
        public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
        public int getLateCount() { return lateCount; }
        public void setLateCount(int lateCount) { this.lateCount = lateCount; }
        public int getAbsentCount() { return absentCount; }
        public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }
        public List<StudentMarkEntry> getRecords() { return records; }
        public void setRecords(List<StudentMarkEntry> records) { this.records = records; }
    }

    public static class CourseSummaryEntry {
        private Long studentId;
        private String rollNumber;
        private String fullName;
        private int rawPresents;
        private int rawAbsents;
        private int rawLates;
        private int effectivePresents;
        private int effectiveAbsents;
        private int effectiveLates;
        private int totalClasses;
        public CourseSummaryEntry() {}
        public CourseSummaryEntry(Long studentId, String rollNumber, String fullName, int rawPresents, int rawAbsents, int rawLates, int effectivePresents, int effectiveAbsents, int effectiveLates, int totalClasses) {
            this.studentId = studentId; this.rollNumber = rollNumber; this.fullName = fullName; this.rawPresents = rawPresents; this.rawAbsents = rawAbsents; this.rawLates = rawLates; this.effectivePresents = effectivePresents; this.effectiveAbsents = effectiveAbsents; this.effectiveLates = effectiveLates; this.totalClasses = totalClasses;
        }
        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getRollNumber() { return rollNumber; }
        public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public int getRawPresents() { return rawPresents; }
        public void setRawPresents(int rawPresents) { this.rawPresents = rawPresents; }
        public int getRawAbsents() { return rawAbsents; }
        public void setRawAbsents(int rawAbsents) { this.rawAbsents = rawAbsents; }
        public int getRawLates() { return rawLates; }
        public void setRawLates(int rawLates) { this.rawLates = rawLates; }
        public int getEffectivePresents() { return effectivePresents; }
        public void setEffectivePresents(int effectivePresents) { this.effectivePresents = effectivePresents; }
        public int getEffectiveAbsents() { return effectiveAbsents; }
        public void setEffectiveAbsents(int effectiveAbsents) { this.effectiveAbsents = effectiveAbsents; }
        public int getEffectiveLates() { return effectiveLates; }
        public void setEffectiveLates(int effectiveLates) { this.effectiveLates = effectiveLates; }
        public int getTotalClasses() { return totalClasses; }
        public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    }
}

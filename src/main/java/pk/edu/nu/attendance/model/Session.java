package pk.edu.nu.attendance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "sessions")
public class Session {
    public Session(Long id, String courseName, String section, User teacher, LocalDateTime startTime, LocalDateTime endTime, SessionStatus status, AttendanceMode attendanceMode) {
        this.id = id;
        this.courseName = courseName;
        this.section = section;
        this.teacher = teacher;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.attendanceMode = attendanceMode;
    }

    public Session() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courseName;

    @Column(nullable = false)
    private String section;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Enumerated(EnumType.STRING)
    private AttendanceMode attendanceMode;

    @Column(name = "teacher_lat")
    private Double teacherLat;

    @Column(name = "teacher_lng")
    private Double teacherLng;

    public enum SessionStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

    public enum AttendanceMode {
        AUTO, MANUAL, BLUETOOTH  // BLUETOOTH kept for backward compat with existing DB rows
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public AttendanceMode getAttendanceMode() { return attendanceMode; }
    public void setAttendanceMode(AttendanceMode attendanceMode) { this.attendanceMode = attendanceMode; }
    public Double getTeacherLat() { return teacherLat; }
    public void setTeacherLat(Double teacherLat) { this.teacherLat = teacherLat; }
    public Double getTeacherLng() { return teacherLng; }
    public void setTeacherLng(Double teacherLng) { this.teacherLng = teacherLng; }
}

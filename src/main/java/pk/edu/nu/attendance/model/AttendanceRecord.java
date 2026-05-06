package pk.edu.nu.attendance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecord {
    public AttendanceRecord(Long id, Session session, User student, AttendanceStatus status, LocalDateTime markedAt, MarkMethod markMethod, String detectedMac) {
        this.id = id;
        this.session = session;
        this.student = student;
        this.status = status;
        this.markedAt = markedAt;
        this.markMethod = markMethod;
        this.detectedMac = detectedMac;
    }
    public AttendanceRecord() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private LocalDateTime markedAt;

    @Enumerated(EnumType.STRING)
    private MarkMethod markMethod;

    private String detectedMac; // MAC address detected during BT scan

    public enum AttendanceStatus {
        PRESENT, ABSENT, LATE
    }

    public enum MarkMethod {
        AUTO, BLUETOOTH, MANUAL
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Session getSession() { return session; }
    public void setSession(Session session) { this.session = session; }
    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public LocalDateTime getMarkedAt() { return markedAt; }
    public void setMarkedAt(LocalDateTime markedAt) { this.markedAt = markedAt; }
    public MarkMethod getMarkMethod() { return markMethod; }
    public void setMarkMethod(MarkMethod markMethod) { this.markMethod = markMethod; }
    public String getDetectedMac() { return detectedMac; }
    public void setDetectedMac(String detectedMac) { this.detectedMac = detectedMac; }
}

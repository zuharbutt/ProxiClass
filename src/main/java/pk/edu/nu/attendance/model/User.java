package pk.edu.nu.attendance.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
public class User {
    public User(Long id, String username, String password, Role role, String fullName, String rollNumber, String bluetoothMac, String section, String department) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.rollNumber = rollNumber;
        this.bluetoothMac = bluetoothMac;
        this.section = section;
        this.department = department;
    }
    public User() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String fullName;

    // For students
    private String rollNumber;
    private String bluetoothMac; // Device MAC address for BT detection
    private String section;
    private String department;
    private String fingerprint; // Unique browser fingerprint

    public enum Role {
        TEACHER, STUDENT
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getBluetoothMac() { return bluetoothMac; }
    public void setBluetoothMac(String bluetoothMac) { this.bluetoothMac = bluetoothMac; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
}

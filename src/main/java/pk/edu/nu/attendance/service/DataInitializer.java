package pk.edu.nu.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pk.edu.nu.attendance.model.User;
import pk.edu.nu.attendance.repository.AttendanceRecordRepository;
import pk.edu.nu.attendance.repository.SessionRepository;
import pk.edu.nu.attendance.repository.UserRepository;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Clear existing data to start from zero as requested
        attendanceRecordRepository.deleteAll();
        sessionRepository.deleteAll();
        
        // Delete only students to preserve teachers
        List<User> existingStudents = userRepository.findByRole(User.Role.STUDENT);
        userRepository.deleteAll(existingStudents);

        // 2. Create Teachers
        createTeacher("dr.ahmed", "password123", "Dr. Ahmed Khan", "CS");
        createTeacher("ms.fatima", "password123", "Ms. Fatima Malik", "CS");
        createTeacher("prof.ali", "password123", "Prof. Ali Hassan", "EE");

        // 3. Import Students from students_data folder
        File dataDir = new File("students_data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            File[] files = dataDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String section = file.getName(); // e.g. BSE-4A
                        importStudentsFromFile(file, section);
                    }
                }
            }
        }

        System.out.println("=== NU Attendance System Started ===");
        System.out.println("Teachers: dr.ahmed / ms.fatima / prof.ali (password: password123)");
        System.out.println("Imported students from students_data/ (password: pass123)");
        System.out.println("=====================================");
    }

    private void importStudentsFromFile(File file, String section) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            // Skip header (S#	Roll No.	Student Name	A	 L)
            int count = 0;
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split("\t");
                if (parts.length >= 3) {
                    String rollNo = parts[1].trim();
                    String name = parts[2].trim();
                    
                    // Use roll number as username and 'pass123' as default password
                    createStudent(rollNo.toLowerCase(), "pass123", name, rollNo, section, "SE", "");
                    count++;
                }
            }
            System.out.println("Imported " + count + " students for section " + section);
        } catch (Exception e) {
            System.err.println("Error importing from " + file.getName() + ": " + e.getMessage());
        }
    }

    private void createTeacher(String username, String password, String fullName, String dept) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole(User.Role.TEACHER);
            u.setFullName(fullName);
            u.setDepartment(dept);
            userRepository.save(u);
        }
    }

    private void createStudent(String username, String password, String fullName,
                                String rollNumber, String section, String dept, String mac) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(password));
            u.setRole(User.Role.STUDENT);
            u.setFullName(fullName);
            u.setRollNumber(rollNumber);
            u.setSection(section);
            u.setDepartment(dept);
            u.setBluetoothMac(mac);
            userRepository.save(u);
        }
    }
}

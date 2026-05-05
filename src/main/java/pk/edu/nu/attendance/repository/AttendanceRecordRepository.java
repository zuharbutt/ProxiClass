package pk.edu.nu.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.edu.nu.attendance.model.AttendanceRecord;
import pk.edu.nu.attendance.model.Session;
import pk.edu.nu.attendance.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findBySession(Session session);
    List<AttendanceRecord> findByStudent(User student);
    Optional<AttendanceRecord> findBySessionAndStudent(Session session, User student);
    List<AttendanceRecord> findBySessionAndStatus(Session session, AttendanceRecord.AttendanceStatus status);
    long countBySessionAndStatus(Session session, AttendanceRecord.AttendanceStatus status);
}

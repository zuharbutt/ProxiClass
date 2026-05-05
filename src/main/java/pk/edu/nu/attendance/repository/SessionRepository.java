package pk.edu.nu.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.edu.nu.attendance.model.Session;
import pk.edu.nu.attendance.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByTeacher(User teacher);
    List<Session> findByStatus(Session.SessionStatus status);
    Optional<Session> findFirstByStatusOrderByStartTimeDesc(Session.SessionStatus status);
    List<Session> findBySectionAndStatus(String section, Session.SessionStatus status);
}

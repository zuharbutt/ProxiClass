package pk.edu.nu.attendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pk.edu.nu.attendance.model.User;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndSection(User.Role role, String section);
    Optional<User> findByBluetoothMac(String bluetoothMac);
    Optional<User> findByRollNumber(String rollNumber);
    Optional<User> findByFingerprint(String fingerprint);
}

package pk.edu.nu.attendance;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashTest {
    @Test
    public void generateHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("===HASH-START===");
        System.out.println("HASH_PASSWORD123:" + encoder.encode("password123"));
        System.out.println("HASH_PASS123:" + encoder.encode("pass123"));
        System.out.println("===HASH-END===");
    }
}

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("password123: " + encoder.encode("password123"));
        System.out.println("pass123: " + encoder.encode("pass123"));
    }
}

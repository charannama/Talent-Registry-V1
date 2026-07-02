import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String pass = "Password@123";
        String hash1 = "$2a$12$IVko1m3HWh1VGEV4uJ/gt.dYt2iDzGzai0NLerdp0PfrFxZvrrMA2";
        String hash2 = "$2a$12$Pj4u2KlO3qukUIkz5s/JmuChrzvhJr44SG6P1MWjL06puWjdIKlsy";
        System.out.println("Hash1 Matches: " + encoder.matches(pass, hash1));
        System.out.println("Hash2 Matches: " + encoder.matches(pass, hash2));
    }
}

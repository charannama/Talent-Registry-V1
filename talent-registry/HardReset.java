import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class HardReset {
    public static void main(String[] args) {
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hash = encoder.encode("Secure@123");
            
            String url = "jdbc:postgresql://localhost:5432/talent_registry?stringtype=unspecified";
            String user = "postgres";
            String pass = "postgres";
            
            Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE users SET password_hash = ?, failed_login_attempts = 0, lockout_until = NULL, status = 'ACTIVE' WHERE email = 'emily.carter81@example.test'"
            );
            updateStmt.setString(1, hash);
            
            int rows = updateStmt.executeUpdate();
            System.out.println("Updated " + rows + " row(s) for emily to Secure@123. Hash: " + hash);
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

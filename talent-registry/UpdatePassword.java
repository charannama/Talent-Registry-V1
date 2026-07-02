import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdatePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("Password@123");
        
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "UPDATE users SET password_hash = '" + hash + "' WHERE email IN ('hr-admin@example.com', 'corp@myenterprise.com')";
            int rows = stmt.executeUpdate(sql);
            System.out.println("UPDATED_ROWS: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

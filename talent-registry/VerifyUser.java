import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class VerifyUser {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate("UPDATE users SET email_verified = true, email_verified_at = CURRENT_TIMESTAMP, status = 'ACTIVE' WHERE email = 'alexander.reed91@example.test'");
            System.out.println("UPDATED_ROWS: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

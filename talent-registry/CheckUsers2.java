import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckUsers2 {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "SELECT email, account_status, password_hash, lockout_until, is_deleted FROM users WHERE email IN ('hr-admin@example.com', 'corp@myenterprise.com')";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Status: " + rs.getString("account_status"));
                System.out.println("Hash: " + rs.getString("password_hash"));
                System.out.println("Lockout: " + rs.getString("lockout_until"));
                System.out.println("Deleted: " + rs.getBoolean("is_deleted"));
                System.out.println("---");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

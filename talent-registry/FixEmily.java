import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FixEmily {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry?stringtype=unspecified";
        String user = "postgres";
        String pass = "postgres";
        
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            
            // Check status first
            PreparedStatement getStmt = conn.prepareStatement("SELECT status, failed_login_attempts, lockout_until FROM users WHERE email = 'emily.carter81@example.test'");
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                System.out.println("BEFORE FIX:");
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("Failed Attempts: " + rs.getInt("failed_login_attempts"));
                System.out.println("Locked Until: " + rs.getTimestamp("lockout_until"));
            }

            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE users SET password_hash = (SELECT password_hash FROM users WHERE email = 'hr.manager@company.test'), " +
                "failed_login_attempts = 0, lockout_until = NULL, status = 'ACTIVE' " +
                "WHERE email = 'emily.carter81@example.test'"
            );
            
            int rows = updateStmt.executeUpdate();
            System.out.println("Updated " + rows + " row(s) for emily.");
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckProfile {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            // Get user ID first
            ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE email = 'alexander.reed91@example.test'");
            if (rs.next()) {
                String userId = rs.getString("id");
                System.out.println("User ID: " + userId);
                
                // Check student profile
                ResultSet profileRs = stmt.executeQuery("SELECT * FROM student_profiles WHERE user_id = '" + userId + "'");
                if (profileRs.next()) {
                    System.out.println("Profile found: " + profileRs.getString("id"));
                } else {
                    System.out.println("NO_PROFILE_FOUND");
                }
            } else {
                System.out.println("USER_NOT_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

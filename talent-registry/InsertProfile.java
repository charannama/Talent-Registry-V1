import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

public class InsertProfile {
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
                
                String profileId = UUID.randomUUID().toString();
                String sql = "INSERT INTO student_profiles (id, user_id, sync_status, eligibility_level, created_at, updated_at, version, is_deleted) " +
                             "VALUES ('" + profileId + "', '" + userId + "', 'NEVER_SYNCED', 'NO_PROJECT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false)";
                
                int rows = stmt.executeUpdate(sql);
                System.out.println("PROFILE_INSERTED: " + rows);
            } else {
                System.out.println("USER_NOT_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

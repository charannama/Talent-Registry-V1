import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestInsertCorrect2 {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            // Get user ID
            ResultSet rs = stmt.executeQuery("SELECT id FROM users WHERE email = 'alexander.reed91@example.test'");
            if (rs.next()) {
                String userId = rs.getString("id");
                
                // Get profile ID
                ResultSet profileRs = stmt.executeQuery("SELECT id FROM student_profiles WHERE user_id = '" + userId + "'");
                if (profileRs.next()) {
                    String profileId = profileRs.getString("id");
                    
                    String sql = "INSERT INTO applications (id, profile_id, opening_id, status, created_at, updated_at, version, is_deleted, applied_at) " +
                                 "VALUES (gen_random_uuid(), '" + profileId + "', '8cffaa67-3970-41d1-be25-d4f82c052996', 'APPLIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false, CURRENT_TIMESTAMP)";
                    int rows = stmt.executeUpdate(sql);
                    System.out.println("TEST_INSERT_SUCCESS: " + rows);
                } else {
                    System.out.println("NO_PROFILE");
                }
            } else {
                System.out.println("NO_USER");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestInsert {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "INSERT INTO applications (id, profile_id, opening_id, status, created_at, updated_at, version, is_deleted, applied_at) " +
                         "VALUES (gen_random_uuid(), 'profileId', '8cffaa67-3970-41d1-be25-d4f82c052996', 'APPLIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false, CURRENT_TIMESTAMP)";
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

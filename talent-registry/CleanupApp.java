import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CleanupApp {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("DELETE FROM applications WHERE opening_id = '8cffaa67-3970-41d1-be25-d4f82c052996'");
            System.out.println("CLEANUP_SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

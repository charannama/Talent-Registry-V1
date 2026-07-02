import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixSchema {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("ALTER TABLE applications ALTER COLUMN student_id DROP NOT NULL;");
            System.out.println("SCHEMA_FIXED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

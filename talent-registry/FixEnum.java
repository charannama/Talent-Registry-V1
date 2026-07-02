import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class FixEnum {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'APPLIED';");
            stmt.execute("ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'FORWARDED';");
            stmt.execute("ALTER TYPE application_status ADD VALUE IF NOT EXISTS 'SELECTED';");
            
            System.out.println("ENUM_FIXED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

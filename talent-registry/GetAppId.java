import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetAppId {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "SELECT id FROM applications ORDER BY created_at DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println("APP_ID: " + rs.getString("id"));
            } else {
                System.out.println("NO_APPS_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

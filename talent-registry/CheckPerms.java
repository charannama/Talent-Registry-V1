import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckPerms {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "SELECT p.code, r.name FROM users u " +
                         "JOIN user_roles ur ON u.id = ur.user_id " +
                         "JOIN roles r ON ur.role_id = r.id " +
                         "JOIN role_permissions rp ON r.id = rp.role_id " +
                         "JOIN permissions p ON rp.permission_id = p.id " +
                         "WHERE u.email = 'hr-admin@example.com' OR u.email = 'corp@myenterprise.com'";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("name") + " -> " + rs.getString("code"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

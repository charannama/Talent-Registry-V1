import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class FindHrUsers {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            String sql = "SELECT u.email, r.name FROM users u " +
                         "JOIN user_roles ur ON u.id = ur.user_id " +
                         "JOIN roles r ON ur.role_id = r.id " +
                         "WHERE r.name IN ('HR_STAFF', 'HR_MANAGER', 'ENTERPRISE_RECRUITER', 'ENTERPRISE_ADMIN')";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("email") + " - " + rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

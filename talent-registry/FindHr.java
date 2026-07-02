import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class FindHr {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            // Need to join user_roles to find an HR user
            String sql = "SELECT u.email FROM users u " +
                         "JOIN user_roles ur ON u.id = ur.user_id " +
                         "JOIN roles r ON ur.role_id = r.id " +
                         "WHERE r.name = 'HR_STAFF' OR r.name = 'HR_MANAGER' OR r.name = 'ENTERPRISE_RECRUITER' " +
                         "LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                System.out.println(rs.getString("email"));
            } else {
                System.out.println("NO_HR_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

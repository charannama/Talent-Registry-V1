import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class GrantPerms2 {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            // Insert APPLICATION_UPDATE permission if it doesn't exist
            stmt.executeUpdate("INSERT INTO permissions (id, code, name, description, created_at, updated_at, version, is_deleted) " +
                               "SELECT gen_random_uuid(), 'APPLICATION_UPDATE', 'Application Update', 'Can update applications', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false " +
                               "WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'APPLICATION_UPDATE')");
                               
            // Grant to HR_STAFF
            stmt.executeUpdate("INSERT INTO role_permissions (role_id, permission_id) " +
                               "SELECT r.id, p.id FROM roles r, permissions p " +
                               "WHERE r.name = 'HR_STAFF' AND p.code = 'APPLICATION_UPDATE' " +
                               "AND NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = r.id AND permission_id = p.id)");
                               
            // Grant to ENTERPRISE_RECRUITER
            stmt.executeUpdate("INSERT INTO role_permissions (role_id, permission_id) " +
                               "SELECT r.id, p.id FROM roles r, permissions p " +
                               "WHERE r.name = 'ENTERPRISE_RECRUITER' AND p.code = 'APPLICATION_UPDATE' " +
                               "AND NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = r.id AND permission_id = p.id)");
                               
            System.out.println("PERMISSIONS_GRANTED");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

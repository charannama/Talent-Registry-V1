import java.sql.*;

public class JdbcQuery4 {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement()) {
             
            System.out.println("--- USERS ---");
            ResultSet rs = stmt.executeQuery("SELECT id, email FROM users WHERE email LIKE '%enterprise%'");
            while (rs.next()) {
                System.out.println("User ID: " + rs.getString("id") + " | Email: " + rs.getString("email"));
            }
            rs.close();
            
            System.out.println("--- ENTERPRISE ACCOUNTS ---");
            rs = stmt.executeQuery("SELECT id, user_id, company_name FROM enterprise_accounts");
            while (rs.next()) {
                System.out.println("Enterprise ID: " + rs.getString("id") + " | User ID: " + rs.getString("user_id") + " | Name: " + rs.getString("company_name"));
            }
            rs.close();
            
            System.out.println("--- ENTERPRISE TEAM MEMBERS ---");
            rs = stmt.executeQuery("SELECT id, enterprise_id, user_id FROM enterprise_team_members");
            while (rs.next()) {
                System.out.println("Team Member ID: " + rs.getString("id") + " | Enterprise ID: " + rs.getString("enterprise_id") + " | User ID: " + rs.getString("user_id"));
            }
            rs.close();
        }
    }
}

import java.sql.*;

public class JdbcQuery6 {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement()) {
             
            System.out.println("--- USERS ---");
            ResultSet rs = stmt.executeQuery("SELECT email, password_hash FROM users WHERE email = 'olivia.bennett@example.com'");
            if (rs.next()) {
                System.out.println("Hash for olivia.bennett: " + rs.getString("password_hash"));
            }
            rs.close();
            
            rs = stmt.executeQuery("SELECT email, password_hash FROM users WHERE email = 'enterprise.admin@company.test'");
            if (rs.next()) {
                System.out.println("Hash for enterprise.admin: " + rs.getString("password_hash"));
            }
            rs.close();
        }
    }
}

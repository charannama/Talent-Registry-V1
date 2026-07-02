import java.sql.*;

public class JdbcQuery5 {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement()) {
             
            System.out.println("--- USERS ---");
            ResultSet rs = stmt.executeQuery("SELECT id, email FROM users WHERE email LIKE '%olivia%' OR id = '9b4482e8-d640-4ce0-ac6e-e37803cfa148'");
            while (rs.next()) {
                System.out.println("User ID: " + rs.getString("id") + " | Email: " + rs.getString("email"));
            }
            rs.close();
        }
    }
}

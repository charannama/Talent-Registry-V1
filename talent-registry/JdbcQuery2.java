import java.sql.*;

public class JdbcQuery2 {
    public static void main(String[] args) throws Exception {
        System.out.println("--- ROLES ---");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, role_type FROM roles")) {
            while (rs.next()) {
                System.out.println(rs.getString("name") + " : " + rs.getString("role_type"));
            }
        }
    }
}

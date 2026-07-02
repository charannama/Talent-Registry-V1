import java.sql.*;

public class JdbcQuery8 {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement()) {
             
            ResultSet rs = stmt.executeQuery("SELECT id FROM express_interests ORDER BY created_at DESC LIMIT 1");
            if (rs.next()) {
                System.out.println("LATEST INTEREST ID: " + rs.getString("id"));
            } else {
                System.out.println("NO INTERESTS FOUND!");
            }
            rs.close();
        }
    }
}

import java.sql.*;

public class JdbcQuery7 {
    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE email = 'olivia.bennett@example.com'")) {
             
            // Set password to Secure@123
            stmt.setString(1, "$2a$12$5woIg37RYnHDQR.F.NbxmO2iPI9jMmnroeAZlFXguT1Hh2w.GJJnm");
            int updated = stmt.executeUpdate();
            System.out.println("Updated rows: " + updated);
        }
    }
}

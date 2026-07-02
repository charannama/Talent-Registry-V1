import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetToken {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT token FROM email_verification_tokens evt JOIN users u ON evt.user_id = u.id WHERE u.email = 'alexander.reed91@example.test' ORDER BY evt.expires_at DESC LIMIT 1")) {
            if (rs.next()) {
                System.out.println("TOKEN_FOUND: " + rs.getString("token"));
            } else {
                System.out.println("TOKEN_NOT_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

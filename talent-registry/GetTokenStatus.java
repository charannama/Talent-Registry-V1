import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetTokenStatus {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT evt.token, evt.expires_at, evt.is_used, u.email_verified_at FROM email_verification_tokens evt JOIN users u ON evt.user_id = u.id WHERE u.email = 'alexander.reed91@example.test' ORDER BY evt.expires_at DESC LIMIT 1")) {
            if (rs.next()) {
                System.out.println("TOKEN: " + rs.getString("token"));
                System.out.println("EXPIRES_AT: " + rs.getTimestamp("expires_at"));
                System.out.println("IS_USED: " + rs.getBoolean("is_used"));
                System.out.println("EMAIL_VERIFIED_AT: " + rs.getTimestamp("email_verified_at"));
            } else {
                System.out.println("TOKEN_NOT_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

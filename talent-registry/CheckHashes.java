import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckHashes {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry?stringtype=unspecified";
        String user = "postgres";
        String pass = "postgres";
        
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement getStmt = conn.prepareStatement("SELECT email, password_hash, status FROM users WHERE email IN ('emily.carter81@example.test', 'hr.manager@company.test')");
            ResultSet rs = getStmt.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString("email") + " | " + rs.getString("password_hash") + " | " + rs.getString("status"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

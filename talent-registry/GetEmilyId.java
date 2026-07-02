import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GetEmilyId {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry?stringtype=unspecified";
        String user = "postgres";
        String pass = "postgres";
        
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement getStmt = conn.prepareStatement("SELECT id FROM users WHERE email = 'emily.carter81@example.test'");
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                System.out.println("EMILY_ID=" + rs.getString("id"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class FindEmilyId {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:file:c:/Users/layar/Downloads/talent-registry/talent-registry/talent-registry;AUTO_SERVER=TRUE", "sa", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, email FROM users WHERE email = 'emily.carter81@example.test'");
            while (rs.next()) {
                System.out.println("USER ID: " + rs.getString("id") + " - " + rs.getString("email"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

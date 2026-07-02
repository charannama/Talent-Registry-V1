import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckAppConstraints {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT column_name, is_nullable FROM information_schema.columns WHERE table_name = 'applications'");
            while (rs.next()) {
                System.out.println(rs.getString("column_name") + ": " + rs.getString("is_nullable"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

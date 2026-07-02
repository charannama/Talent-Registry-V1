import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckOpenings {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, status FROM job_openings")) {
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.println("ID: " + rs.getString("id") + ", Title: " + rs.getString("title") + ", Status: " + rs.getString("status"));
            }
            if (!hasRows) {
                System.out.println("NO_OPENINGS_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

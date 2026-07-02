import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateOpening {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate("UPDATE job_openings SET featured = true, graduation_years = '2024, 2025' WHERE id = '8cffaa67-3970-41d1-be25-d4f82c052996'");
            System.out.println("UPDATED_ROWS: " + rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

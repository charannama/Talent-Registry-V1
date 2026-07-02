import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.ResultSetMetaData;

public class CheckOpeningDetails {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String password = "postgres";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT o.*, e.onboarding_status, e.account_active, e.is_deleted as ent_deleted FROM job_openings o JOIN enterprise_accounts e ON o.enterprise_id = e.id WHERE o.id = '8cffaa67-3970-41d1-be25-d4f82c052996'")) {
            if (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    System.out.println(meta.getColumnName(i) + ": " + rs.getString(i));
                }
            } else {
                System.out.println("OPENING_NOT_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

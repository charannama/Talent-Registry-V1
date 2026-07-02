import java.sql.*;

public class JdbcQuery3 {
    public static void main(String[] args) throws Exception {
        System.out.println("--- STUDENT PROFILES ---");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, user_id FROM student_profiles LIMIT 5")) {
            while (rs.next()) {
                System.out.println("Student Profile ID: " + rs.getString("id") + " | User ID: " + rs.getString("user_id"));
            }
        }
        
        System.out.println("--- OPENINGS ---");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, title, enterprise_id FROM openings LIMIT 5")) {
            while (rs.next()) {
                System.out.println("Opening ID: " + rs.getString("id") + " | Title: " + rs.getString("title") + " | Enterprise ID: " + rs.getString("enterprise_id"));
            }
        }
        
        System.out.println("--- ENTERPRISE ACCOUNTS ---");
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/talent_registry", "postgres", "postgres");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, company_name FROM enterprise_accounts LIMIT 5")) {
            while (rs.next()) {
                System.out.println("Enterprise ID: " + rs.getString("id") + " | Name: " + rs.getString("company_name"));
            }
        }
    }
}

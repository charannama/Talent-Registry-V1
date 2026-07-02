package com.zencube.registry;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbQueryTest {
    @Test
    public void runQuery() throws Exception {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String pass = "postgres";
        String query = "SELECT u.id AS user_id, u.email, u.first_name, u.last_name, u.status AS account_status, u.last_login_at, s.headline, s.bio, s.skills, s.years_of_experience, s.availability, sp.institution, sp.discipline, sp.graduation_year, sp.gpa, sp.eligibility_level, sp.sync_status FROM users u INNER JOIN students s ON u.id = s.user_id LEFT JOIN student_profiles sp ON u.id = sp.user_id;";

        System.out.println("================== QUERY RESULTS ==================");
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("Student #" + count);
                System.out.println("User ID: " + rs.getString("user_id"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                System.out.println("Account Status: " + rs.getString("account_status"));
                System.out.println("Headline: " + rs.getString("headline"));
                System.out.println("Institution: " + rs.getString("institution"));
                System.out.println("GPA: " + rs.getString("gpa"));
                System.out.println("---------------------------------------------------");
            }
            if (count == 0) {
                System.out.println("No students found in the database.");
            }
        }
        System.out.println("===================================================");
    }
}

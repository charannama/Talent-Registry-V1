package com.zencube.registry;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbUpdateTest {
    @Test
    public void runUpdate() throws Exception {
        String url = "jdbc:postgresql://localhost:5432/talent_registry";
        String user = "postgres";
        String pass = "postgres";
        String query = "UPDATE users SET email_verified = true, status = 'ACTIVE';";

        System.out.println("================== UPDATING USERS ==================");
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement stmt = conn.createStatement()) {
            int count = stmt.executeUpdate(query);
            System.out.println("Updated " + count + " users to be email_verified = true");
        }
        System.out.println("====================================================");
    }
}

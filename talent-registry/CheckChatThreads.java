import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckChatThreads {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry?stringtype=unspecified";
        String user = "postgres";
        String pass = "postgres";
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement stmt = conn.prepareStatement("SELECT column_name, is_nullable FROM information_schema.columns WHERE table_name = 'chat_threads'");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                System.out.println(rs.getString("column_name") + " | NULLABLE=" + rs.getString("is_nullable"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

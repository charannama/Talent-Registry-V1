import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/talent_registry?stringtype=unspecified";
        String user = "postgres";
        String pass = "postgres";
        
        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM chat_threads");
            ResultSet rs = stmt.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            System.out.println("COLUMNS:");
            for(int i=1; i<=rsmd.getColumnCount(); i++) {
                System.out.print(rsmd.getColumnName(i) + " | ");
            }
            System.out.println();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

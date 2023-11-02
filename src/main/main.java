import java.sql.Connection;
import java.sql.SQLException;

import com.jcraft.jsch.Session;


public class main {
    public static void main(String[] args) {
        // MovieSearch movieSearch = new MovieSearch();
        Connection conn = null;
        Session session = null;
        try {
            Accounts account = new Accounts();
            conn = account.getConnection();
            session = account.gSession();
            Accounts.printBeginMenu();
        

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("Closing Database Connection");
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
    }
}
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class MovieSearch {

    public void movieSearch() throws SQLException {
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport =5432;
        String user;
        String password;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("pdm-movies-16/src/main/credentials.txt"))) {
            user = bufferedReader.readLine();
            password = bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String databaseName = "p320_16";

        String driverName = "org.postgresql.Driver";

        Connection conn = null;
        Session session = null;

        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.connect();
           System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
           System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;

           System.out.println("database Url: " + url);
            java.util.Properties props = new java.util.Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
           System.out.println("Database connection established");

            PreparedStatement statement = conn.prepareStatement("select * from movie where \"movieID\" = 19995");

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
               System.out.println(resultSet.getString("title"));
                System.out.printf("id:%d runtime:%d title:%s rating=%s%n", resultSet.getLong("movieID"),
                        resultSet.getLong("length"), resultSet.getString("title"),
                        resultSet.getString("MPAA_rating"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }

    }

}

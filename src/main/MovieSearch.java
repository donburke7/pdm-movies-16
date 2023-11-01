import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class MovieSearch {

    public void movieSearch() throws SQLException {
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport =5432;
        String user;
        String password;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("dataSources/credentials.txt"))) {
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
//            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
//            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;

//            System.out.println("database Url: " + url);
            java.util.Properties props = new java.util.Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
//            System.out.println("Database connection established");

            Scanner scanner = new Scanner(System.in);
            System.out.println("What would you like to search by?");
            System.out.println("1: Movie Name");
            System.out.println("2: Movie Release Date");
            System.out.println("3: Cast Member Name");
            System.out.println("4: Studio Name");
            System.out.println("5: Genre");
            int selection = scanner.nextInt();

            switch (selection) {
                case 1:
                    System.out.println("What is the name of the movie you want to search?");
                    String movieName = scanner.next();
                    System.out.println("Searching for "+ movieName);
                    searchByName(conn, movieName);
                    break;
                default:
                    System.out.println("Invalid input");
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

    private void searchByName(Connection connection, String movieName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                "select * from movie where upper(\"title\") like upper(?)");
        preparedStatement.setString(1, "%" + movieName + "%");

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            System.out.printf("id:%d runtime:%d title:%s rating=%s studioID=%s%n", resultSet.getLong("movieID"),
                        resultSet.getLong("length"), resultSet.getString("title"),
                        resultSet.getString("MPAA_rating"), resultSet.getString("studioID"));
        }
    }

}

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
                    searchByName(conn, scanner);
                    break;
                case 2:
                    searchByReleaseDate(conn, scanner);
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

    private void searchByName(Connection connection, Scanner scanner) throws SQLException {

        System.out.println("What is the name of the movie you want to search?");
        String movieName = scanner.next();
        System.out.println("What is your uid");
        int uid = scanner.nextInt();
        System.out.println("Searching for "+ movieName + " for " + uid);

        PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT m.title,\n" +
                        "       m.length,\n" +
                        "       m.\"MPAA_rating\",\n" +
                        "       CONCAT(c.\"fName\", ' ', c.\"lName\") AS \"Director\",\n" +
                        "       r.rating\n" +
                        "FROM movie m\n" +
                        "JOIN directs d ON m.\"movieID\" = d.\"movieID\"\n" +
                        "JOIN contributors c ON d.\"contributorID\" = c.\"contributorID\"\n" +
                        "JOIN rates r ON r.\"movieid\" = m.\"movieID\"\n" +
                        "WHERE m.title ILIKE ?\n" +
                        "AND r.userid = ?");
        preparedStatement.setString(1, "%" + movieName + "%");
        preparedStatement.setInt(2, uid);

//        System.out.println(preparedStatement);


        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            System.out.printf("runtime:%d title:%s MPAArating=%s director=%s userRating=%d%n", resultSet.getLong("length"),
                    resultSet.getString("title"), resultSet.getString("MPAA_rating"),
                    resultSet.getString("director"), resultSet.getLong("rating"));
        }
    }

    private void searchByReleaseDate(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Do you want to search by month, year, or exact date?");
        System.out.println("1: Month\n2: Year\n3: Exact Date");
        int searchOption = scanner.nextInt();
        PreparedStatement preparedStatement = null;
        int queryIntVar;
        switch (searchOption) {
            case 1:
                System.out.println("Enter the month as a number (i.e. for October enter 10)");
                queryIntVar = scanner.nextInt();
                preparedStatement = connection.prepareStatement(
                        "select * from movie where \"movieID\" in (select \"movieID\" from releases where " +
                                "extract(month from \"releaseDate\") = ?)"
                );
                preparedStatement.setInt(1, queryIntVar);
                break;
            case 2:
                System.out.println("Enter the year");
                queryIntVar = scanner.nextInt();
                preparedStatement = connection.prepareStatement(
                        "select * from movie where \"movieID\" in (select \"movieID\" from releases where " +
                                "extract(year from \"releaseDate\") = ?)"
                );
                preparedStatement.setInt(1, queryIntVar);
                break;
            default:
                System.out.println("Not a valid option");
                return;
        }

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            System.out.printf("id:%d runtime:%d title:%s rating=%s studioID=%s%n", resultSet.getLong("movieID"),
                    resultSet.getLong("length"), resultSet.getString("title"),
                    resultSet.getString("MPAA_rating"), resultSet.getString("studioID"));
        }

    }

}

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class MovieSearch {

    private Connection connection;
    private int userID;

    public void movieSearch() throws SQLException {
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
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
                case 3:
                    searchByCastMember(conn, scanner);
                    break;
                case 4:
                    searchByStudioName(conn, scanner);
                    break;
                case 5:
                    break;
                default:
                    System.out.println("Invalid input");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null && !conn.isClosed()) {
//                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
//                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }

    }

    private void printResult(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            Array actors = resultSet.getArray("actors");
            String[] actorStrings = (String[])actors.getArray();
            ArrayList<String> arrayList = new ArrayList<>(List.of(actorStrings));
            System.out.printf("runtime:%d title:%s MPAArating=%s director=%s actors:%s avgrating:%d%n", resultSet.getLong("length"),
                    resultSet.getString("title"), resultSet.getString("MPAA_rating"),
                    resultSet.getArray("director").toString(), arrayList.subList(0,Math.min(5, arrayList.size() - 1)),
                    resultSet.getLong("rating"));
        }
    }

    private void searchByName(Connection connection, Scanner scanner) throws SQLException {

        System.out.println("What is the name of the movie you want to search?");
        String movieName = scanner.next();
        System.out.println("Searching for "+ movieName);

        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                        SELECT  m.title,
                                m.length,
                                m."MPAA_rating",
                                array_agg(distinct concat(dc."fName", ' ', dc."lName")) as director,
                                array_agg(distinct concat(ac."fName", ' ', ac."lName")) as actors,
                                round(avg(ur.rating), 2) as rating
                        FROM movie m
                        JOIN releases r ON m."movieID" = r."movieID"
                        JOIN directs d ON m."movieID" = d."movieID"
                        JOIN acts_in a on m."movieID" = a."movieID"
                        join contributors ac on a."contributorID" = ac."contributorID"
                        JOIN contributors dc ON d."contributorID" = dc."contributorID"
                        join rates ur on m."movieID" = ur.movieid
                        WHERE m.title ILIKE ? group by m."movieID" order by array_agg(distinct m."title"), array_agg(distinct r."releaseDate")
                        """);
        preparedStatement.setString(1, "%" + movieName + "%");

        ResultSet resultSet = preparedStatement.executeQuery();

        printResult(resultSet);
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
                        """
                                SELECT  m.title,
                                        m.length,
                                        m."MPAA_rating",
                                        array_agg(distinct concat(dc."fName", ' ', dc."lName")) as director,
                                        array_agg(distinct concat(ac."fName", ' ', ac."lName")) as actors,
                                        round(avg(ur.rating), 2) as rating
                                FROM movie m
                                JOIN releases r ON m."movieID" = r."movieID"
                                JOIN directs d ON m."movieID" = d."movieID"
                                JOIN acts_in a on m."movieID" = a."movieID"
                                join contributors ac on a."contributorID" = ac."contributorID"
                                JOIN contributors dc ON d."contributorID" = dc."contributorID"
                                join rates ur on m."movieID" = ur.movieid
                                WHERE extract(month from "releaseDate") = ? group by m."movieID" order by array_agg(distinct m."title"), array_agg(distinct r."releaseDate")
                                """
                );
                preparedStatement.setInt(1, queryIntVar);

                break;
            case 2:
                System.out.println("Enter the year");
                queryIntVar = scanner.nextInt();
                preparedStatement = connection.prepareStatement(
                        """
                                SELECT  m.title,
                                        m.length,
                                        m."MPAA_rating",
                                        array_agg(distinct concat(dc."fName", ' ', dc."lName")) as director,
                                        array_agg(distinct concat(ac."fName", ' ', ac."lName")) as actors,
                                        round(avg(ur.rating), 2) as rating
                                FROM movie m
                                JOIN releases r ON m."movieID" = r."movieID"
                                JOIN directs d ON m."movieID" = d."movieID"
                                JOIN acts_in a on m."movieID" = a."movieID"
                                join contributors ac on a."contributorID" = ac."contributorID"
                                JOIN contributors dc ON d."contributorID" = dc."contributorID"
                                join rates ur on m."movieID" = ur.movieid
                                WHERE extract(year from "releaseDate") = ? group by m."movieID" order by array_agg(distinct m."title"), array_agg(distinct r."releaseDate")
                                """
                );
                preparedStatement.setInt(1, queryIntVar);
                break;
            case 3:
                System.out.println("Enter the year");
                int year = scanner.nextInt();
                System.out.println("Enter the month");
                int month = scanner.nextInt();
                System.out.println("Enter the day");
                int day = scanner.nextInt();

                preparedStatement = connection.prepareStatement(
                        """
                                SELECT  m.title,
                                        m.length,
                                        m."MPAA_rating",
                                        array_agg(distinct concat(dc."fName", ' ', dc."lName")) as director,
                                        array_agg(distinct concat(ac."fName", ' ', ac."lName")) as actors,
                                        round(avg(ur.rating), 2) as rating
                                FROM movie m
                                JOIN releases r ON m."movieID" = r."movieID"
                                JOIN directs d ON m."movieID" = d."movieID"
                                JOIN acts_in a on m."movieID" = a."movieID"
                                join contributors ac on a."contributorID" = ac."contributorID"
                                JOIN contributors dc ON d."contributorID" = dc."contributorID"
                                join rates ur on m."movieID" = ur.movieid
                                WHERE "releaseDate" = ? group by m."movieID" order by array_agg(distinct m."title"), array_agg(distinct r."releaseDate")
                                """
                );
                preparedStatement.setDate(1, Date.valueOf(year + "-" + month + "-" + day));
                break;
            default:
                System.out.println("Not a valid option");
                return;
        }

        ResultSet resultSet = preparedStatement.executeQuery();

        printResult(resultSet);

        System.out.println(preparedStatement);

    }

    private void searchByCastMember(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter cast member first name(enter if unknown):");
        String castMemberFirstName = scanner.next();
        System.out.println("Enter cast member last name(enter if unknown):");
        String castMemberLastName = scanner.next();
        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                    SELECT  m.title,
                            m.length,
                            m."MPAA_rating",
                            array_agg(distinct concat(dc."fName", ' ', dc."lName")) as director,
                            array_agg(distinct concat(ac."fName", ' ', ac."lName")) as actors,
                            round(avg(ur.rating), 2) as rating
                    FROM movie m
                    JOIN releases r ON m."movieID" = r."movieID"
                    JOIN directs d ON m."movieID" = d."movieID"
                    JOIN acts_in a on m."movieID" = a."movieID"
                    join contributors ac on a."contributorID" = ac."contributorID"
                    JOIN contributors dc ON d."contributorID" = dc."contributorID"
                    join rates ur on m."movieID" = ur.movieid
                    WHERE concat(ac."fName", ac."lName") ILIKE ?
                    group by m."movieID"\s
                    order by array_agg(distinct m."title"), array_agg(distinct r."releaseDate")
                    """
        );

        preparedStatement.setString(1, "%" + castMemberFirstName + "%" + castMemberLastName + "%");
        ResultSet resultSet = preparedStatement.executeQuery();
        printResult(resultSet);
    }

    private void searchByStudioName(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter studio name:");
        String studioName = scanner.next();

        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                    SELECT  m.title,
                            m.length,
                            m."MPAA_rating",
                            array_agg(distinct concat(dc."fName", ' ', dc."lName")) as director,
                            array_agg(distinct concat(ac."fName", ' ', ac."lName")) as actors,
                            round(avg(ur.rating), 2) as rating
                    FROM movie m
                    JOIN releases r ON m."movieID" = r."movieID"
                    JOIN directs d ON m."movieID" = d."movieID"
                    JOIN acts_in a on m."movieID" = a."movieID"
                    JOIN contributors ac ON a."contributorID" = ac."contributorID"
                    JOIN contributors dc ON d."contributorID" = dc."contributorID"
                    JOIN rates ur ON m."movieID" = ur.movieid
                    JOIN studio s ON m.studioid = s."studioID"
                    WHERE s.name ILIKE ?
                    group by m."movieID"
                    order by array_agg(distinct m."title"), array_agg(distinct r."releaseDate")
                    """
        );

        preparedStatement.setString(1, "%" + studioName + "%");
        ResultSet resultSet = preparedStatement.executeQuery();
        printResult(resultSet);
    }

    private void searchByGenre(Connection connection, Scanner scanner) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select type from genre").executeQuery();
        System.out.println("Select genre to search by using its number");

        HashMap<Integer, String> genreHashMap = new HashMap<>();
        int counter = 0;
        while (resultSet.next()) {
            genreHashMap.put(counter, resultSet.getString("type"));
            System.out.printf("%d: %s%n", counter, genreHashMap.get(counter));
            counter++;
        }

        int genreNumber = scanner.nextInt();
        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                    SELECT  m.title,
                            m.length,
                            m."MPAA_rating",
                            array_agg(distinct concat(dc."fName", ' ', dc."lName")) as director,
                            array_agg(distinct concat(ac."fName", ' ', ac."lName")) as actors,
                            round(avg(ur.rating), 2) as rating
                    FROM movie m
                    JOIN releases r ON m."movieID" = r."movieID"
                    JOIN directs d ON m."movieID" = d."movieID"
                    JOIN acts_in a on m."movieID" = a."movieID"
                    JOIN contributors ac ON a."contributorID" = ac."contributorID"
                    JOIN contributors dc ON d."contributorID" = dc."contributorID"
                    JOIN rates ur ON m."movieID" = ur.movieid
                    JOIN classified_by c ON m."movieID" = c.movieid
                    JOIN genre g ON c.genreid = g.genreid
                    WHERE g.type ILIKE ?
                    group by m."movieID"
                    order by array_agg(distinct m."title"), array_agg(distinct r."releaseDate")
                    """
        );

        preparedStatement.setString(1, genreHashMap.get(genreNumber));
        ResultSet resultSet1 = preparedStatement.executeQuery();
        printResult(resultSet1);
    }

}

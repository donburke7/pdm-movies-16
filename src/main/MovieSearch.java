
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class MovieSearch {

    private static final int MAX_ACTOR_LIST_SIZE = 5;
    private final Connection connection;

    public MovieSearch(Connection connection) {
        this.connection = connection;
    }

    public void movieSearch() throws SQLException {
        int selection = 0;

        while (selection != 9) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("What would you like to search by?");
            System.out.println("1: Movie Name");
            System.out.println("2: Movie Release Date");
            System.out.println("3: Cast Member Name");
            System.out.println("4: Studio Name");
            System.out.println("5: Genre");
            System.out.println("9: To go back to main menu");
            selection = scanner.nextInt();

            switch (selection) {
                case 1:
                    searchByName(connection, scanner);
                    break;
                case 2:
                    searchByReleaseDate(connection, scanner);
                    break;
                case 3:
                    searchByCastMember(connection, scanner);
                    break;
                case 4:
                    searchByStudioName(connection, scanner);
                    break;
                case 5:
                    searchByGenre(connection, scanner);
                    break;
                case 9:
                    System.out.println("Going back to main menu now");
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    private void printResult(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            Array actors = resultSet.getArray("actors");
            String[] actorStrings = (String[])actors.getArray();
            ArrayList<String> actorArrayList = new ArrayList<>(List.of(actorStrings));
            String[] directorString = (String[])resultSet.getArray("director").getArray();
            ArrayList<String> directorArrayList = new ArrayList<>(List.of(directorString));
            if (directorArrayList.isEmpty()) {
                directorArrayList.add("Not Found");
            }
            System.out.printf("Movie Name: \"%s\" | Runtime: %d | Director: %s | MPAA Rating: %s | " +
                    "Average User Rating :%d | Actor List: %s%n", resultSet.getString("title"),
                    resultSet.getInt("length"), directorArrayList.get(0),
                    resultSet.getString("MPAA_rating"), resultSet.getLong("rating"),
                    actorArrayList.subList(0, Math.min(MAX_ACTOR_LIST_SIZE, actorArrayList.size())));
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
        PreparedStatement preparedStatement;
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

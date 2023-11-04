import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;

public class WatchMovie {
    Timestamp timestamp;
    Connection conn;
    Scanner scanner;
    int userID;


    public WatchMovie(Connection connection, int id) {
        scanner = new Scanner(System.in);
        this.conn = connection;
        this.userID = id;
    }


    public void movieWatch() throws SQLException {
        String movieName = "";
        System.out.println("What movie would you like to watch?: ");

        // whole lotta nonsense just to see if the movie exists in the db
        while (movieName.equals("")) {
            movieName = scanner.nextLine();
            if (!movieName.isEmpty()) {
                PreparedStatement selectMovie = 
                    conn.prepareStatement("select m.\"movieID\" from movie m where m.title = ?");
                selectMovie.setString(1, movieName);
                ResultSet movieResult = selectMovie.executeQuery();
                if (!movieResult.next()) {
                    System.out.println("Unable to find movie: \"" + movieName + "\"");
                    System.out.println("What movie would you like to watch?: ");
                    movieName = "";
                }
            }
        }
        // movie exists, so we insert a watch entry (we watch it)
        PreparedStatement statement = conn.prepareStatement("""
                    insert into watches ("userID", "movieID", "dateTimeWatched")
                    values (?, (select m."movieID" from movie m
                    where m.title = ?), ?)
                    """);
        statement.setInt(1, userID);
        statement.setString(2, movieName);
        statement.setTimestamp(3, timestamp);
        statement.executeUpdate();
        System.out.println("You watched: \"" + movieName + "\" at " + timestamp + "!\n");
    }


    public void collectionWatch() throws SQLException {
        ArrayList<Integer> movieIds = new ArrayList<>();
        String collectionName = "";
        // display the collections available to watch
        System.out.println("Here are your collections:");
        PreparedStatement selectAllCollections = 
            conn.prepareStatement("""
                select "collectionName" from collection where "userID" = ? order by "collectionName"
                    """);
        selectAllCollections.setInt(1, userID);
        ResultSet allCollections = selectAllCollections.executeQuery();
        while (allCollections.next()) {
            System.out.println("- " + allCollections.getString("collectionName"));
        }

        System.out.println("What collection of movies would you like to watch?: ");
        // whole lotta nonsense just to see if the collection exists in the db
        while (collectionName.equals("")) {
            collectionName = scanner.nextLine();
            if (!collectionName.isEmpty()) {
                PreparedStatement selectCollection = 
                conn.prepareStatement("""
                    select con."movieID"
                    from contains con
                    join collection c on con."collectionID" = c."collectionID"
                    where c."userID" = ? and c."collectionName" = ?
                        """);
                selectCollection.setInt(1, userID);
                selectCollection.setString(2, collectionName);
                ResultSet collectionResult = selectCollection.executeQuery();
                while (collectionResult.next()) {
                    movieIds.add(collectionResult.getInt("movieID"));
                }
                if (movieIds.isEmpty()) {
                    System.out.println("Unable to find collection: \"" + collectionName + "\"");
                    System.out.println("What collection of movies would you like to watch?: ");
                    collectionName = "";
                }
            }
        }

        for (int movieId : movieIds) {
            // insert a watch entry for each movie in collection (we watch them)
            PreparedStatement statement = conn.prepareStatement("""
                        insert into watches ("userID", "movieID", "dateTimeWatched")
                        values (?, ?, ?)
                        """);
            statement.setInt(1, userID);
            statement.setInt(2, movieId);
            statement.setTimestamp(3, timestamp);
            statement.executeUpdate();
        }
        System.out.println("You just binge-watched the entirety of: \"" + 
                                collectionName + "\" at " + timestamp + "!\n");
    }


    public void watchOptions() throws SQLException {
            boolean isValidInput = true;
            while (isValidInput) {
                System.out.println("Would you like to watch a movie or a collection of movies?");
                System.out.println("Select 3 to go back to the main menu");
                System.out.println("1: Movie");
                System.out.println("2: Collection");
                int choice = scanner.nextInt();

                String date = LocalDate.now().toString();
                String dateTime = date + " " + LocalTime.now();
                this.timestamp = Timestamp.valueOf(dateTime);
                
                if (choice == 1) {
                    isValidInput = true;
                    movieWatch();
                }
                else if (choice == 2) {
                    isValidInput = true;
                    collectionWatch();
                } else if (choice == 3) {
                    isValidInput = false;
                } else {
                    System.out.println("Invalid Input\n");
                }
            }

//            scanner.close();
    }
}


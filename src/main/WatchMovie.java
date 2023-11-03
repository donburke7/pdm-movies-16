import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter; 

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
        System.out.println("What collection of movies would you like to watch?: ");
        // whole lotta nonsense just to see if the movie exists in the db
        while (collectionName.equals("")) {
            collectionName = scanner.nextLine();
            if (!collectionName.isEmpty()) {
                // FIGURE OUT IF IT SHOULD PULL OTHER PEOPLES COLLECTIONS OR NOT
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

        }


    }

    public void watchOptions() throws SQLException {
            boolean validInput = false;
            while (!validInput) {
                System.out.println("Would you like to watch a movie or a collection of movies?");
                System.out.println("1: Movie");
                System.out.println("2: Collection");
                int choice = scanner.nextInt();

                String date = LocalDate.now().toString();
                String dateTime = date + " " + LocalTime.now().toString();
                this.timestamp = Timestamp.valueOf(dateTime);
                
                if (choice == 1) {
                    validInput = true;
                    movieWatch();
                }
                else if (choice == 2) {
                    validInput = true;
                    //PreparedStatement statement = conn.prepareStatement();
                    collectionWatch();
                }
                else { 
                    System.out.println("Invalid Input\n");
                }
            }
            scanner.close();

            /**  watch entire collection
             * insert ...
             * 
                select con."movieID"
                from contains con
                join collection c on con."collectionID" = c."collectionID"
                where c."userID" = 43 and c."collectionName" = 'Pennie''s Collection'
             * 
             */
    }
}


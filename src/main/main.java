import java.sql.SQLException;

public class main {
    public static void main(String[] args) {
        MovieSearch movieSearch = new MovieSearch();
        try {
            movieSearch.movieSearch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
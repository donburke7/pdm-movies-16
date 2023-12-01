import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Recomendations {

    private final Connection connection;
    private final int userid;
    private final Scanner scanner;

    public Recomendations(Connection connection, int userid) {
        this.connection = connection;
        this.userid = userid;
        scanner = new Scanner(System.in);
    }

    public void runRecommendations() throws SQLException {
        int selection = 0;

        while (selection != 9) {
            System.out.println("\nSelect the number of the option to view recommendations");
            System.out.println("1: The top 20 most popular movies in the last 90 days (rolling)");
            System.out.println("2: The top 20 most popular movies among your followers");
            System.out.println("3: The top 5 new releases of this month");
            System.out.println("4: For you");
            System.out.println("9: to go back to main menu");
            selection = scanner.nextInt();

            switch (selection) {
                case 1:
                    last90Days();
                    break;
                case 2:
                    amongFollowers();
                    break;
                case 3:
                    ofThisMonth();
                    break;
                case 4:
                    forYou();
                    break;
                case 9:
                    System.out.println("Going back to main menu now");
                    break;
                default:
                    System.out.println("Invalid input");
            }
        }
    }

    private static void printResult(ResultSet resultSet) throws SQLException {
        System.out.println("\nRecommended:");
        while (resultSet.next()) {
            System.out.println(resultSet.getString("title"));
        }
        System.out.println();
    }


    private void last90Days() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                    select m.title from movie as m join watches as w on m."movieID" = w."movieID"
                    where "dateTimeWatched" >= now() - INTERVAL '90 DAY'
                    group by m."movieID"
                    order by count(m."movieID") desc
                    limit 20;
                    """
        );
        ResultSet resultSet = preparedStatement.executeQuery();

        printResult(resultSet);
    }

    private void amongFollowers() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                    select m.title from movie as m
                    join watches as w on m."movieID" = w."movieID"
                    join follows as f on "userID" = "Following"
                    where "Follower" = ?
                    group by m.title
                    order by count(m.title) desc
                    limit 20;
                    """
        );
        preparedStatement.setInt(1, userid);

        ResultSet resultSet = preparedStatement.executeQuery();

        printResult(resultSet);
    }

    private void ofThisMonth() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                select m.title, avg(r.rating) from movie as m
                join watches as w on m."movieID" = w."movieID"
                join rates as r on m."movieID" = r.movieid
                where extract(month from "dateTimeWatched") = extract(month from now())
                group by m.title
                order by avg(r.rating) desc, count(m.title) desc
                limit 5;
                """
        );

        ResultSet resultSet = preparedStatement.executeQuery();

        printResult(resultSet);
    }

    private void forYou() throws SQLException {
        // the first query selects a users top five movies, then looks at who watched those, and selects those
        // users top movies
        // the second query selects a users
        PreparedStatement preparedStatement = connection.prepareStatement(
                """
                        (select m."title" from watches as w
                            join movie as m on w."movieID" = m."movieID"
                            where w."userID" in (select distinct w."userID" from watches as w
                                                    where w."movieID" in (select w."movieID" from watches as w
                                                                            where w."userID" = ?
                                                                            group by w."movieID"
                                                                            order by count(w."movieID") DESC
                                                                            limit 5))
                            group by m.title
                            order by count(m.title)
                            limit 5)
                        union
                        (select m.title from movie as m
                            join acts_in as a on m."movieID" = a."movieID"
                            join releases as r on m."movieID" = r."movieID"
                            where a."contributorID" in (select a."contributorID" from acts_in as a
                                                            where a."movieID" in (select w."movieID" from watches as w
                                                                                                where "userID" = ?)
                                                            group by a."contributorID"
                                                            order by count(a."contributorID") desc
                                                            limit 5)
                            group by m.title, r."releaseDate"
                            order by r."releaseDate" desc
                            limit 5)
                        union
                        (select m.title from movie as m
                            join classified_by as c on m."movieID" = c.movieid
                            join releases as r on m."movieID" = r."movieID"
                            where c.genreid in (select c.genreid from classified_by as c
                                                    where c.movieid in (select w."movieID" from watches as w
                                                                                        where "userID" = ?)
                                                    group by c.genreid
                                                    order by count(c.genreid)
                                                    limit 3)
                            group by m.title, r."releaseDate"
                            order by r."releaseDate" desc
                            limit 5)
                    """
        );

        preparedStatement.setInt(1, userid);
        preparedStatement.setInt(2, userid);
        preparedStatement.setInt(3, userid);

        ResultSet resultSet = preparedStatement.executeQuery();

        printResult(resultSet);
    }

}

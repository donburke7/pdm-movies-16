import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Ratings {

    private Connection c;
    private int userid;

    public Ratings(Connection c, int userID) {
        this.c = c;
        this.userid = userID;
    }

    public void printMenu() throws SQLException {
        System.out.println("\n0: Display Rating Menu");
        System.out.println("1: Search by Movie Name");
        System.out.println("2: Rate Movie by Title");
        System.out.print("9: Exit Rate Menu");
        input();
    }

    public void input() throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number that corresponds to the command you wish to execute: ");
        int selection = scanner.nextInt();
        switch (selection) {
            case 0:
                printMenu();
                break;
            case 1:
                searchByName(scanner);
                break;
            case 2:
                rate(scanner);
                break;
            default:
                break;
        }
    }

    public void searchByName(Scanner sc) throws SQLException{
        System.out.print("What is the name of the movie you want to search? ");
        String movieName = sc.next();
        System.out.println("Searches for: " + movieName);

        PreparedStatement ps = c.prepareStatement(
                "SELECT \"title\" FROM movie WHERE \"title\" ILIKE ?"
        );

        ps.setString(1, "%" + movieName + "%");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            System.out.printf("%s\n", rs.getString("title"));
        }
        input();
    }

    public void rate(Scanner sc) throws SQLException {
        System.out.print("What is the exact name of the movie you want to rate? ");
        String movieName = sc.next();
        System.out.print("What rating do you give this movie? (Scale 1-10) ");
        int rating = sc.nextInt();
        System.out.println("Rating movie...");

        PreparedStatement ps = c.prepareStatement(
                "SELECT \"movieID\" FROM movie WHERE \"title\" = ?"
        );

        ps.setString(1, movieName);
        ResultSet rs = ps.executeQuery();

//        while(rs.next()){
//            System.out.printf("%d\n", rs.getInt("movieID"));
//        }
//        System.out.println("here lol");
//        int movieid = rs.getInt("movieID");
        boolean valid_movie = rs.next();

        if(!valid_movie){
            System.out.println("Invalid Movie Title");
            printMenu();
        }
        else {
//            System.out.println("here2");
//            int movieID = rs.getInt("movieID");
//            System.out.println(movieID);
            PreparedStatement insert = c.prepareStatement(
                    "INSERT INTO rates(userid, movieid, rating) VALUES (?, " +
                            "(Select \"movieID\" from movie where \"title\" = ?), ?)" +
                            " ON CONFLICT (userid, movieid) DO UPDATE SET rating = excluded.rating"
            );

            insert.setInt(1, userid);
            insert.setString(2, movieName);
            insert.setInt(3, rating);

//            System.out.println(insert);
            insert.execute();
            System.out.println("You gave a rating of " + rating + " to " + movieName);
            input();
        }
//        System.out.println("now here lolol");
    }
}
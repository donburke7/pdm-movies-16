import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Ratings {

    public void rating() throws SQLException {
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
            String url = "jdbc:postgresql://127.0.0.1:" + assigned_port + "/" + databaseName;

//            System.out.println("database Url: " + url);
            java.util.Properties props = new java.util.Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
//            System.out.println("Database connection established");
            System.out.print("Welcome to the Rating Menu");
            printMenu(conn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if( conn != null && !conn.isClosed()){
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if(session != null && session.isConnected()){
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
    }

    static void printMenu(Connection conn) throws SQLException {
        System.out.println("\n0: Display Rating Menu");
        System.out.println("1: Search by Movie Name");
        System.out.print("2: Rate Movie by Title");
        input(conn);
    }

    static void input(Connection conn) throws SQLException{
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number that corresponds to the command you wish to execute: ");
        int selection = scanner.nextInt();
        switch (selection) {
            case 0:
                printMenu(conn);
                break;
            case 1:
                searchByName(conn, scanner);
                break;
            case 2:
                rate(conn, scanner);
                break;
            default:
                System.out.println("Invalid input");
                printMenu(conn);
        }
    }

    static void searchByName(Connection conn, Scanner sc) throws SQLException{
        System.out.print("What is the name of the movie you want to search? ");
        String movieName = sc.next();
        System.out.println("Searches for: " + movieName);

        PreparedStatement ps = conn.prepareStatement(
                "SELECT \"title\" FROM movie WHERE \"title\" ILIKE ?"
        );

        ps.setString(1, "%" + movieName + "%");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            System.out.printf("%s\n", rs.getString("title"));
        }
        input(conn);
    }

    static void rate(Connection conn, Scanner sc) throws SQLException {
        System.out.print("What is the exact name of the movie you want to rate? ");
        String movieName = sc.next();
        System.out.print("What rating do you give this movie? (Scale 1-10) ");
        int rating = sc.nextInt();
        System.out.println("Rating movie...");

        PreparedStatement ps = conn.prepareStatement(
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
            printMenu(conn);
        }
        else {
//            System.out.println("here2");
//            int movieID = rs.getInt("movieID");
//            System.out.println(movieID);
            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO rates(userid, movieid, rating) VALUES (?, " +
                            "(Select \"movieID\" from movie where \"title\" = ?), ?)" +
                            " ON CONFLICT (userid, movieid) DO UPDATE SET rating = excluded.rating"
            );

            insert.setInt(1, 1);
            insert.setString(2, movieName);
            insert.setInt(3, rating);

//            System.out.println(insert);
            insert.execute();
            System.out.println("You gave a rating of " + rating + " to " + movieName);
            input(conn);
        }
//        System.out.println("now here lolol");
    }
}
import java.util.Scanner;
import java.io.*;
import java.sql.*;
import com.jcraft.jsch.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.text.Utilities;

/// create account has to check if a user exists
/// if not, create account , if does ask to login
/// when creating acc. check if username exists/not
/// store last accessed into table
/// store creation date into table
/// everytime someone signs in update last accessed

public class accounts {
    public static Scanner sc = new Scanner(System.in);
    public static int userID = -1;
    public static int incrementUserID = 1001;
    static Statement stmt;


    public static boolean isLogin(){
        if (userID!= -1){
            return true;
        }
        else{
            return false;
        }
    }

    public static void Login(){
      
        System.out.println("Enter username:");
        String username = sc.nextLine();
        System.out.println("Enter password:");
        String password = sc.nextLine();
        int tempID;
        String getIDStatment = "select \"userID\" from \"users\" where (username = '" + username + "') and (password = '" + password +"')";
        try{
            ResultSet rset = stmt.executeQuery(getIDStatment);
            while(rset.next()){
                tempID = rset.getInt("userID");
                userID = tempID;
                System.out.println(userID);
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            // UPDATE "Users" SET lastAccess = currentTime
            // where userID = userID
        }
        catch(SQLException e){
            System.out.println(e.getErrorCode());
        }
    }
        // else{
        //     System.out.println("Could not login - try again or create an account");
        // }
    

    public static void createAccount(){
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        System.out.println("Please enter first name:");
        String firstName = sc.nextLine();
        System.out.println("Please enter last name:");
        String lastName = sc.nextLine();
        System.out.println("Please enter new username:");
        String username = sc.nextLine();
        System.out.println("Please enter new password:");
        String password = sc.nextLine();
        String insertNewUser = "insert into users values ("+ incrementUserID+",'"+ username +"', '"+ password +"', '"+ firstName +"', '"+ lastName +"', '"+currentTime+"',"+ null + ")";
        System.out.println("SQL IS "+ insertNewUser);
        try {
            stmt.executeUpdate(insertNewUser);
            System.out.println("Your account has been created, please sign in to see other functionality");
            incrementUserID++;
        } catch (SQLException e) {
            // prompt to reenter new username
            e.printStackTrace();
        }

        

    }
    public static void main(String[] args) throws SQLException {

        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user;
        String password;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("src/credentials.txt"))) {
            user = bufferedReader.readLine();
            password = bufferedReader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // String user = cs_user; //change to your username
        // String password = cs_password; //change to your password

        String databaseName = "p320_16"; //change to your database name

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
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            stmt = conn.createStatement();
            // createAccount();
            Login();
            System.out.println(isLogin());

            // Do something with the database....

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
}
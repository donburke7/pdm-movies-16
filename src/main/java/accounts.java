package main.java;
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
    public static boolean loginChecker;

    public static void printBeginMenu(){
        System.out.println("Welcome to the Movies Database!");
        System.out.println("Please sign in or create account with commands: \n");
        
        System.out.println("0: Login to your account");
        System.out.println("1: Create account");
        System.out.println("9: Quit Application");
        System.out.println("Please enter corresponding number to command you wish to perform:");

        int input = Integer.parseInt(sc.nextLine());
        while(input != 0 && input != 1 && input != 9){
            System.out.println("Please enter valid number of command you wish to perform: \n");
            System.out.println("0: Login to your account");
            System.out.println("1: Create account");
            System.out.println("9: Quit Application");
            input = Integer.parseInt(sc.nextLine());
        }
        if(input == 1){
            createAccount();
        }
        else if(input == 0){
            Login();
        }
        else if(input == 9){
            System.out.println("Cloing Appplication now :)");
        }
        
    }

    public static void printMainMenu(){
        System.out.println("You are now logged in! Find below more functionality:\n");

        System.out.println("2: Access and Edit Collections"); 
        System.out.println("3: Search for Movies");
        System.out.println("4: Rate Movies");
        System.out.println("5: Watch Movies");
        System.out.println("6: Follow other Users");
        System.out.println("7: Logout");


    }


    public static boolean isLogin(){
        if (userID!= -1){
            loginChecker = true;
        }
        else{
            loginChecker = false;
        }
        return loginChecker;
    }

    public static void Login(){
        System.out.println("Welcome to login. Please enter credentials below: \n");
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
                
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            String updateLastAccess = "update users set \"lastAccess\" = '" + currentTime +"' where \"userID\" = " + userID;
            stmt.executeUpdate(updateLastAccess); 
            isLogin();
            printMainMenu();

        }
        catch(SQLException e){
            System.out.println("Could not login - try again or create an account");
            printBeginMenu();
        }
    }
    public static void Logout(){
        userID = -1;
        loginChecker = false;
    }

    public static void createAccount(){
        System.out.println("Welcome to Account Creation. Please enter credentials below: \n");
        System.out.println("Please enter first name:");
        String firstName = sc.nextLine();
        System.out.println("Please enter last name:");
        String lastName = sc.nextLine();
        System.out.println("Please enter new username:");
        String username = sc.nextLine();
        System.out.println("Please enter new password:");
        String password = sc.nextLine();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String insertNewUser = "insert into users values ("+ incrementUserID+",'"+ username +"', '"+ password +"', '"+ firstName +"', '"+ lastName +"', '"+currentTime+"',"+ null + ")";
        System.out.println("SQL IS "+ insertNewUser);
        try {
            stmt.executeUpdate(insertNewUser);
            incrementUserID++;
            System.out.println("Your account has been created, please sign in to see other functionality");
            printBeginMenu();
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
            // Login();
            // System.out.println(isLogin());
            System.out.println(incrementUserID);
            printBeginMenu();
            

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
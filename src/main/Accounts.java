
import java.util.Scanner;
import java.io.*;
import java.sql.*;
import com.jcraft.jsch.*;

import java.util.Properties;


/// This class handles all account issues as well as passing off functionality to other 
/// classes
public class Accounts {

    ///The scanner used for reading user input
    public static Scanner sc = new Scanner(System.in);

    // the static userID of the current user logged in
    public static int userID = -1; 

    // the counter to increment generated userIDs when creating accounts
    public static int incrementUserID;

    // used for sql statements
    static Statement stmt;

    static Connection conn;
    static Session session;
    // checks if a user is logged in or not
    public static boolean loginChecker;

    public Accounts() throws Exception{

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

        String databaseName = "p320_16"; //change to your database name

        String driverName = "org.postgresql.Driver";
        
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


    }
    
    public Session gSession(){
        return session;
    }
    /**
     * This gets the current logged in user's userID 
     * @return the int userID
     */
    public int getUserID(){
        return userID;
    }
    /**
     * Gets connection for everyone else to use
     * @return the connection created
     */
    public Connection getConnection(){
        return conn;
    }

    public static int incrementCounterUserID()throws SQLException{
        int counterUserID = 0; 
            PreparedStatement statement = conn.prepareStatement("select MAX(\"userID\") from \"users\"");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                counterUserID= resultSet.getInt(1);
            }
        counterUserID = counterUserID+1;
        return counterUserID;
    }
    /**
     * This prints the beginning menu when a user launches console app.
     */
    public static void printBeginMenu() throws SQLException{
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
    /**
     * This prints the main menu after a user has logged in and directs them to the right path.
     */
    public static void printMainMenu() throws SQLException{
        System.out.println("Find below more functionality:\n");

        System.out.println("2: Access and Edit Collections"); 
        System.out.println("3: Search for Movies");
        System.out.println("4: Rate Movies");
        System.out.println("5: Watch Movies");
        System.out.println("6: Follow other Users");
        System.out.println("7: Logout");
        int input = 0 ;
        while(input != 2 && input != 3 && input != 4 && input != 5 && input != 6 && input != 7 ){
            input = Integer.parseInt(sc.nextLine());
            switch (input) {
                case 2:
                    // collection work
                    collections MyCollection = new collections(userID, conn);
                    int command = MyCollection.printMenu(); 
                     if (command == 0){
                        command=MyCollection.printMenu();
                    }else if (command == 1){
                            MyCollection.createCollection(conn,userID);
                    }else if (command == 2){
                         MyCollection.viewCollections(conn,userID);
                    }else if (command == 3){
                         MyCollection.deleteCollection(conn,userID);
                    }else if (command == 4){
                         MyCollection.addMovie(conn, userID);
                    }else if (command == 5){
                         MyCollection.deleteMovie(conn,userID);
                    }else if (command ==6 ){
                         MyCollection.modifyCollection(conn,userID);
                    }
                    break;
                case 3: 
                    //search work
                    MovieSearch movieSearch = new MovieSearch(conn);
                    movieSearch.movieSearch();
                    break;
                case 4:
                    // rate work
                    break;

                case 5:
                    // watch work
                    break;
                case 6: 
                    // followers work
                    break;

                case 7:
                    Logout();
                    break;
                default: 
                    System.out.println("Please enter valid number of command you wish to perform: \n"); 
            }
        }
        if(input != 7){
            printMainMenu();
        }
    }

    /**
     * This checks if a user is logged in or not depending on if userID is -1 or an actual value
     * @return the respective boolean if user is logged in or not and is stored in static var loginChecker
     */
    public static boolean isLogin(){
        if (userID!= -1){
            loginChecker = true;
        }
        else{
            loginChecker = false;
        }
        return loginChecker;
    }
    /**
     * This checks if the username is already taken.
     * @param username the username being checked
     * @return the userID of the given username if taken or -1 otherwise
     */
    public static int isIdTaken(String username) throws SQLException{
        PreparedStatement getIDStatment;
        
        getIDStatment = conn.prepareStatement("select \"userID\" from \"users\" where (username = ?)");
        getIDStatment.setString(1, username);
        int tempID = -1;
        try{
            ResultSet rset = getIDStatment.executeQuery();
            while(rset.next()){
                tempID = rset.getInt("userID");
            }

        }
        catch(SQLException e){
            tempID = -1;
        }
        return tempID;
    }

    /**
     * This method logs in a user - storing the userID into the static var and updating the last
     * accessed timestamp.
     */
    public static void Login() throws SQLException{
        System.out.println("Welcome to login. Please enter credentials below: \n");
        System.out.println("Enter username:");
        String username = sc.nextLine();
        System.out.println("Enter password:");
        String password = sc.nextLine();
        int tempID;
        PreparedStatement getIDStatment = conn.prepareStatement("select \"userID\" from \"users\" where (username = ?) and (password = ?)");
        getIDStatment.setString(1, username);
        getIDStatment.setString(2, password);
        
        ResultSet rset = getIDStatment.executeQuery();
        while(rset.next()){
            tempID = rset.getInt("userID");
            userID = tempID;
            
        }
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        String updateLastAccess = "update users set \"lastAccess\" = '" + currentTime +"' where \"userID\" = " + userID;
        stmt.executeUpdate(updateLastAccess); 
        if(isLogin()){
            printMainMenu();
        }
        else{
            System.out.println("Could not login - try again or create an account");
            printBeginMenu();
        }
        
       
    }

    /**
     * This method is used to logout. Resets userID to -1 and loginchecker to be false'
     */
    public static void Logout(){
        userID = -1;
        loginChecker = false;
        System.out.println("You are now logged out and disconnected, goodbye ;)");
    }
    
    /**
     * This method is used to create a new account and prompts user for everything but userID which is generated
     */
    public static void createAccount() throws SQLException{
        incrementUserID = incrementCounterUserID();
        System.out.println("Welcome to Account Creation. Please enter credentials below: \n");
        System.out.println("Please enter first name:");
        String firstName = sc.nextLine();
        System.out.println("Please enter last name:");
        String lastName = sc.nextLine();
        System.out.println("Please enter new username:");
        String username = sc.nextLine();
        //check if username taken checkerID will not be -1
        int checkerID = isIdTaken(username);
        while(checkerID != -1){
            System.out.println("Username taken - please enter new username:");
            username = sc.nextLine();
            checkerID = isIdTaken(username);
        }
        System.out.println("Please enter new password:");
        String password = sc.nextLine();
        Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        PreparedStatement insertNewUser = conn.prepareStatement("insert into users values (?,?, ?,?, ?, ?,?)");
        insertNewUser.setInt(1, incrementUserID);
        insertNewUser.setString(2,username);
        insertNewUser.setString(3, password);
        insertNewUser.setString(4, firstName);
        insertNewUser.setString(5, lastName);
        insertNewUser.setTimestamp(6, currentTime);
        insertNewUser.setNull(7,java.sql.Types.TIMESTAMP);
        // System.out.println("SQL IS "+ insertNewUser);
        try {
            insertNewUser.executeUpdate();
            System.out.println("Your account has been created, please sign in to see other functionality");
            printBeginMenu();
        } catch (SQLException e) {
            e.printStackTrace();
        } 

        
    }

}
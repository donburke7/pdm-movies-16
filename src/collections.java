//all needed imports to do the appropriate things
import java.util.Scanner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;


public class collections {
    //extend serenas file and use getter to get userID
    static Scanner scanner = new Scanner(System.in);
     

    static int printMenu() {
        System.out.println("\nWelcome to the collections menu!");
        System.out.println("Here are the available commands:\n");
        System.out.println("0: Reprint the collections menu");
        System.out.println("1: Create a collection");
        System.out.println("2: View a List of Your Collections");
        System.out.println("3: Delete a collection");
        System.out.println("4: Add a movie to a collection");
        System.out.println("5: Delete a movie from a collection");
        System.out.println("6: Modify collection\n");
        System.out.println("Enter the number that corresponds to the command you wish to execute:");
        int command=Integer.parseInt(scanner.nextLine());
        return command;

    }
    static void createCollection(Connection conn, int userID) throws SQLException{
        
 // PreparedStatement statement = conn.prepareStatement("select * from movie where \"movieID\" = 19995");

            // ResultSet resultSet = statement.executeQuery();
            int collID = 0; 
            PreparedStatement statement = conn.prepareStatement("select MAX(\"collectionID\") from \"collection\"");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                collID= resultSet.getInt(1);
            }
               
            //increment the max id by 1 
            int newCollectionID=collID+1;
            System.out.println(newCollectionID);
            //prompt for the name of the collection
            String collectionName="";
            System.out.println("Enter a name for your collection: ");
            String nameInput = scanner.nextLine();

            
            //check name for null
            if( nameInput.isEmpty()){
                collectionName="collection";
            }else{
                collectionName=nameInput;
            }


            //SQL statement to create a new collection
            // INSERT INTO "Collection" Values(collectionID, userID, collectionName)
            
            //example prepared statement with variables 
            // PreparedStatement statement = conn.prepareStatement("select * from movie where \"movieID\" = ?");
            // statement.setInt(1,)
            statement = conn.prepareStatement("Insert into \"collection\" values(?,?,?)");
            statement.setInt(1,newCollectionID);
            statement.setInt(2,userID);
            statement.setString(3,collectionName);
            statement.executeQuery();
        
       
        //at this point, collecitonID, userID, and name are known
        //sql statment to create colleciton
        //INSERT INTO "Collection" Values(collectionID, userID, collectionName)

        
    }
    
    
    /**
     * Users will be able to see the list of all their collections of movies 
     * IN ASCENDING ORDER 
     * Collections name
     * number of movies in the collections
     */
    static void viewCollections(int userID){
        
        System.out.println("Here is a list of your collections:\n");

    }
    static void deleteCollection(Connection conn,int userID) throws SQLException{
        System.out.println("Enter the name of the collection you would like to delete: ");
        String collectionName=scanner.nextLine();
        PreparedStatement statement = conn.prepareStatement("delete from \"collection\", \"contains\" where collectionName=?");
        statement.setString(1,collectionName);
        statement.executeUpdate();

        //sql command 
        //Delete From "Collection", "Contains" WHERE collectionName = collectionName
    }
    static void modifyCollection(Connection conn) throws SQLException{
        System.out.println("Enter the name of the collection you would like to change: ");
        String oldName = scanner.nextLine();

        while(oldName.isEmpty()){
            System.out.println("The name you input was not valid.\nEnter the name of the collection you would like to change.");
            oldName=scanner.nextLine();
        }

        System.out.println("Enter the new collection name: " );
        String newName = "";
        String nameInput=scanner.nextLine();
        if(nameInput.isEmpty()){
            newName = "collection";
        }else{{ 
            newName=nameInput;
        }}   
        
        //get the collectionID for the old name: 
        PreparedStatement statement = conn.prepareStatement("select \"collectionID\" from \"collection\" where \"collectionName\"=?");
        statement.setString(1,oldName);
        ResultSet resultSet = statement.executeQuery();
        int collID=0;
        while (resultSet.next()){
                collID= resultSet.getInt(1);
            }

        // System.out.println(collID);

        statement = conn.prepareStatement("update \"collection\" set \"collectionName\"=? where \"collectionID\" = ?");
        statement.setString(1, newName);
        statement.setInt(2, collID);
        statement.executeUpdate();

        //statement = conn.prepareStatement("update \"contains")

    }
    static void addMovie(Connection conn) throws SQLException{
        //enter the name of the collection you would like to add to 
        System.out.println("Enter the name of the collection you want to add a movie to: ");
        String collectionName = scanner.nextLine();

        while(collectionName.isEmpty()){
            System.out.println("The name you input was not valid.\nEnter the name of the collection you would like to change.");
            collectionName=scanner.nextLine();
        }

        //get the id of that collection if that names exist 
        int collectionID=0;
        PreparedStatement statement = conn.prepareStatement("select \"collectionID\" from \"collection\" where exists (select \"collectionName\" from \"collection\" where \"collectionName\" =?)");
        statement.setString(1, collectionName);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            collectionID=resultSet.getInt(1);
        }

    
        //Enter the name of the movie you would like to add to the collection
        System.out.println("Enter the name of the movie you would like to add: ");
        String movieName = scanner.nextLine();

        while(movieName.isEmpty()){
            System.out.println("The movie you input was not valid.\nEnter the name of the movie you would like to add.");
            movieName=scanner.nextLine();
        }

        statement = conn.prepareStatement("select \"movieID\" from \"movie\" where \"title\" = ?");
        statement.setString(1, movieName);
        
    }
    static void deleteMovie(){}


    public static void main(String[] args) throws SQLException{
        int command = printMenu();
        
        
        
        int lport = 5432; 
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432; 
        String user; 
        String password; 

        //get the username and password for logging into the database
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader ("pdm-movies-16/dataSources/credentials.txt"))){
            user = bufferedReader.readLine();
            password = bufferedReader.readLine();
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        // System.out.println(user);
        // System.out.println(password);
        String databaseName = "p320_16";

        String driverName = "org.postgresql.Driver";

        Connection conn = null; 
        Session session = null;

        try{ 
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");


            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
            String url = "jdbc:postgresql://127.0.0.1:" + assigned_port + "/" + databaseName;

            java.util.Properties props = new java.util.Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);

             int userID = 2;
        if (command == 0){
            command=printMenu();
        }else if (command == 1){
            try {
                createCollection(conn,userID);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
           
        }else if (command == 2){
            viewCollections(userID);
        }else if (command == 3){
            deleteCollection(conn,userID);
        }else if (command == 4){
            addMovie(conn);
        }else if (command == 5){
            deleteMovie();
        }else if (command ==6 ){
            modifyCollection(conn);
        }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if ( conn != null && !conn.isClosed()){
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()){
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
       

    }
}

//all needed imports to do the appropriate things
import java.util.Scanner;

import java.sql.SQLException;
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
    static void createCollection(int userID) throws SQLException{
        int lport = 5432; 
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432; 
        String user; 
        String password; 

        //get the username and password for logging into the database
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader ("pdm-movies-16/src/main/credentials.txt"))){
            user = bufferedReader.readLine();
            password = bufferedReader.readLine();
        }catch (IOException e){
            throw new RuntimeException(e);
        }

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

            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
            String url = "jdbc:postgresql://127.0.0.1:" + assigned_port + "/" + databaseName;

            java.util.Properties props = new java.util.Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);

            // PreparedStatement statement = conn.prepareStatement("select * from movie where \"movieID\" = 19995");

            // ResultSet resultSet = statement.executeQuery();
            
            PreparedStatement statement = conn.prepareStatement("select MAX(collecitonID) from collection ");
            ResultSet resultSet = statement.executeQuery();

            System.out.println(resultSet);
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

        

        //generate next CollectionID
        // int collectionID=generateID(); 
        


        
        // //prompt for the name of the collection
        // String collectionName="";
        // System.out.println("Enter a name for your collection: ");
        // String nameInput = scanner.nextLine();

        // //check name for null
        // if( nameInput.isEmpty()){
        //     collectionName="collection";
        // }else{
        //     collectionName=nameInput;
        // }

        //at this point, collecitonID, userID, and name are known
        //sql statment to create colleciton
        //INSERT INTO "Collection" Values(collectionID, userID, collectionName)

        
    }
    
    static int generateID(){
        //Select collectionID from Collections where collectionID=MAX(collectionID)
        return 0;
        //return ID; 
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
    static void deleteCollection(int userID){
        System.out.println("Enter the name of the collection you would like to delete: ");
        String collectionName=scanner.nextLine();
        //sql command 
        //Delete From "Collection", "Contains" WHERE collectionName = collectionName
    }
    static void modifyCollection(){
        
    }
    static void addMovie(){}
    static void deleteMovie(){}


    public static void main(String[] args){
        int command = printMenu();
        int userID = 2;
        if (command == 0){
            command=printMenu();
        }else if (command == 1){
            try {
                createCollection(userID);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
           
        }else if (command == 2){
            viewCollections(userID);
        }else if (command == 3){
            deleteCollection(userID);
        }

    }
}

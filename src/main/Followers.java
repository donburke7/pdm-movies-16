import java.sql.*;
import java.util.Scanner;

public class Followers {
    //users can follow another user
    //users can search for new users to follow by email 
    //users can unfollow other users

    public Scanner scanner = new Scanner(System.in);
    public int userID;
    public Connection conn;


    public Followers(int userID, Connection conn){
        this.userID=userID;
        this.conn=conn;
    }

    public int printMenu(){
        System.out.println("\nWelcome to the followers menu!");
        System.out.println("Here are the available commands:\n");
        System.out.println("0: Reprint the followers menu");
        System.out.println("1: Follow A User");
        System.out.println("2: Unfollow A User");
        System.out.println("Enter the number that corresponds to the command you wish to execute:");
        int command=Integer.parseInt(scanner.nextLine());
        return command;
    }
    public void followUser(int userID, Connection conn) throws SQLException{
        // String firstName="";
        // System.out.println("Enter the email of the user you would like to follow:");
        // String email= scanner.nextLine();
        // PreparedStatement statement = conn.prepareStatement("select \"fName\" from \"users\" where (select \"userID\" from \"email\" where \"email\" = ?)");
        // statement.setString(1, email);
        // ResultSet resultSet = statement.executeQuery();
        // while(resultSet.next()){
        //     firstName=resultSet.getString("fName");
        // }
        
        System.out.println("You have selected to follow a user.\nEnter the number that corresponds to one of the following options:");
        System.out.println("1: Follow a user by Name");
        System.out.println("2: Follow a user by Email");
    }
}

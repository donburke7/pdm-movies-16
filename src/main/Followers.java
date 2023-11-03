import java.sql.*;
import java.util.ArrayList;
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
        System.out.println("3: Return to the main menu\n");

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
        
        int result =  Integer.parseInt(scanner.nextLine());

        if(result == 1){
            System.out.println("Enter the first name of the person you would like to follow:");
            String firstName = scanner.nextLine(); 
            System.out.println("Enter the last name of the person you would like to follow:");
            String lastName = scanner.nextLine(); 
            PreparedStatement statement = conn.prepareStatement("select \"userID\" from \"users\" where \"fName\" = ? and \"lName\" = ? ");
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<Integer> userList = new ArrayList<Integer>();
           
            while(resultSet.next()){
                userList.add(resultSet.getInt("userID"));
            }

            if(userList.size()==0){
                System.out.println("Sorry the person you entered does not exist :(");

            }else if (userList.size()==1){
                statement= conn.prepareStatement("insert into \"follows\" values (?,?)");
                statement.setInt(1,userList.get(0));
                statement.setInt(2,userID);
                statement.executeUpdate();
            }else{
                System.out.println("The name you entered had multiple results.");
                for(int i=0; i< userList.size(); i++){
                    System.out.print(i+": ");
                    statement=conn.prepareStatement("select \"email\" from \"email\" where \"userID\" = ?");
                    statement.setInt(1, userList.get(i));
                    resultSet=statement.executeQuery();
                    while(resultSet.next()){
                        System.out.println(resultSet.getString("email"));
                    }
                } 
                System.out.println("Please choose the number that corresponds to the correct email address for the user:");
                int Userinput = Integer.parseInt(scanner.nextLine());
                statement=conn.prepareStatement("insert into \"follows\" values(?,?)");
                statement.setInt(1, Userinput);
                statement.setInt(2, userID);
                statement.executeUpdate();
            }

        }else if (result==2){
            System.out.println("Enter the email address of the user you would like to follow:");
            String email = scanner.nextLine();
            int following = -1; 
            PreparedStatement statement = conn.prepareStatement("select \"userID\" from \"email\" where \"email\" = ?");
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                following = resultSet.getInt("userID");
            }
            // System.out.println(following);
            if(following==-1){
                System.out.println("sorry the email you entered does not have an account :(");
            }else{
                statement=conn.prepareStatement("insert into \"follows\" values(?,?)");
                statement.setInt(1,following);
                statement.setInt(2, userID);
                statement.executeUpdate();
            }
        }


        
    
    }

     public void unfollowUser(int userID, Connection conn) throws SQLException{
        
        System.out.println("You have selected to unfollow a user.\nEnter the number that corresponds to one of the following options:");
        System.out.println("1: Unfollow a user by Name");
        System.out.println("2: Unfollow a user by Email");
        
        int result =  Integer.parseInt(scanner.nextLine());

        if(result == 1){
            System.out.println("Enter the first name of the person you would like to unfollow:");
            String firstName = scanner.nextLine(); 
            System.out.println("Enter the last name of the person you would like to unfollow:");
            String lastName = scanner.nextLine(); 
            PreparedStatement statement = conn.prepareStatement("select \"userID\" from \"users\" where \"fName\" = ? and \"lName\" = ? ");
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<Integer> userList = new ArrayList<Integer>();
           
            while(resultSet.next()){
                userList.add(resultSet.getInt("userID"));
            }

            if(userList.size()==0){
                System.out.println("Sorry the person you entered does not exist :(");

            }else if (userList.size()==1){
                statement= conn.prepareStatement("delete from \"follows\" where \"Following\"=? and \"Follower\"=?");
                statement.setInt(1,userList.get(0));
                statement.setInt(2,userID);
                statement.executeUpdate();
            }else{
                System.out.println("The name you entered had multiple results.");
                for(int i=0; i< userList.size(); i++){
                    System.out.print(i+": ");
                    statement=conn.prepareStatement("select \"email\" from \"email\" where \"userID\" = ?");
                    statement.setInt(1, userList.get(i));
                    resultSet=statement.executeQuery();
                    while(resultSet.next()){
                        System.out.println(resultSet.getString("email"));
                    }
                } 
                System.out.println("Please choose the number that corresponds to the correct email address for the user:");
                int Userinput = Integer.parseInt(scanner.nextLine());
                statement=conn.prepareStatement("delete from \"follows\" where \"Following\"=? and \"Follower\"=?");
                statement.setInt(1, Userinput);
                statement.setInt(2, userID);
                statement.executeUpdate();
            }

        }else if (result==2){
            System.out.println("Enter the email address of the user you would like to unfollow:");
            String email = scanner.nextLine();
            int following = -1; 
            PreparedStatement statement = conn.prepareStatement("select \"userID\" from \"email\" where \"email\" = ?");
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                following = resultSet.getInt("userID");
            }
            if(following==-1){
                System.out.println("sorry the email you entered does not have an account :(");
            }else{
                statement=conn.prepareStatement("delete from \"follows\" where \"Following\"=? and \"Follower\"=?");
                statement.setInt(1,following);
                statement.setInt(2, userID);
                statement.executeUpdate();
            }
        }

        
        
    
    }
}

import java.util.Scanner;
import java.util.ArrayList;
import java.sql.*;

public class UserStats {
     //extend serenas file and use getter to get userID
     static Scanner scanner = new Scanner(System.in);
   
     public int userID; 
     public Connection conn;
 
 
     public UserStats(int userID, Connection conn){
         this.userID=userID; 
         this.conn=conn;
     }
 
     public int getUserID(){
         return userID;
     }
 
     public Connection getConnection(){ 
         return conn; 
     }
      /**
      * prints the main menu 
      * @return
      */
      public void printMenu() throws SQLException {
          int command = 0;
          while (command != 5) {
              System.out.println("\nWelcome to the collections menu!");
              System.out.println("Here are the available commands:\n");
              System.out.println("1: View the number of collections you have");
              System.out.println("2: View the number of users who follow you");
              System.out.println("3: View the number of users you follow");
              System.out.println("4: View your top ten movies");
              System.out.println("5: Return to the main menu\n");
              System.out.println("Enter the number that corresponds to the command you wish to execute:");
              command = Integer.parseInt(scanner.nextLine());
 
              switch (command) {
                  case 0:
                      break;
                  case 1:
                      numCollections();
                      break;
                  case 2:
                      followingCount();
                      break;
                  case 3:
                      followsCount();
                      break;
                  case 4:
                      topTen();
                      break;
                  case 5:
                       System.out.println("Going back to main menu now");
                       break;
                  default:
                      System.out.println("Please pick a valid number");
              }
          }
     }
     public void numCollections() throws SQLException{
        int collectionCount=0;
        PreparedStatement statement = conn.prepareStatement("select count(*) from \"collection\" where \"userID\" = ?");
        statement.setInt(1,userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            collectionCount=resultSet.getInt(1);
        }
        System.out.println("You have "+collectionCount+" collections.");
    }

    /**
     * the count of who this user follows
     */
    public void followsCount() throws SQLException{
        int followCount=0;
        PreparedStatement statement = conn.prepareStatement("select count(*) from \"follows\" where \"Following\"=?");
        statement.setInt(1,userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            followCount=resultSet.getInt(1);
        }
        System.out.println("You are following "+followCount+" users.");
    }

     /**
     * the count of users who follow this person 
     */
    public void followingCount() throws SQLException{
        int followCount=0;
        PreparedStatement statement = conn.prepareStatement("select count(*) from \"follows\" where \"Follower\"=?");
        statement.setInt(1,userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            followCount=resultSet.getInt(1);
        }
        System.out.println("You have "+followCount+" followers.");

    }

    public void topTen() throws SQLException{
        System.out.println("Here is the Top 10 filter options:");
        System.out.println("1: Filter by highest rating");
        System.out.println("2: Filter by most watched");
        System.out.println("3: Filter by both highest rating and most watched");
        System.out.println("Enter the option number you would like to filter by:");
        int option = Integer.parseInt(scanner.nextLine());
        if(option==1){
            bestRated();
        }
        else if(option==2){
            mostPlays();
        }
        else if(option==3){
            combination();
        }
        

    }

    public void mostPlays() throws SQLException{
        ArrayList<String> movieNames = new ArrayList<String>();
        PreparedStatement statement = conn.prepareStatement("select m.\"title\" from \"movie\" m join \"watches\" w on m.\"movieID\" = w.\"movieID\" where w.\"userID\"=? group by m.\"movieID\" order by count(m.\"movieID\") Desc limit 10");
        statement.setInt(1,userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            movieNames.add(resultSet.getString(1));
        }

        System.out.println("\nHere are your top ten movies by most watched:");
        for(int i=0;i<movieNames.size();i++){
            System.out.print((i+1)+": ");
            System.out.println(movieNames.get(i));
        }
    }

    public void bestRated() throws SQLException{
        ArrayList<String> movieNames = new ArrayList<String>();
        PreparedStatement statement = conn.prepareStatement("select m.\"title\" from \"movie\" m join \"rates\" r on m.\"movieID\" = r.\"movieid\" where r.\"userid\"=? group by m.\"movieID\" order by max(r.\"rating\") Desc limit 10");
        statement.setInt(1,userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            movieNames.add(resultSet.getString(1));
        }

        System.out.println("\nHere are your top ten movies by top rated:");
        for(int i=0;i<movieNames.size();i++){
            System.out.print((i+1)+": ");
            System.out.println(movieNames.get(i));
        }
    }

    public void combination() throws SQLException{
        ArrayList<String> movieNames = new ArrayList<String>();
        PreparedStatement statement = conn.prepareStatement("select m.\"title\" from \"movie\" m join \"rates\" r on m.\"movieID\" = r.\"movieid\" join \"watches\" w on m.\"movieID\" = w.\"movieID\" where r.\"userid\"=? group by m.\"movieID\" order by count(w.\"movieID\"),max(r.\"rating\") Desc limit 10");
        statement.setInt(1,userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            movieNames.add(resultSet.getString(1));
        }

        System.out.println("\nHere are your top ten movies by top rated and most watched:");
        for(int i=0;i<movieNames.size();i++){
            System.out.print((i+1)+": ");
            System.out.println(movieNames.get(i));
        }
    }
}

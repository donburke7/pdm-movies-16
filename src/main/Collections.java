//all needed imports to do the appropriate things
import java.util.Scanner;
import java.util.ArrayList;
import java.sql.*;


public class Collections {
    //extend serenas file and use getter to get userID
    static Scanner scanner = new Scanner(System.in);
   
    public int userID; 
    public Connection conn;


    public Collections(int userID, Connection conn){
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
         while (command != 11) {
             System.out.println("\nWelcome to the collections menu!");
             System.out.println("Here are the available commands:\n");
             System.out.println("0: Reprint the collections menu");
             System.out.println("1: Create a collection");
             System.out.println("2: View a List of Your Collections");
             System.out.println("3: Delete a collection");
             System.out.println("4: Add a movie to a collection");
             System.out.println("5: Delete a movie from a collection");
             System.out.println("6: Modify collection");
             System.out.println("7: View the number of collections you have");
             System.out.println("8: View the number of users who follow you");
             System.out.println("9: View the number of users you follow");
             System.out.println("10: View your top ten movies");


             System.out.println("11: Return to the main menu\n");
             System.out.println("Enter the number that corresponds to the command you wish to execute:");
             command = Integer.parseInt(scanner.nextLine());

             switch (command) {
                 case 0:
                     break;
                 case 1:
                     createCollection();
                     break;
                 case 2:
                     viewCollections();
                     break;
                 case 3:
                     deleteCollection();
                     break;
                 case 4:
                     addMovie();
                     break;
                 case 5:
                     deleteMovie();
                     break;
                 case 6:
                     modifyCollection();
                     break;
                 case 7:
                     numCollections();
                     break;
                 case 8:
                      followsCount();
                      break;
                 case 9:
                      followingCount();
                      break;
                 case 10:
                      topTen();
                      break;
                 default:
                     System.out.println("Please pick a valid number");
             }
         }
    }

    /**
     * Create collection-creates a new colleciton in collection table
     * Default name if no input is Collection
     * @throws SQLException
     */
    public void createCollection() throws SQLException{
        
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
            // System.out.println(newCollectionID);
            //prompt for the name of the collection
            String collectionName="";
            System.out.println("Enter a name for your collection: ");
            String nameInput = scanner.nextLine();

            
            //check name for null
            if( nameInput.isEmpty()){
                collectionName="Collection";
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
            statement.executeUpdate();
        
       
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
    public  void viewCollections()throws SQLException{
        System.out.println("Here is a list of your collections:\n");
        PreparedStatement statement=conn.prepareStatement("(select \"collectionID\", \"collectionName\" from \"collection\" where \"userID\" =? order by \"collectionName\"  ASC)");
        statement.setInt(1, userID);
        ResultSet resultSet = statement.executeQuery(); 
        ArrayList<Integer> collectionIDList=new ArrayList<Integer>();
        ArrayList<String> collectionNameList=new ArrayList<String>();
        while(resultSet.next()){
            collectionIDList.add(resultSet.getInt("collectionID"));
            collectionNameList.add(resultSet.getString("collectionName"));
        }

        for(int i =0; i< collectionIDList.size(); i++){
            int count =0; 
            int length=0;
            System.out.println("Collection Name: "+collectionNameList.get(i));
            ArrayList<Integer> movieIDList = new ArrayList<Integer>();
            statement=conn.prepareStatement("select count(*) from \"contains\" where \"collectionID\"=? group by \"collectionID\"");
            statement.setInt(1,collectionIDList.get(i));
            resultSet=statement.executeQuery();
            while(resultSet.next()){
                // movieIDList.add(resultSet.getInt("movieID"));
                count=resultSet.getInt(1);
            }
            statement=conn.prepareStatement("select \"movieID\" from \"contains\" where \"collectionID\"= ? ");
            statement.setInt(1,collectionIDList.get(i));
            resultSet=statement.executeQuery();
            while(resultSet.next()){
                movieIDList.add(resultSet.getInt("movieID"));
            }
            System.out.println("\tNumber of movies in the collection: "+ count);
            for(int j =0; j<movieIDList.size(); j++){
                statement=conn.prepareStatement("select sum(\"length\") from \"movie\" where \"movieID\"=?");
                statement.setInt(1,movieIDList.get(i));
                resultSet=statement.executeQuery();
                while(resultSet.next()){
                length+=resultSet.getInt(1);
                }
            }
            System.out.println("\tThe total length of the collection is "+length+" minutes.");


        }

    }


    /**
     * Delete Collection - Deletes a whole collection from collections and contains
     * @throws SQLException
     */
    public void deleteCollection() throws SQLException{
        System.out.println("Enter the name of the collection you would like to delete: ");
        String collectionName=scanner.nextLine();
        int collectionID=0;
        PreparedStatement statement = conn.prepareStatement("select \"collectionID\" from \"collection\" where \"collectionName\" =? and \"userID\"=?");
        statement.setString(1,collectionName);
        statement.setInt(2, userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            collectionID=resultSet.getInt(1);
        }

        //delete from collections
        // System.out.println(collectionID);
        statement = conn.prepareStatement("delete from \"collection\" where \"collectionID\"=? ");
        statement.setInt(1, collectionID);
        statement.executeUpdate();

        //delete from contains 
        statement = conn.prepareStatement("delete from \"contains\" where \"collectionID\" =?");
        statement.setInt(1, collectionID);
        statement.executeUpdate();

        //sql command 
        //Delete From "Collection", "Contains" WHERE collectionName = collectionName
    }



    /**
     * Modify collection - modifies the name of the collection in colletion table (nothing changes in contains)
     * @throws SQLException
     */
    public void modifyCollection() throws SQLException{
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
        PreparedStatement statement = conn.prepareStatement("select \"collectionID\" from \"collection\" where \"collectionName\"=? and \"userID\"=?");
        statement.setString(1,oldName);
        statement.setInt(2, userID);
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

       
    }








    public void addMovie() throws SQLException{
        //enter the name of the collection you would like to add to 
        System.out.println("Enter the name of the collection you want to add a movie to: ");
        String collectionName = scanner.nextLine();


        while(collectionName.isEmpty()){
            System.out.println("The name you input was not valid.\nEnter the name of the collection you would like to change.");
            collectionName=scanner.nextLine();
        }

        //System.out.println(collectionName);
        //get the id of that collection if that names exist 
        int collectionID=0;
        // PreparedStatement statement = conn.prepareStatement("select \"collectionID\" from \"collection\" where \"userID\" = ? and Replace(\"collectionName\",'''','') = Replace( ?,'''','')  ");
        PreparedStatement statement = conn.prepareStatement("select \"collectionID\" from \"collection\" where \"userID\" = ? and \"collectionName\" = ?  ");

        statement.setString(2, collectionName);
        statement.setInt(1, userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            collectionID=resultSet.getInt(1);
        }

        // System.out.println(collectionID);
        int movieID=-1;
        while(movieID==-1){
            String movieName="";
            //Enter the name of the movie you would like to add to the collection
            System.out.println("Enter the name of the movie you would like to add: ");
            movieName = scanner.nextLine();
        
            while(movieName.isEmpty()){
                System.out.println("The movie you input was not valid.\nEnter the name of the movie you would like to add.");
                movieName=scanner.nextLine();
            }
            statement = conn.prepareStatement("select \"movieID\" , \"MPAA_rating\" from \"movie\" where \"title\" = ?");
            statement.setString(1,movieName);
            resultSet=statement.executeQuery();
            ArrayList<Integer> movieIDList = new ArrayList<Integer>();
            ArrayList<String> ratingList = new ArrayList<String>();

            while(resultSet.next()){
                movieIDList.add(resultSet.getInt("movieID"));
                ratingList.add(resultSet.getString("MPAA_rating"));
            }
           
            if (movieIDList.size()==1){
                movieID=movieIDList.get(0);
            }else if(movieIDList.size()>1){
                System.out.println("There are multiple movies with that title.");
                System.out.println("Please select the id of the movie you want to add:");
                for(int i=0; i< movieIDList.size(); i++){
                    System.out.println( movieIDList.get(i)+": "+movieName+" , rating: "+ratingList.get(i));
                }
                movieID=Integer.parseInt(scanner.nextLine());
                
            }

            
        }

        statement = conn.prepareStatement("insert into \"contains\" values(?,?)");
        statement.setInt(1,collectionID);
        statement.setInt(2, movieID);
        statement.executeUpdate();
        
        
    }


    public void deleteMovie() throws SQLException{
          //enter the name of the collection you would like to add to 
        System.out.println("Enter the name of the collection you want to delete a movie from: ");
        String collectionName = scanner.nextLine();


        while(collectionName.isEmpty()){
            System.out.println("The name you input was not valid.\nEnter the name of the collection you would like to change.");
            collectionName=scanner.nextLine();
        }

        //System.out.println(collectionName);
        //get the id of that collection if that names exist 
        int collectionID=0;
        PreparedStatement statement = conn.prepareStatement("select \"collectionID\" from \"collection\" where  \"collectionName\" = ? and \"userID\" = ?");
        statement.setString(1, collectionName);
        statement.setInt(2, userID);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()){
            collectionID=resultSet.getInt(1);
        }

        System.out.println(collectionID);
        statement= conn.prepareStatement("select \"movieID\" from \"contains\" where \"collectionID\" = ?");
        statement.setInt(1, collectionID);
        resultSet=statement.executeQuery();
        ArrayList<Integer> movieIDList = new ArrayList<Integer>();
        while(resultSet.next()){
            movieIDList.add(resultSet.getInt("movieID"));
        }

        ArrayList<String> movieNames = new ArrayList<String>();

        for(int i=0; i< movieIDList.size(); i++){
            int current=movieIDList.get(i);
            statement= conn.prepareStatement("select \"title\" from \"movie\" where \"movieID\" = ?");
            statement.setInt(1, current);
            resultSet=statement.executeQuery();
            while(resultSet.next()){
                movieNames.add(resultSet.getString("title"));
            }
        }
        
        System.out.println("Here are the movies in this collection:");
        for(int j=0; j<movieNames.size(); j++ ){
            System.out.println(movieIDList.get(j)+": "+movieNames.get(j));
        }

        System.out.println("Enter the ID number of the movie you would like to delete: ");
        int movieID=Integer.parseInt(scanner.nextLine());

        statement = conn.prepareStatement("delete from \"contains\" where \"collectionID\" =? and \"movieID\"=?");
        statement.setInt(1,collectionID);
        statement.setInt(2, movieID);
        statement.executeUpdate();
        
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
        PreparedStatement statement = conn.prepareStatement("select count(*) from \"follows\" where \"Follower\"=?");
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
        PreparedStatement statement = conn.prepareStatement("select count(*) from \"follows\" where \"Following\"=?");
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
        System.out.println("2: Filter by most plays");
        System.out.println("3: Filter by both highest rating and most plays");
        System.out.println("Enter the option number you would like to filter by:");
        int option = Integer.parseInt(scanner.nextLine());
        if(option==2){
            mostPlays();
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

        for(int i=0;i<movieNames.size();i++){
            System.out.println(movieNames.get(i));
        }
        
    

    
    }

}

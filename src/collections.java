import java.util.Scanner;

public class collections {
    //extend serenas file and use getter to get userID
    static Scanner scanner = new Scanner(System.in);
    static int increment=0;
    static int startID=200; 

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
    static void createCollection(int userID){
        //generate next CollectionID
        int collectionID=generateID(); 
        increment++;

        //prompt for the name of the collection
        String name="";
        System.out.println("Enter a name for your collection: ");
        String nameInput = scanner.nextLine();

        //check name for null
        if( nameInput.isEmpty()){
            name="collection";
        }else{
            name=nameInput;
        }
        
        System.out.println("you created a collection of name: "+ name+", with a collection id of: "+collectionID);
    }
    static int generateID(){
        int ID=startID+increment;
        return ID; 
    }
    static void viewCollections(){}
    static void deleteCollection(){}
    static void modifyCollection(){}
    static void addMovie(){}
    static void deleteMovie(){}


    public static void main(String[] args){
        int command = printMenu();
        int userID = 2;
        if (command == 0){
            command=printMenu();
        }else if (command == 1){
            createCollection(userID);
            createCollection(userID);
        }

    }
}

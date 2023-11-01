import java.util.Scanner;
import java.sql.*;

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
    public boolean isLogin(){
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
        // tempID = (SELECT "userID" from "Users"
        //             WHERE ("username" = username) AND
        //             ("password" = password))
        // if (tempID != null){
        //     userID = tempID;
        //     Timestamp currentTime = new Timestamp(System.currentTimeMillis());
        //     UPDATE "Users" SET lastAccess = currentTime
        //     where userID = userID
        // }
        // else{
        //     System.out.println("Could not login - try again or create an account");
        // }
    }

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
        
        // INSERT INTO "Users" VALUES 
        // "(incrementUserID, username, password, firstName, lastName, currentTime, null);

        // incrementUserID++;

    }
}
package ScraperClass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DataBase {

    static String driver = "jdbc:sqlserver:";
    static String url = "//lcmdb.cbjmpwcdjfmq.us-east-1.rds.amazonaws.com:";
    static String port = "1433";
    static String username = "DS3";
    static String password = "Touro123";
    static String database = "DS3";
    static PreparedStatement state;
    static String connection = driver + url + port
            + ";databaseName=" + database + ";user=" + username + ";password=" + password + ";";

    public static void addUrl(String url) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("Driver Successfully Loaded!");
            
            try (Connection connect = DriverManager.getConnection(connection)) {
                System.out.println("Connected to Database!");
                
                state = connect.prepareStatement("INSERT INTO Websites VALUES(\'" + url
                        + "\')");
                state.execute();
                
                System.out.println("Query Executed Successfully!");
            }

        } catch (ClassNotFoundException ex) {
            System.out.println("Error: Driver Class not found.");
            ex.printStackTrace();
        } catch (SQLException sqlex) {
            System.out.println("Error: SQL Error");
            sqlex.printStackTrace();
        }
    }
    
    public static void addEmail(String email) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("Driver Successfully Loaded!");
            
            try (Connection connect = DriverManager.getConnection(connection)) {
                System.out.println("Connected to Database!");
                
                state = connect.prepareStatement("INSERT INTO Websites VALUES(\'" + email
                        + "\')");
                state.execute();
                
                System.out.println("Query Executed Successfully!");
            }

        } catch (ClassNotFoundException ex) {
            System.out.println("Error: Driver Class not found.");
            ex.printStackTrace();
        } catch (SQLException sqlex) {
            System.out.println("Error: SQL Error");
            sqlex.printStackTrace();
        }
    }
}

package Model;
import java.sql.*;

/**
 * Created by davinci on 8/5/16.
 */

/**
    THIS CLASS IS THE DRIVER FOR DO THE DATABASE'S CONNECTION (DATABASE: MYSQL)
    DATABASE IP: 127.0.0.1 or localhost ---> BECAUSE THE DATABASE IN THE LOCAL MACHINE
    DATABASE NAME: UNICOL
    USER NAME: user1
    USER PASSWORD: password1
*/
public class DatabaseConnection {

    public Connection databaseConnection(){
        String databaseIP = "localhost";
        String databasename = "UNICOL";
        String username = "user1";
        String userpassword = "password1";
        String connectionUrl = "jdbc:mysql://"+databaseIP+"/"+databasename+"?" + "user="+username+"&password="+userpassword+"";
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(connectionUrl);
            System.out.println("CONNECTION TO DATABASE: SUCCESSFUL");
        } catch (SQLException e) {
            System.out.println("SQL Exception: "+ e.toString());
            System.exit(0); //STOP BECAUSE THE APPLICATION MUST BE CONNECTED TO THE DATABASE
        } catch (ClassNotFoundException cE) {
            System.out.println("Class Not Found Exception: "+ cE.toString());
            System.exit(0); //STOP BECAUSE THE APPLICATION MUST BE CONNECTED TO THE DATABASE
        }
        return con;
    }
}


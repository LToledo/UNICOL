package Model;

/**
 * Created by davinci on 8/8/16.
 */

import com.sun.applet2.preloader.event.InitEvent;
import com.sun.corba.se.impl.interceptors.ServerRequestInfoImpl;
import com.sun.corba.se.spi.orbutil.fsm.Guard;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.sun.org.apache.bcel.internal.generic.INEG;
import com.sun.org.apache.xml.internal.security.algorithms.implementations.IntegrityHmac;
import org.w3c.dom.ls.LSInput;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.stream.IntStream;


/**
 * This class has the method to connect to database and also has the only connection to this database
 * This class also has contain all methods to do inserts, updates, consults (gets) and deletes on the database
 */
public class ModelFunctions {

    public static Connection con = null;
    public static boolean debug = true;

    //Connection to database to start
    public static void databaseConnetion(){
        //Connection to database
        DatabaseConnection connection = new DatabaseConnection();
        con = connection.databaseConnection();

        if (con == null) {
            System.out.println("CONNECTION REFUSE!!");
        }

        if (debug)
            System.out.println("CONNECTED!! LETS START!!!");
        //Connection to database done!!!

        //DO NOT FORGET TO CLOSE THE DATABASE CONNECTION!!!
    }

    /**
     * MOCKUP -> INSERIR/APAGAR LOCALIZACAO
     * Add a new location (The 3 fields need to be fill and not with an empty string)
     * @param name (Location name)
     * @param department (Department where is the equipment)
     * @param room (Which room is the equipment)
     * @return (return FALSE if occur an error or if this supposedly new location already exist) (return TRUE if create this new location correctly)
     */
    public static boolean addnewLocation(String name, String department, String room){
        java.sql.Statement stmt;
        try {
            //First verify if this supposedly new location already exist or not (actually_used is a field for we know if this is used yet(1) or if this already was deleted but it is still saved on historic table(2))
            String query = "select name, department, room from Location where name like '"+name+"' and department like '"+department+"' and room like '"+room+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                //If enter here, it is because this location already exist, so it does not need to be create again
                return false;
            }
            //otherwise, it needs to be create
            String query2 = "Insert into Location (name,department,room,actually_used)"+ "values (?,?,?,?)";
            PreparedStatement preparedStmt= con.prepareStatement(query2);
            preparedStmt.setString(1, name);
            preparedStmt.setString(2, department);
            preparedStmt.setString(3, room);
            preparedStmt.setString(4, "1"); //1 -> true
            if(debug) {
                System.out.println("Insert a new Location!!!");
                System.out.println("Name:" + name);
                System.out.println("Department:" + department);
                System.out.println("room:" + room);
            }
            preparedStmt.execute();
        } catch (SQLException Ex) {
            System.out.println("Error creating a new location - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * MOCKUP -> INSERIR/APAGAR LOCALIZACAO
     * Delete a specific location if this exist (The 3 fields need to be fill and not with an empty string)
     * The specific location is not really eliminated because we need to save the historic of all equipments, so for this reason we need to save everything,
     * so what the program does it is update the field "actually_used" on the table "Equipments" (to 0 in this case, because 1 means that the location is used yet)
     * When the program change this field "actually_used" on the table "Equipments", at the same time we need to save all records of this table that have this location,
     * because we the location supposedly was eliminated, so we need save all records about this location on the "Historic" table.
     * @param name (Location name)
     * @param department (Department where is the equipment)
     * @param room (Which room is the equipment)
     * @return (return FALSE if occur some error or if this location does not exist) (Return TRUE if this specific location is deleted correctly)
     */
    public static boolean deleteLocation(String name, String department, String room){
        java.sql.Statement stmt;
        try {
            //First verify if this supposedly location already exist or not
            String query = "SELECT  location_id FROM Location WHERE name LIKE '"+name+"' and department LIKE '"+department+"' and room LIKE '"+room+"' and actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            int id_row = 0;
            if (result.next()){
                //If enter here, it is because this location exist, so it will be erased.. for this, we need the id of this row on database
                id_row = Integer.parseInt(result.getString(1));
                String query2 = "UPDATE Location SET actually_used=? WHERE location_id=?";
                PreparedStatement preparedStmt= con.prepareStatement(query2);
                preparedStmt.setString(1, "0");
                preparedStmt.setInt(2, id_row);
                preparedStmt.execute();
                if(debug) {
                    System.out.println("Delete a Location!!!");
                    System.out.println("Name:" + name);
                    System.out.println("Department:" + department);
                    System.out.println("room:" + room);
                }
                //Now it is necessary change the records with this location that are at table Equipments and save them now on the table Historic
                String location, family, category, date, code, status, query_insert;
                String query3 = "SELECT id_location, id_family, id_category, id_date, code, status FROM Equipments WHERE id_location LIKE '"+id_row+"'";
                stmt = con.createStatement();
                ResultSet result2 = stmt.executeQuery(query3);
                while (result2.next()){
                    //Get values from table Equipments
                    location = result2.getString(1);
                    family = result2.getString(2);
                    category = result2.getString(3);
                    date = result2.getString(4);
                    code = result2.getString(5);
                    status = result2.getString(6);
                    //Insert values on table Historic
                    query_insert = "INSERT INTO Historic (id_location, id_family, id_category, id_date, code, status) VALUES (?,?,?,?,?,?)";
                    PreparedStatement preparedStmt2 = con.prepareStatement(query_insert);
                    preparedStmt2.setString(1, location);
                    preparedStmt2.setString(2, family);
                    preparedStmt2.setString(3, category);
                    preparedStmt2.setString(4, date);
                    preparedStmt2.setString(5, code);
                    preparedStmt2.setString(6, status);
                    preparedStmt2.execute();
                }
                if (debug){
                    System.out.println("Change the products!!!");
                }
            }
            else{
                //If this location does not exist
                if (debug)
                    System.out.println("Does not exist this location!!!");
                return false;
            }

        } catch (SQLException Ex) {
            System.out.println("Error deleting a specific location - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * MOCKUP -> INSERIR/APAGAR FAMILIA
     * Add a new family to the product
     * @param name (Family name)
     * @return (return FALSE if occur some error or if this location already exist) (return TRUE if create the new location with success)
     */
    public static boolean addNewFamily (String name){
        java.sql.Statement stmt;
        try {
            //First verify if this supposedly new family already exist or not
            String query = "select name from Family where name like '"+name+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                //If enter here, it is because this family already exist, so it does not need to be create again
                return false;
            }
            //otherwise, it needs to be create
            String query2 = "Insert into Family (name, actually_used)"+ "values (?,?)";
            PreparedStatement preparedStmt= con.prepareStatement(query2);
            preparedStmt.setString(1, name);
            preparedStmt.setString(2, "1");
            if(debug) {
                System.out.println("Insert a new Location!!!");
                System.out.println("Name:" + name);
            }
            preparedStmt.execute();
        } catch (SQLException Ex) {
            System.out.println("Error creating a new family - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * MOCKUP -> INSERIR/APAGAR FAMILIA
     * Delete a specific family
     * Here happens the same case when we need to delete a family.. the program needs to update the field "actually_used" to '0' and after it needs to save all
     * records about this family that are on "Equipments" table from this table to the table "Historic"
     * @param name (Family name)
     * @return (return FALSE if occur some error or if this family does not exist) (return TRUE if this specific family is deleted correctly)
     */
    public static boolean deleteFamily(String name){
        java.sql.Statement stmt;
        try {
            //First verify if this supposedly family already exist or not
            String query = "SELECT  family_id FROM Family WHERE name LIKE '"+name+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            int id_row = 0;
            if (result.next()){
                //If enter here, it is because this family exist, so it will be erased.. for this, we need the id of this row on database
                id_row = Integer.parseInt(result.getString(1));
                String query2 = "UPDATE Family SET actually_used=? WHERE location_id=?";
                PreparedStatement preparedStmt= con.prepareStatement(query2);
                preparedStmt.setString(1, "0");
                preparedStmt.setInt(2, id_row);
                preparedStmt.execute();
                if(debug) {
                    System.out.println("Delete a family!!!");
                    System.out.println("Name:" + name);
                }
                //Now it is necessary change the records with this family that are at table Equipments and save them now on the table Historic
                String location, family, category, date, code, status, query_insert;
                String query3 = "SELECT id_location, id_family, id_category, id_date, code, status FROM Equipments WHERE id_family LIKE '"+id_row+"'";
                stmt = con.createStatement();
                ResultSet result2 = stmt.executeQuery(query3);
                while (result2.next()){
                    //Get values from table Equipments
                    location = result2.getString(1);
                    family = result2.getString(2);
                    category = result2.getString(3);
                    date = result2.getString(4);
                    code = result2.getString(5);
                    status = result2.getString(6);
                    //Insert values on table Historic
                    query_insert = "INSERT INTO Historic (id_location, id_family, id_category, id_date, code, status) VALUES (?,?,?,?,?,?)";
                    PreparedStatement preparedStmt2 = con.prepareStatement(query_insert);
                    preparedStmt2.setString(1, location);
                    preparedStmt2.setString(2, family);
                    preparedStmt2.setString(3, category);
                    preparedStmt2.setString(4, date);
                    preparedStmt2.setString(5, code);
                    preparedStmt2.setString(6, status);
                    preparedStmt2.execute();
                }
                if (debug){
                    System.out.println("Change the products!!!");
                }
            }
            else{
                //If this location does not exist
                if (debug)
                    System.out.println("Does not exist this family!!!");
                return false;
            }

        } catch (SQLException Ex) {
            System.out.println("Error deleting a specific family - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * MOCKUP -> INSERIR/APAGAR CATEGORIA
     * Add a new Category to the product
     * @param name (Category name)
     * @return (return FALSE if this category already exist or if occur some error) (return TRUE if create this new category correctly)
     */
    public static boolean addNewCategory(String name){
        java.sql.Statement stmt;
        try {
            //First verify if this supposedly new category already exist or not
            String query = "select name from Category where name like '"+name+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                //If enter here, it is because this category already exist, so it does not need to be create again
                return false;
            }
            //otherwise, it needs to be create
            String query2 = "Insert into Category (name, actuallu_used)"+ "values (?,?)";
            PreparedStatement preparedStmt= con.prepareStatement(query2);
            preparedStmt.setString(1, name);
            preparedStmt.setInt(2, 1);
            if(debug) {
                System.out.println("Insert a new Category!!!");
                System.out.println("Name:" + name);
            }
            preparedStmt.execute();
        } catch (SQLException Ex) {
            System.out.println("Error creating a new category - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * MOCKUP -> INSERIR/APAGAR CATEGORIA
     * Delete a specific category
     * Here happens the same case when we need to delete a category.. the program needs to update the field "actually_used" to '0' and after it needs to save all
     * records about this family that are on "Equipments" table from this table to the table "Historic"
     * @param name (Category name)
     * @return (return FALSE when this specific category does not exist or when occur some error) (return TRUE when the specific category is deleted correctly)
     */
    public static boolean deleteCategory(String name){
        java.sql.Statement stmt;
        try {
            //First verify if this supposedly category already exist or not
            String query = "SELECT  category_id FROM Category WHERE name LIKE '"+name+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            int id_row = 0;
            if (result.next()){
                //If enter here, it is because this category exist, so it will be erased.. for this, we need the id of this row on database
                id_row = Integer.parseInt(result.getString(1));
                String query2 = "UPDATE Category SET actually_used=? WHERE category_id=?";
                PreparedStatement preparedStmt= con.prepareStatement(query2);
                preparedStmt.setString(1, "0");
                preparedStmt.setInt(2, id_row);
                preparedStmt.execute();
                if(debug) {
                    System.out.println("Delete a family!!!");
                    System.out.println("Name:" + name);
                }
                //Now it is necessary change the records with this family that are at table Equipments and save them now on the table Historic
                String location, family, category, date, code, status, query_insert;
                String query3 = "SELECT id_location, id_family, id_category, id_date, code, status FROM Equipments WHERE id_category LIKE '"+id_row+"'";
                stmt = con.createStatement();
                ResultSet result2 = stmt.executeQuery(query3);
                while (result2.next()){
                    //Get values from table Equipments
                    location = result2.getString(1);
                    family = result2.getString(2);
                    category = result2.getString(3);
                    date = result2.getString(4);
                    code = result2.getString(5);
                    status = result2.getString(6);
                    //Insert values on table Historic
                    query_insert = "INSERT INTO Historic (id_location, id_family, id_category, id_date, code, status) VALUES (?,?,?,?,?,?)";
                    PreparedStatement preparedStmt2 = con.prepareStatement(query_insert);
                    preparedStmt2.setString(1, location);
                    preparedStmt2.setString(2, family);
                    preparedStmt2.setString(3, category);
                    preparedStmt2.setString(4, date);
                    preparedStmt2.setString(5, code);
                    preparedStmt2.setString(6, status);
                    preparedStmt2.execute();
                }
                if (debug){
                    System.out.println("Change the products!!!");
                }
            }
            else{
                //If this location does not exist
                if (debug)
                    System.out.println("Does not exist this category!!!");
                return false;
            }

        } catch (SQLException Ex) {
            System.out.println("Error deleting a specific category - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * Add a new date (but depend if this date already exist or not)
     * This method first try find an equal date and if it finds, just return the ID of the specific date, and so if this date does not exist yet,
     * the method creates a new record with the specific date!
     * Protections like verify the day, month or year.. this is does in the other side, when the user put the inputs!!!
     * @param day (number of the day)
     * @param month (number od the month)
     * @param year (number of the year)
     * @return (return 0 if occur some error) (return the ID of the record when the specific already exist or if it creates a new record with this specific date)
     */
    public static int addNewDate(int day, int month, int year){
        java.sql.Statement stmt;
        int id = 0;
        try {
            //First verify if this supposedly new date already exist or not
            String query = "select date_id from Date where day like '"+day+"' AND month like '"+month+"' AND year LIKE '"+year+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                //If enter here, it is because this date already exist, so just need the row's ID
                id = Integer.parseInt(result.getString(1));
            }
            //otherwise, it needs to be create
            else {
                String query2 = "Insert into Date (day, month, year)" + "values (?,?,?)";
                PreparedStatement preparedStmt = con.prepareStatement(query2);
                preparedStmt.setString(1, Integer.toString(day));
                preparedStmt.setString(2, Integer.toString(month));
                preparedStmt.setString(3, Integer.toString(year));
                if (debug) {
                    System.out.println("Insert a new Category!!!");
                    System.out.println("Day:" + day);
                    System.out.println("Month:" + month);
                    System.out.println("Year:" + year);
                }
                preparedStmt.execute();
                //Now, it needs return also the row's ID of the new date to be used after in other method
                stmt = con.createStatement();
                result = stmt.executeQuery(query);
                if (result.next()){
                    //If enter here, it is because this date already exist, so just need the row's ID
                    id = Integer.parseInt(result.getString(1));
                }
            }
        } catch (SQLException Ex) {
            System.out.println("Error selecting or creating a date - SQL error!!!");
            System.out.println(Ex);
            return id; // 0 in this case!!!
        }
        return id;
    }

    /**
     * This method is used just to verify all fields about a specific product to know if this already exist on the table "Equipments" exactly with these information in all fields
     * @param location_name (Location name where is the product)
     * @param location_department (Department where is the product)
     * @param location_room (Room where is the location)
     * @param family (Family of the product)
     * @param category (Category of the product)
     * @param date_day (Day of the product's registration )
     * @param date_month (Month of the product's registration)
     * @param date_year (Year of the product's registration)
     * @param code (Product's code that identify him)
     * @param status (Product's status that describe if the specific product is operational or not)
     * @param observations (This field contain the observations about the product)
     * @return (return FALSE if occur some error or if this specific record with these information already exist) (return TRUE if this record does not exist yet with these information)
     */
    public static boolean verifyAllRecordFields(String location_name, String location_department, String location_room, String family, String category, int date_day, int date_month, int date_year, String code, String status, String observations){
        java.sql.Statement stmt;
        int id_location=0, id_family=0, id_category=0, id_date=0, id_status = 0;
        //We need select all ID's that are necessary to do the select after
        //Location's ID
        try {

            String query_location = "select location_id from Location where name like '"+location_name+"' AND department like '"+location_department+"' AND room LIKE '"+location_room+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_location);
            if (result.next()){
                id_location = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex) {
            System.out.println("Error selecting location's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Family's ID
        try {
            String query_family = "select family_id from Family where name like '"+family+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_family);
            if (result.next()){
                id_family = Integer.parseInt(result.getString(1));
            }
        } catch (SQLException Ex) {
            System.out.println("Error selecting family's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Category's ID
        try {
            String query_category = "select category_id from Category where name like '"+category+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_category);
            if (result.next()){
                id_category = Integer.parseInt(result.getString(1));
            }
        } catch (SQLException Ex) {
            System.out.println("Error selecting category's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Status's ID
        try{
            String query_status = "SELECT status_id FROM Status WHERE name LIKE '"+status+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_status);
            if (result.next()){
                id_status = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting status's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Date's ID
        id_date = addNewDate(date_day,date_month,date_year);

        //Here verify if this specific product (record) already exist or not
        try{
            String query_verification = "SELECT equipments_id from Equipments where id_location like '"+id_location+"' AND id_family like '"+id_family+"' AND id_category like '"+id_category+"' AND id_date like '"+id_date+"' AND code LIKE '"+code+"' AND id_status LIKE '"+id_status+"' AND observations LIKE '"+observations+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_verification);
            if (result.next()){
                //if enter here, it is because this record already exist in the table equipments with these same information in all fields (return FALSE if already exist)
                if (debug)
                    System.out.println("This record already exist in the table Equipments with the same information on all fields!!!");
                return false;
            }
            else{
                //Return TRUE if it does not exist yet
                if (debug)
                    System.out.println("This record does not exist yet!!! It is new!!!");
                return true;
            }
        }catch (SQLException Ex){
            System.out.println("Error in record verification (all fields) - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
    }

    /**
     * This method is used just to verify if a specific product already exist or not in inventory (table "Equipments")
     * @param code (The code that identify the product)
     * @return (return FALSE if occur some error or if this specific product does not exist yet) (return TRUE if this specific product already exist)
     */
    public static boolean verifyIfProductExist(String code){
        java.sql.Statement stmt;
        try{
            String query_verify = "SELECT equipments_id FROM Equipments WHERE code LIKE '"+code+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_verify);
            if (result.next()){
                //If enter here it is because this specific product already exist on the table Equipments
                return true;
            }
        }catch (SQLException Ex){
            System.out.println("Error verifying if a specific product already exist or not - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return false;
    }

    /**
     * MOCKUP -> INSERIR/APAGAR PRODUTO
     * This method add a new product to the inventory.. add a new record on the table "Equipments" (but first we call the method "verifyIfProductExist" to verify
     * if this record already exist or not, and if not yet, after we can call this method to create a new record on the table "Equipments")
     * @param location_name (Location name where is the product)
     * @param location_department (Department where is the product)
     * @param location_room (Room where is the product)
     * @param family (Family of the product)
     * @param category (Category of the product)
     * @param date_day (Product's day of him registration)
     * @param date_month (Product's month of him registration)
     * @param date_year (Product's year of him registration)
     * @param code (Product's code that identify him)
     * @param status (Product's status that describe if the specific product is operational or not)
     * @param observations (This field contain some observations about this product that the user can write, like fix something.. etc)
     * @return (return FALSE if occur some error) (return TRUE when the new record about the new product is create)
     */
    public static boolean addNewEquipement(String location_name, String location_department, String location_room, String family, String category, int date_day, int date_month, int date_year, String code, String status, String observations){
        java.sql.Statement stmt;
        int id_location=0, id_family=0, id_category=0, id_date=0, id_status=0;

        //Get ID's from selected location, family, category and date.. because this ID are created a priori
        //Location's ID
        try {
            String query_location = "select location_id from Location where name like '"+location_name+"' AND department like '"+location_department+"' AND room LIKE '"+location_room+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_location);
            if (result.next()){
                id_location = Integer.parseInt(result.getString(1));
            }
        } catch (SQLException Ex) {
            System.out.println("Error selecting location's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //family's ID
        try {
            String query_family = "select family_id from Family where name like '"+family+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_family);
            if (result.next()){
                id_family = Integer.parseInt(result.getString(1));
            }
        } catch (SQLException Ex) {
            System.out.println("Error selecting family's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Category's ID
        try {
            String query_category = "select category_id from Category where name like '"+category+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_category);
            if (result.next()){
                id_category = Integer.parseInt(result.getString(1));
            }
        } catch (SQLException Ex) {
            System.out.println("Error selecting category's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Status's ID
        try{
            String query_statys = "SELECT status_id FROM Status WHERE name LIKE '"+status+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_statys);
            if (result.next()){
                id_status = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting status's ID - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Date's ID
        id_date = addNewDate(date_day,date_month,date_year);

        //Verify if this equipment already exist and get some fields that will be need after.. because it is necessary to save the last record at Historic table and then after, update the record in this table
        try{
            String query_verify_equipment = "INSERT INTO Equipments (id_location, id_family, id_category, id_date, id_status, code, observations) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = con.prepareStatement(query_verify_equipment);
            preparedStatement.setString(1, Integer.toString(id_location));
            preparedStatement.setString(2, Integer.toString(id_family));
            preparedStatement.setString(3, Integer.toString(id_category));
            preparedStatement.setString(4, Integer.toString(id_date));
            preparedStatement.setString(5, Integer.toString(id_status));
            preparedStatement.setString(6, code);
            preparedStatement.setString(7, observations);
            preparedStatement.execute();
        } catch (SQLException Ex) {
            //Logger.getLogger(class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error creating new equipment record - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * This method is to get all locations(name, department, room) from the table "Location"
     * @param locations_list (an arrayList to add all maps with the information about each location contained in the table "Location" and pass this by parameter)
     * @return (return FALSE if occur some error) (return TRUE if create the maps with the information about each location and add these maps to the arrayList in parameter)
     */
    public static boolean allLocations(ArrayList locations_list){
        java.sql.Statement stmt;
        try{
            String query = "SELECT name, department, room FROM Location WHERE actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                //Each record is a hashmap because after it is better for us separate each field from each row
                Map aux = new HashMap();
                aux.put("name", result.getString(1));
                aux.put("department", result.getString(2));
                aux.put("room", result.getString(3));
                locations_list.add(aux);
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting all Locations - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * This method is to get all families from the table "Family"
     * @param families_list (an arrayList of strings to add all families names from the table "Family" and pass this by parameter)
     * @return (return FALSE if occur some error) (return TRUE if add correctly all families names to the arrayList in parameter)
     */
    public static boolean allFamily(ArrayList<String> families_list){
        java.sql.Statement stmt;
        try{
            String query = "SELECT name FROM Family WHERE actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                //Save each row selected
                families_list.add(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting all Families - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * This method is to get all categories from the table "Category"
     * @param categories_list (an arrayList of strings to add all categories names from the table "Category" and pass this by parameter)
     * @return (return FALSE if occur some error) (return TRUE if add correctly all categories names to the arrayList in parameter)
     */
    public static boolean allCategories(ArrayList<String> categories_list){
        java.sql.Statement stmt;
        try{
            String query = "SELECT name FROM Category WHERE actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                //Save each row selected
                categories_list.add(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting all Categories - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * This method is to get an ID about a specific location (default -> 1)
     * (1 -> ON - used yet || 0 -> OFF - not used already)
     * @param name (Location's name)
     * @param department (Department's name)
     * @param room (Room's name)
     * @return (return 0 if occur some error) (return the ID of this specific location if her exist)
     */
    public static int getSpecificLocationID(String name, String department, String room){
        java.sql.Statement stmt;
        int location_id = 0;
        try{
            String query = "SELECT location_id FROM Location WHERE name LIKE '"+name+"' AND department LIKE '"+department+"' AND room LIKE '"+room+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            //If exist this specific location
            if (result.next()){
                location_id = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error getting a specific location's ID - SQL error!!!");
            System.out.println(Ex);
            return location_id;
        }
        return location_id;
    }

    /**
     * This method is to get a specific family's ID (default -> 1)
     * (1 -> ON - used yet || 0 -> OFF - not used already)
     * @param name (Family's name)
     * @return (return 0 if occur some error) (return the ID of this specific family if her exist)
     */
    public static int getSpecificFamilyID(String name){
        java.sql.Statement stmt;
        int family_id = 0;
        try{
            String query = "SELECT family_id FROM Family WHERE name LIKE '"+name+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            //If exist this specific family
            if (result.next()){
                family_id = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting a specific family's ID - SQL error!!!");
            System.out.println(Ex);
            return family_id;
        }
        return family_id;
    }

    /**
     * This method is to get a specific category's ID (default -> 1)
     * (1 -> ON - used yet || 0 -> OFF - not used already)
     * @param name (Category's name)
     * @return (return 0 if occur some error) (return the ID of this specific category if her exist)
     */
    public static int getSpecificCategoryID(String name){
        java.sql.Statement stmt;
        int category_id = 0;
        try{
            String query = "SELECT category_id FROM Category WHERE name LIKE '"+name+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            //If this specific category exist
            if (result.next()){
                category_id = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error getting a specific category's ID - SQL error!!!");
            System.out.println(Ex);
            return category_id;
        }
        return category_id;
    }

    /**
     * This method is to get all information about a specific location receiving just her ID (name, department, room)
     * @param locationID (The ID of the specific location)
     * @return (return an array with 3 positions but all of them empty if occur some error) (return an array with the information this specific location if exist)
     */
    public static String[] getLocationInformation(int locationID){
        java.sql.Statement stmt;
        String  []locationInformation = {"", "", ""};
        String query_locationInformation = "SELECT name, department, room FROM Location where location_id LIKE '"+locationID+"' AND actually_used LIKE '1'";
        try {
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_locationInformation);
            if (result.next()) {
                locationInformation[0] = result.getString(1);
                locationInformation[1] = result.getString(2);
                locationInformation[2] = result.getString(3);
            }
        }catch (SQLException Ex){
            System.out.println("Error getting all information about a specific location - SQL error!!!");
            System.out.println(Ex);
            return locationInformation;
        }
        return locationInformation;
    }

    /**
     * This method is to get the name of a specific family receiving just her ID
     * @param id (The ID of the specific family)
     * @return (return "" if occur some error) (return the ID of this specific family if her exist)
     */
    public static String getFamilyName(int id){
        java.sql.Statement stmt;
        String familyName = "";
        try{
            String query = "SELECT name FROM Family WHERE family_id LIKE '"+id+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                familyName = result.getString(1);
            }
        }catch (SQLException Ex){
            System.out.println("Error getting the name of a specific family - SQL error!!!");
            System.out.println(Ex);
            return familyName;
        }
        return familyName;
    }

    /**
     * This method is to get the name of a specific category receiving just her ID
     * @param id (The ID of the specific category)
     * @return (return "" if occur some error) (return the ID of this specific category if her exist)
     */
    public static String getCategoryName(int id){
        java.sql.Statement stmt;
        String categoryName = "";
        try{
            String query = "SELECT name FROM Category WHERE category_id LIKE '"+id+"' AND actually_used LIKE '1'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                categoryName = result.getString(1);
            }
        }catch (SQLException Ex){
            System.out.println("Error getting the name of a specific category _ SQL error!!!");
            System.out.println(Ex);
            return categoryName;
        }
        return categoryName;
    }

    /**
     * This method is to get all information about a specific date receiving just her ID
     * @param dateID (The ID of the specific date)
     * @return (return an array with 3 positions with all fields empty ("") if occur some error) (return an array with all information (day,month,year) about this specific date if her exist)
     */
    public static String[] getDateInformation(int dateID){
        java.sql.Statement stmt;
        String []dateInformation = {"", "", ""};
        try{
            String query = "SELECT day, month, year FROM Date WHERE day LIKE '"+dateID+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                dateInformation[0] = result.getString(1); //day
                dateInformation[1] = result.getString(2); //month
                dateInformation[2] = result.getString(3); //year
            }
        }catch (SQLException Ex){
            System.out.println("Error getting information about a specific date - SQL error!!!");
            System.out.println(Ex);
            return dateInformation;
        }
        return dateInformation;
    }

    /**
     * This method is to get the ID of a specific status receiving just her name
     * @param name (Status's name)
     * @return (return 0 if occur some error) (return the ID of the specific status if his exist)
     */
    public static int getStatusID(String name){
        java.sql.Statement stmt;
        int status_id = 0;
        try{
            String query = "SELECT status_id FROM Status WHERE name LIKE '"+name+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                status_id = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting the status's ID - SQL error!!!");
            System.out.println(Ex);
            return status_id;
        }
        return status_id;
    }

    /**
     * This method is to get a name about a specific status receiving just his name
     * @param id (The ID of the specific status)
     * @return (return a string empty ("") if occur some error) (return the correspondent name of this specific status if his exist)
     */
    public static String getStatusName(int id){
        java.sql.Statement stmt;
        String status_name = "";
        try{
            String query = "SELECT name FROM Status WHERE status_id LIKE '"+id+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                status_name = result.getString(1);
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting the status's name - SQL error!!!");
            System.out.println(Ex);
            return status_name;
        }
        return status_name;
    }

    /**
     * This method is to get an ID of a specific equipment in the table 'Equipments' or 'Historic'
     * @param code (Code that identify the specific product)
     * @param tableName (Table where the method will execute the select)
     * @return (return FALSE if occur some error) (return TRUE if run everything correctly)
     */
    public static int getEquipmentID(String code, String tableName){
        java.sql.Statement stmt;
        int equipment_id = 0;
        try{
            String query = "SELECT equipemnts_id FROM " + tableName + " WHERE code LIKE '"+code+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                equipment_id = Integer.parseInt(result.getString(1));
            }
        }catch (SQLException Ex){
            System.out.println("Error selecting the ID of a specific equipment - SQL error!!!");
            System.out.println(Ex);
            return equipment_id;
        }
        return equipment_id;
    }

    /**
     * MOCKUP -> PROCURAR PRODUTO
     * This method id to get all information about a specific product that is in the table 'Equipments' just selecting by the field 'code'
     * @param informationProduct (An arrayList to add the map with all information about the specific product)
     * @param code (code that identify this product)
     * @return (return FALSE if occur some error) (return TRUE if get all information correctly without errors)
     */
    public static boolean getProductInformationByCode(ArrayList informationProduct, String code){
        java.sql.Statement stmt;
        String locationID, familyID, categoryID, dateID, statusID;
        String familyName, categoryName, statusName, observations;
        String []date;
        String []locationName;
        try{
            //Get the product's information that has this code
            String query = "SELECT id_location, id_family, id_category, id_date, id_status, observations FROM Equipments WHERE code LIKE '"+code+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            if (result.next()){
                locationID = result.getString(1);
                familyID = result.getString(2);
                categoryID = result.getString(3);
                dateID = result.getString(4);
                statusID = result.getString(5);
                observations = result.getString(6);

                //Get some information
                locationName = getLocationInformation(Integer.parseInt(locationID));
                familyName = getFamilyName(Integer.parseInt(familyID));
                categoryName = getCategoryName(Integer.parseInt(categoryID));
                date = getDateInformation(Integer.parseInt(dateID));
                statusName = getStatusName(Integer.parseInt(statusID));

                //Map
                Map informationMap = new HashMap();
                informationMap.put("LocationName", locationName[0]);
                informationMap.put("DepartmentName", locationName[1]);
                informationMap.put("RoomName", locationName[2]);
                informationMap.put("Family", familyName);
                informationMap.put("Category", categoryName);
                informationMap.put("DateDay", date[0]);
                informationMap.put("DateMonth", date[1]);
                informationMap.put("DateYear", date[2]);
                informationMap.put("Status", statusName);
                informationMap.put("Code", code);
                informationMap.put("Observations", observations);
                informationProduct.add(informationMap);
            }
        }catch (SQLException Ex){
            System.out.println("Error in the method 'getProductInformationByCode' - SQL error!!!");
            System.out.println(Ex);
        }
        return true;
    }

    /**
     * MOCKUP -> PROCURAR PRODUTO
     * This method is used when the user try select a group of specific products to show the information about these but when the user just inserts the fields 'Family' and 'Category'
     * We will reuse the method called "getSpecificEquipment" to get the information about these products in this method
     * This method will select just in the table "Equipments"!!!
     * @param informationProduct (An ArrayList to add a map with all information about all group of specific products from this family and category)
     * @param family (Family's name of the product)
     * @param category (Category's name of the product)
     * @return (return FALSE if occur some error) (return TRUE if get all information correctly)
     */
    public static boolean getProductInformationByFamilyCategory(ArrayList informationProduct, String family, String category){
        java.sql.Statement stmt;
        ArrayList<String> codesToSelect = new ArrayList<String>();
        String code = "";
        //It is necessary get the ID's about the fields family and category
        int familyID = getSpecificFamilyID(family);
        int categoryID = getSpecificCategoryID(category);

        //Now it necessary get all codes of these specific products of this group, because it is necessary to call after the method "getSpecificEquipment" to get all the information about each one
        try{
            String query_getCode = "SELECT code FROM Equipments WHERE id_family LIKE '"+familyID+"' AND id_category LIKE '"+categoryID+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_getCode);
            while (result.next()){
                code = result.getString(1);
                codesToSelect.add(code);
            }
            //Get the information about each product and add to the ArrayList with the method 'getSpecificEquipment'
            for (int x=0; x<codesToSelect.size(); x++){
                code = codesToSelect.get(x);
                getSpecificEquipment(informationProduct, familyID, categoryID, code, "Equipments");
            }
        }catch (SQLException Ex){
            System.out.println("Error getting the product's information in the method 'getProductInformation' - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * This method is to get all information about a specific product (from the "Equipments" table or the "Historic" table)
     * @param specific_equipment (ArrayList to add a map with all information about this specific product)
     * @param family_id (The ID of his family)
     * @param category_id (The ID of his category)
     * @param code (The code that identify the specific product)
     * @param tableName (The name of the table where this method will try find this information about this product (table "Equipments" or "historic")
     * @return (return FALSE if occur some error) (return TRUE if create correctly the map and add it to the arrayList in parameter)
     */
    public static boolean getSpecificEquipment(ArrayList specific_equipment, int family_id, int category_id, String code, String tableName){
        java.sql.Statement stmt;
        int locationID = 0, dateID = 0, statusID = 0;
        String family = "", category = "", status = "";
        String []locationInformation = new String[3];
        String []dateInformation = new String[3];
        String observations = "";

        try{
            String query_equipment = "SELECT id_location, id_date, id_status, observations FROM "+tableName+" WHERE id_family LIKE '"+family_id+"' AND id_category LIKE '"+category_id+"' AND code LIKE '"+code+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_equipment);
            //As exist just one record of each equipment at table equipments, this query just will return one row, if this equipment exist of course, otherwise return nothing
            if (result.next()){
                //Save some information about last query that it will be needed after
                locationID = Integer.parseInt(result.getString(1));
                dateID = Integer.parseInt(result.getString(2));
                //Status information
                statusID = Integer.parseInt(result.getString(3));
                //Observations information
                observations = result.getString(4);

                //Get the information about the location of this equipment
                locationInformation = getLocationInformation(locationID); //(0 - name, 1 - department, 2 - room)
                //Get the information about the family
                family = getFamilyName(family_id);
                //Get the information about the category
                category = getCategoryName(category_id);
                //Get the information about the date
                dateInformation = getDateInformation(dateID); //(0 - day, 1  - month, 2 - year)
                //Get the information about the status
                status = getStatusName(statusID);

                //Now, we have all information to build a map with all information about this specific equipment
                Map equipmentInformation = new HashMap();
                equipmentInformation.put("LocationName", locationInformation[0]);
                equipmentInformation.put("LocationDepartment", locationInformation[1]);
                equipmentInformation.put("LocationRoom", locationInformation[2]);
                equipmentInformation.put("Family", family);
                equipmentInformation.put("Category", category);
                equipmentInformation.put("DateDay", dateInformation[0]);
                equipmentInformation.put("DateMonth", dateInformation[1]);
                equipmentInformation.put("DateYear", dateInformation[2]);
                equipmentInformation.put("Status", status);
                equipmentInformation.put("Code", code);
                equipmentInformation.put("observations", observations);
                //Add this map to the ArrayList
                specific_equipment.add(equipmentInformation);
            }
        }catch (SQLException Ex){
            System.out.println("Error getting a specif equipment - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * MOCKUP -> VER PRODUTOS ACTUAIS
     * This method is to get all information about all products on the table "Equipments"
     * @param allEquipmentsInformation (an ArrayList to add all maps with the information about each record (each product) from the table "Equipments)
     * @return (return FALSE if occur some error) (return TRUE if get all information about all records of this table correctly and add these information in the ArrayList in parameter)
     */
    public static boolean getAllEquipmentsInformation(ArrayList allEquipmentsInformation){
        java.sql.Statement stmt;
        String family, category, code;
        //First it needs select all familyID, categoryID and code to after it can be used through the method getSpecificEquipment
        try{
            String query = "SELECT id_family, id_category, code FROM Equipments ORDER BY id_family, id_category"; //order by family and after by category
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                family = result.getString(1);
                category = result.getString(2);
                code = result.getString(3);
                //Call the method getSpecificEquipment to get all information about one equipment and it will does this until has more equipments
                getSpecificEquipment(allEquipmentsInformation, Integer.parseInt(family), Integer.parseInt(category), code, "Equipments");
            }
        }catch (SQLException Ex){
            System.out.println("Error getting familyID, categoryID and code at getAllEquipmentsInformation method - SQL error");
            System.out.println(Ex);
            return false;
        }
        return true;
    }

    /**
     * MOCKUP -> VER HISTORICO
     * This method is to get all information about all products on the table "Historic"
     * @param allEquipmentsInformationHistoric (an ArrayList to add all maps with the information about each record (each product) from the table "Historic")
     * @return (return FALSE if occur some error) (return TRUE if get all information about all records of this table correctly and add these information in the ArrayList in parameter)
     */
    public static boolean getAllEquipmentsInformationHistoric(ArrayList allEquipmentsInformationHistoric){
        java.sql.Statement stmt;
        String family, category, code;
        //First it needs select all familyID, categoryID and code to after it can be used through the method getSpecificEquipment
        try{
            String query = "SELECT id_family, id_category, code FROM Historic ORDER BY id_family, id_category"; //order by family and after by category
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query);
            while (result.next()){
                family = result.getString(1);
                category = result.getString(2);
                code = result.getString(3);
                //Call the method getSpecificEquipment to get all information about one equipment and it will does this until has more equipments
                getSpecificEquipment(allEquipmentsInformationHistoric, Integer.parseInt(family), Integer.parseInt(category), code, "Historic");
            }
        }catch (SQLException Ex){
            System.out.println("Error getting familyID, categoryID and code at getAllEquipmentsInformation method - SQL error");
            System.out.println(Ex);
        }
        return true;
    }

    /**
     * MOCKUP -> UPDATE PRODUTO
     * This method is to UPDATE the fields about a specific product
     * The fields to update are different that "" and the fields that are equal at "", we do not will update this fields (update fields != "") (Not update fields == "")
     * @param family (Family's name)
     * @param category (Category's name)
     * @param status (Status of the product)
     * @param code (Code that identify the product - But this is the new code to update if it will happen, if not this field is empty (""))
     * @param day (Day of the registration)
     * @param month (Month of the registration)
     * @param year (Year of the registration)
     * @param location (Location where is the product)
     * @param department (Department where is the product)
     * @param room (Room where is the product)
     * @param last_code (The actually code of this product, before update this code if this will happen.. this field always need to has something.. the code of this product in the moment)
     * @param observations (This field is to save some observations about the specific product, some like fix something on the product (hardware or software).. things like that)
     * @return (return FALSE if occur some error or if this specific product does not exist (do not find the product's code)) (return TRUE if update the fields that need to be updated correctly)
     */
    public static boolean updateProductFields(String family, String category, String status, String code, String day, String month, String year, String location, String department, String room, String last_code, String observations){
        java.sql.Statement stmt;
        //First, lets go verify if this product is on the Equipments table and get his id
        String product_id = "", familyID = "", categoryID = "", statusID = "", dateID = "", locationID = "";
        String location_id_save = "", family_id_save = "", category_id_save = "", date_id_save = "", status_id_save = "", code_save = "", observations_save = "";
        try{
            //Get the ID just with the CODE
            String query_getProductID = "SELECT equipments_id, id_location, id_family, id_category, id_date, id_status, code, observations FROM Equipments WHERE code LIKE '"+last_code+"'";
            stmt = con.createStatement();
            ResultSet result = stmt.executeQuery(query_getProductID);
            //if return some row it is because this equipment exist
            if (result.next()){
                product_id = result.getString(1);

                //Before we start to update the fields of this product, we need first save this state in the Historic table
                location_id_save = result.getString(2);
                family_id_save = result.getString(3);
                category_id_save = result.getString(4);
                date_id_save = result.getString(5);
                status_id_save = result.getString(6);
                code_save = result.getString(7);
                observations_save = result.getString(8);

                String query_save_state = "INSERT INTO Historic (id_location, id_family, id_category, id_date, id_status, code, observations) VALUES (?,?,?,?,?,?,?)";
                PreparedStatement preparedStatement = con.prepareStatement(query_save_state);
                preparedStatement.setString(1, location_id_save);
                preparedStatement.setString(2, family_id_save);
                preparedStatement.setString(3, category_id_save);
                preparedStatement.setString(4, date_id_save);
                preparedStatement.setString(5, status_id_save);
                preparedStatement.setString(6, code_save);
                preparedStatement.setString(7, observations_save);
                preparedStatement.execute();
                //The state of this product is already save before start the update to the fields
            }
            //if do not return anything, it because do not exist any equipment with this code, so we need stop here
            else{
                return false;
            }
        }catch (SQLException Ex){
            System.out.println("Error getting the equipment's ID to update fields - SQL error!!!");
            System.out.println(Ex);
            return false;
        }
        //Start the update of the fields!
        //The user can update all fields or just some of these, so we need verify which fields we need to update and which fields not
        //Verify the field family - if field family is equal "" we do not need to update, so if not we need to update this field
        if(!(family.equals(""))){
            familyID = "";
            try {
                String query_getNewFamilyID = "SELECT family_id FROM Family WHERE name LIKE '"+family+"' AND actually_used LIKE '1'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query_getNewFamilyID);
                if (result.next()){
                    familyID = result.getString(1);
                }
                String query_updateFamilyField = "UPDATE Equipments SET id_family=? WHERE equipments_id=?";
                PreparedStatement preparedStatement = con.prepareStatement(query_updateFamilyField);
                preparedStatement.setString(1, familyID);
                preparedStatement.setString(2, product_id);
                preparedStatement.execute();
            }catch (SQLException Ex){
                System.out.println("Error updating family's ID field - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Verify now the field category.. the rules are the same
        if (!(category.equals(""))){
            categoryID = "";
            try{
                String query_getNewCategoryID = "SELECT category_id FROM Category WHERE name LIKE '"+category+"' AND actually_used LIKE '1'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query_getNewCategoryID);
                if (result.next()){
                    categoryID = result.getString(1);
                }
                String query_updateCategoryField = "UPDATE Equipments SET id_category=? WHERE equipments_id=?";
                PreparedStatement preparedStatement = con.prepareStatement(query_updateCategoryField);
                preparedStatement.setString(1, categoryID);
                preparedStatement.setString(2, product_id);
                preparedStatement.execute();
            }catch (SQLException Ex){
                System.out.println("Error updating category's ID field - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Verify now the field status.. the rules are the same
        if (!(status.equals(""))){
            statusID = "";
            try{
                String query_getNewStatusID = "SELECT status_id FROM Status WHERE name LIKE '"+status+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query_getNewStatusID);
                if (result.next()){
                    statusID = result.getString(1);
                }
                String query_updateStatusField = "UPDATE Equipments SET id_status=? WHERE equipments_id=?";
                PreparedStatement preparedStatement = con.prepareStatement(query_updateStatusField);
                preparedStatement.setString(1, statusID);
                preparedStatement.setString(2, product_id);
                preparedStatement.execute();
            }catch (SQLException Ex){
                System.out.println("Error updating status's ID field - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Verify now the field code.. the rules are the same (if the user change the code, the program do not verify if this code already exist or not)
        if(!(code.equals(""))){
            try{
                String query_updateCodeField = "UPDATE Equipments SET code=? WHERE equipments_id=?";
                PreparedStatement preparedStatement = con.prepareStatement(query_updateCodeField);
                preparedStatement.setString(1, code);
                preparedStatement.setString(2, product_id);
                preparedStatement.execute();
            }catch (SQLException Ex){
                System.out.println("Error updating the field code - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Verify now the field day.. the rules are the same (here, the user can chose any day or month or year, so we need verify if this data already exist, and if not we need create a new date)
        //The user need to fill the 3 fields (day, month, year) to update the date
        if(!(day.equals("")) && !(month.equals("")) && !(year.equals(""))){
            //Get the date's  ID (if this date already exist just return the ID, but if not, this method create this new date and after return the ID)
            dateID = Integer.toString(addNewDate(Integer.parseInt(day), Integer.parseInt(month), Integer.parseInt(year)));
            try{
                String query_updateDateFields = "UPDATE Equipments SET id_date=? WHERE equipments_id=?";
                PreparedStatement preparedStatement = con.prepareStatement(query_updateDateFields);
                preparedStatement.setString(1, dateID);
                preparedStatement.setString(2, product_id);
                preparedStatement.execute();
            }catch (SQLException Ex){
                System.out.println("Error updating day field - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Verify now the field location.. the rules are the same
        //The user need to fill the 3 fields (location, department, room) to update this fields about the location
        if(!(location.equals("")) && !(department.equals("")) && !(room.equals(""))) {
            locationID = "";
            try {
                String query_getNewLocationID = "SELECT location_id FROM Location WHERE name LIKE '"+location+"' AND department LIKE '"+department+"' AND room LIKE '"+room+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query_getNewLocationID);
                if (result.next()){
                    locationID = result.getString(1);
                }
                String query_updateLocation = "UPDATE Equipments SET id_location=? WHERE equipments_id=?";
                PreparedStatement preparedStatement = con.prepareStatement(query_updateLocation);
                preparedStatement.setString(1, locationID);
                preparedStatement.setString(2, product_id);
                preparedStatement.execute();
            }catch (SQLException Ex){
                System.out.println("Error updating the fields location, department, room - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Verify now the field observations.. the rules are the same
        if (!(observations.equals(""))){
            try{
                String query_updateObservations = "UPDATE Equipments SET observations=? WHERE equipments_id=?";
                PreparedStatement preparedStatement = con.prepareStatement(query_updateObservations);
                preparedStatement.setString(1, observations);
                preparedStatement.setString(2, product_id);
                preparedStatement.execute();
            }catch (SQLException Ex){
                System.out.println("Error updating the field observations - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        return true;
    }

    /**
     * MOCKUP -> VER PRODUTOS ACTUAIS
     * This method is to get some information about 1 or more products.. the user can chose select the products to show only by the field family or
     * only by the field category or only by the field status or by the 3 fields about the localization (localization, department, room) or between them!!
     * The fields that need get information are not empty like this "", they need have something write!!! We just just will get the information
     * about the fields that are not empty ("")!!!
     * This method just will select on the table "Equipments"!!!
     * @param informationFields (ArrayList to add maps with all information about the respective fields)
     * @param familyField (Family's name)
     * @param categoryField (Category's name)
     * @param statusField (Status's name)
     * @param locationField (Locations's name where is the product)
     * @param departmentField (Department's name where is the product)
     * @param roomField (Room's name where is the product)
     * @return (return FALSE when occur some error) (return TRUE when everything run correctly)
     */
    public static boolean getSomeFields(ArrayList informationFields, String familyField, String categoryField, String statusField, String locationField, String departmentField, String roomField){
        java.sql.Statement stmt;
        String query = "";
        //Just select by the field 'family'
        if ((familyField!="") && (categoryField.equals("")) && (statusField.equals("")) && (locationField.equals("")) && (departmentField.equals("")) && (roomField.equals(""))){
            String familyID, categoryID, statusID, locationID, dateID;
            String categoryName, statusName, code, observations;
            String []locationName;
            String []date;
            try {
                //First it is necessary get the family's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                //Now select all products with this family's ID
                query = "SELECT id_location, id_category, id_date, id_status, code, observations FROM Equipments WHERE id_family LIKE '"+familyID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information about each record and add to the arrayList
                while (result.next()){
                    locationID = result.getString(1);
                    categoryID = result.getString(2);
                    dateID = result.getString(3);
                    statusID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get information
                    locationName = getLocationInformation(Integer.parseInt(locationID)); //name, department, room
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID)); //day, month, year
                    statusName = getStatusName(Integer.parseInt(statusID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("LocationDepartment", locationName[1]);
                    informationMap.put("LocationRoom", locationName[2]);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            } catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 1 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the field 'category'
        else if ((familyField.equals("")) && (categoryField!="") && (statusField.equals("")) && (locationField.equals("")) && (departmentField.equals("")) && (roomField.equals(""))){
            String familyID, categoryID, statusID, locationID, dateID;
            String familyName, statusName, code, observations;
            String [] locationName;
            String [] date;
            try{
                //First select the Category ID
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                //Now select all records with this category's ID
                query = "SELECT id_location, if_family, id_date, id_status, code, observations FROM Equipments WHERE id_category LIKE '"+categoryID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information about each record and add to the arrayList
                while (result.next()){
                    locationID = result.getString(1);
                    familyID = result.getString(2);
                    dateID = result.getString(3);
                    statusID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("LocationDepartment", locationName[1]);
                    informationMap.put("LocationRoom", locationName[2]);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 2 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the field 'status'
        else if ((familyField.equals("")) && (categoryField.equals("")) && (statusField!="") && (locationField.equals("")) && (departmentField.equals("")) && (roomField.equals(""))){
            String familyID, categoryID, statusID, dateID, locationID;
            String familyName, categoryName, code, observations;
            String []locationName;
            String []date;
            try{
                //Get status's ID
                statusID = Integer.toString(getStatusID(statusField));
                //Get all records with this status's ID
                query = "SELECT id_location, id_family, id_category, id_date, code, observations FROM Equipments WHERE id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    familyID = result.getString(2);
                    categoryID = result.getString(3);
                    dateID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("LocationDepartment", locationName[1]);
                    informationMap.put("locationRoom", locationName[2]);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 3 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'family' and 'category'
        else if ((familyField!="") && (categoryField!="") && (statusField.equals("")) && (locationField.equals("")) && (departmentField.equals("")) && (roomField.equals(""))){
            String familyID, categoryID, statusID, dateID, locationID;
            String statusName, code, observations;
            String [] locationName;
            String []date;
            try{
                //Get the family's ID and the category's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                //Now select all records with these fields
                query = "SELECT id_location, id_status, id_date, code, observations FROM Equipments WHERE id_family LIKE '"+familyID+"' AND id_category LIKE '"+categoryID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    statusID = result.getString(2);
                    dateID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    statusName = getStatusName(Integer.parseInt(statusID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("LocationDepartment", locationName[1]);
                    informationMap.put("LocationRoom", locationName[2]);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 4 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the field 'family' and 'status'
        else if ((familyField!="") && categoryField.equals("") && (statusField!="") && (locationField.equals("")) && (departmentField.equals("")) && (roomField.equals(""))){
            String familyID, categoryID, statusID, dateID, locationID;
            String categoryName, code, observations;
            String []locationName;
            String []date;
            try{
                //First get the IDs of the fields 'family' and 'status'
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                statusID = Integer.toString(getStatusID(statusField));
                //Now select all records with these fields
                query = "SELECT id_location, id_category, id_date, code, observations FROM Equipments WHERE id_family LIKE '"+familyID+"' AND id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    categoryID = result.getString(2);
                    dateID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("LocationDepartment", locationName[1]);
                    informationMap.put("LocationRoom", locationName[2]);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 5 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'category' and 'status'
        else if ((familyField.equals("")) && (categoryField!="") && (statusField!="") && (locationField.equals("")) && (departmentField.equals("")) && (roomField.equals(""))){
            String familyID, categoryID, statusID, locationID, dateID;
            String familyName, code, observations;
            String []locationName;
            String []date;
            try{
                //Get the IDs of the fields 'category' and 'status'
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                statusID = Integer.toString(getStatusID(statusField));
                query = "SELECT id_location, id_family, id_date, code, observations FROM Equipments WHERE id_category LIKE '"+categoryID+"' AND id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    familyID = result.getString(2);
                    dateID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("LocationDepartment", locationName[1]);
                    informationMap.put("locationRoom", locationName[2]);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 6 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields about the location ('localization', 'department', 'room')
        else if ((familyField.equals("")) && (categoryField.equals("")) && (statusField.equals("")) && (locationField!="") && (departmentField!="") && (roomField!="")){
            String familyID, categoryID, dateID, locationID, statusID;
            String familyName, categoryName, statusName, code, observations;
            String date[];
            try{
                //Get the location's ID
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                //Select all records with this locations
                query = "SELECT id_family, id_category, id_date, id_status, code, observations FROM Equipments WHERE id_location LIKE '"+locationID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    familyID = result.getString(1);
                    categoryID = result.getString(2);
                    dateID = result.getString(3);
                    statusID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get some information
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("LocationDepartment", departmentField);
                    informationMap.put("LocationRoom", roomField);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 7 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'family' and ('localization', 'department', 'room')
        else if ((familyField!="") && (categoryField.equals("")) && (statusField.equals("")) && (locationField!="") && (departmentField!="") && (roomField!="")){
            String familyID, categoryID, statusID, dateID, locationID;
            String categoryName, statusName, code, observations;
            String []date;
            try{
                //Get the family's ID and the location's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                //Get all records with these fields
                query = "SELECT id_category, id_date, id_status, code, observations FROM Equipments WHERE id_family LIKE '"+familyID+"' AND id_location LIKE '"+locationID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    categoryID = result.getString(1);
                    dateID = result.getString(2);
                    statusID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get some information
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("LocationDepartment", departmentField);
                    informationMap.put("LocationRoom", roomField);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 8 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'category' and ('localization', 'department', 'room')
        else if ((familyField.equals("")) && (categoryField!="") && (statusField.equals("")) && (locationField!="") && (departmentField!="") && (roomField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String familyName, statusName, code, observations;
            String []date;
            try{
                //Get the category's ID and the location's ID
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                //Get all records with these fields
                query = "SELECT id_family, id_date, id_status, code, observations FROM Equipments WHERE id_location LIKE '"+locationID+"' AND id_category LIKE '"+categoryID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    familyID = result.getString(1);
                    dateID = result.getString(2);
                    statusID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get some information
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("LocationDepartment", departmentField);
                    informationMap.put("LocationRoom", roomField);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 9 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'status' and ('localization', 'department', 'room')
        else if ((familyField.equals("")) && (categoryField.equals("")) && (statusField!="") && (locationField!="") && (departmentField!="") && (roomField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String familyName, categoryName, code, observations;
            String []date;
            try{
                //Get the status's ID and the location's ID
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                statusID = Integer.toString(getStatusID(statusField));
                query = "SELECT id_family, id_category, id_date, code, observations FROM Equipments WHERE id_location LIKE '"+locationID+"' AND id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    familyID = result.getString(1);
                    categoryID = result.getString(2);
                    dateID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get some information
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("LocationDepartment", departmentField);
                    informationMap.put("LocationRoom", roomField);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 10 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'family', 'status' and ('localization', 'department', 'room')
        else if ((familyField!="") && (categoryField.equals("")) && (statusField!="") && (locationField!="") && (categoryField!="") && (roomField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String categoryName, code, observations;
            String []date;
            try{
                //Get the family's ID, status's ID and the location's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                statusID = Integer.toString(getStatusID(statusField));
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                query = "SELECT id_category, id_date, code, observations FROM Equipments WHERE id_location LIKE '"+locationID+"' AND id_family LIKE '"+familyID+"' AND id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    categoryID = result.getString(1);
                    dateID = result.getString(2);
                    code = result.getString(3);
                    observations = result.getString(4);

                    //Get some information
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("LocationDepartment", departmentField);
                    informationMap.put("LocationRoom", roomField);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 11 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'family', 'category' and ('localization', 'department', 'room')
        else if ((familyField!="") && (categoryField!="") && (statusField.equals("")) && (locationField!="") && (departmentField!="") && (roomField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String statusName, code, observations;
            String []date;
            try{
                //Get the family's ID, category's ID and location's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                query = "SELECT id_date, id_status, code, observations FROM Equipments WHERE id_location LIKE '"+locationID+"' AND id_family LIKE '"+familyID+"' AND id_category LIKE '"+categoryID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    dateID = result.getString(1);
                    statusID = result.getString(2);
                    code = result.getString(3);
                    observations = result.getString(4);

                    //Get some information
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("LocationDepartment", departmentField);
                    informationMap.put("LocationRoom", roomField);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 12 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Just select by the fields 'category', 'status' and ('localization', 'department', 'room')
        else if ((familyField.equals("")) && (categoryField!="") && (statusField!="") && (locationField!="") && (departmentField!="") && (roomField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String familyName, code, observations;
            String []date;
            try{
                //Get the category's ID, status's ID and the location's ID
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                statusID = Integer.toString(getStatusID(statusField));
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                query = "SELECT id_family, id_date, code, observations FROM Equipments WHERE id_location LIKE '"+locationID+"' AND id_category LIKE '"+categoryID+"' AND id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    familyID = result.getString(1);
                    dateID = result.getString(2);
                    code = result.getString(3);
                    observations = result.getString(4);

                    //Get some information
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("LocationDepartment", departmentField);
                    informationMap.put("LocationRoom", roomField);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeFields'.. 13 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        return false;
    }

    /**
     * MOCKUP -> VER HISTORICO
     * This method is to get part of the historic related with some fields, such as family, category, status, code and the localization
     * The fields that are to select the records by them, they have something write inside, the others do not have anything inside ("")
     * This method just will select on the table 'Historic'
     * @param informationFields (An arrayList to add all maps with the correspondent information correctly)
     * @param familyField (Family's name)
     * @param categoryField (Category's name)
     * @param statusField (Status's name)
     * @param codeField (The code that identify a specific product)
     * @param locationField (Location's name)
     * @param departmentField (Department's name)
     * @param roomField (Room's name)
     * @return (return FALSE if occur some error) (return TRUE if everything run correctly)
     */
    public static boolean getSomeHistoricByFields(ArrayList informationFields, String familyField, String categoryField, String statusField, String codeField, String locationField, String departmentField, String roomField){
        java.sql.Statement stmt;
        String query = "";
        //Select just by the field 'family'
        if ((familyField!="") && (categoryField.equals("")) && (statusField.equals(""))){
            String familyID, categoryID, statusID, dateID, locationID;
            String categoryName, statusName, code, observations;
            String []date;
            String []locationName;
            try{
                //Get the family's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                //Get all records with this field
                query = "SELECT id_location, id_category, id_date, id_status, code, observations FROM Historic WHERE id_family LIKE '"+familyID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    categoryID = result.getString(2);
                    dateID = result.getString(3);
                    statusID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("DepartmentName", locationName[1]);
                    informationMap.put("RoomName", locationName[2]);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 1 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Select just by the field 'category'
        else if ((familyField.equals("")) && (categoryField!="") && (statusField.equals(""))){
            String familyID, categoryID, dateID, statusID, locationID;
            String familyName, statusName, code, observations;
            String []date;
            String []locationName;
            try{
                //Get the category's ID
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                //Get all records with this field
                query = "SELECT id_location, id_family, id_date, id_status, code, observations FROM Historic WHERE id_category LIKE '"+categoryID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    familyID = result.getString(2);
                    dateID = result.getString(3);
                    statusID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("DepartmentName", locationName[1]);
                    informationMap.put("RoomName", locationName[2]);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 2 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Select just by the field 'status'
        else if ((familyField.equals("")) && (categoryField.equals("")) && (statusField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String familyName, categoryName, code, observations;
            String []date;
            String []locationName;
            try{
                //Get the status's ID
                statusID = Integer.toString(getStatusID(statusField));
                query = "SELECT id_location, id_family, id_category, id_date, code, observations FROM Historic WHERE id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    familyID = result.getString(2);
                    categoryID = result.getString(3);
                    dateID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("DepartmentName", locationName[1]);
                    informationMap.put("RoomName", locationName[2]);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 3 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Select just by the field 'code'
        else if (codeField!=""){
            String familyID, categoryID, dateID, statusID, locationID, equipmentID;
            String familyName, categoryName, statusName, observations;
            String []date;
            String []locationName;
            try{
                //Get the equipment's ID that have this code
                equipmentID = Integer.toString(getEquipmentID(codeField, "Historic"));
                query = "SELECT id_location, id_family, id_category, id_date, id_status, observations FROM Historic WHERE equipments_id LIKE '"+equipmentID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    familyID = result.getString(2);
                    categoryID = result.getString(3);
                    dateID = result.getString(4);
                    statusID = result.getString(5);
                    observations = result.getString(6);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("DepartmentName", locationName[1]);
                    informationMap.put("RoomName", locationName[2]);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", codeField);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 4 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //select just by the location fields ('localization', 'department', 'room')
        else if ((locationField!="") && (departmentField!="") && (roomField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String familyName, categoryName, statusName, code, observations;
            String []date;
            try{
                //Get the location's ID
                locationID = Integer.toString(getSpecificLocationID(locationField, departmentField, roomField));
                //Get all records with this location
                query = "SELECT id_family, id_category, id_date, id_status, code, observations FROM Historic WHERE id_location LIKE '"+locationID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    familyID = result.getString(1);
                    categoryID = result.getString(2);
                    dateID = result.getString(3);
                    statusID = result.getString(4);
                    code = result.getString(5);
                    observations = result.getString(6);

                    //Get some information
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationField);
                    informationMap.put("DepartmentName", departmentField);
                    informationMap.put("RoomName", roomField);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 5 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Select by the fields 'family' and 'category'
        else if ((familyField!="") && (categoryField!="") && (statusField.equals(""))){
            String familyID, categoryID, dateID, statusID, locationID;
            String statusName, code, observations;
            String []date;
            String []locationName;
            try{
                //Get the family's ID and the category's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                categoryID = Integer.toString(getSpecificCategoryID(categoryField));
                //Get all records with these fields
                query = "SELECT id_location, id_date, id_status, code, observations FROM Historic WHERE id_family LIKE '"+familyID+"' AND id_category LIKE '"+categoryID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    dateID = result.getString(2);
                    statusID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    date = getDateInformation(Integer.parseInt(dateID));
                    statusName = getStatusName(Integer.parseInt(statusID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("DepartmentName", locationName[1]);
                    informationMap.put("RoomName", locationName[2]);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusName);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 6 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Select by the fields 'family' and 'status'
        else if ((familyField!="") && (categoryField.equals("")) && (statusField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String categoryName, code, observations;
            String []date;
            String []locationName;
            try{
                //Get the family's ID and the status's ID
                familyID = Integer.toString(getSpecificFamilyID(familyField));
                statusID = Integer.toString(getStatusID(statusField));
                //Get all records with these fields
                query = "SELECT id_location, id_category, id_date, code, observations FROM Historic WHERE id_family LIKE '"+familyID+"' AND id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    categoryID = result.getString(2);
                    dateID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    categoryName = getCategoryName(Integer.parseInt(categoryID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("DepartmentName", locationName[1]);
                    informationMap.put("RoomName", locationName[2]);
                    informationMap.put("Family", familyField);
                    informationMap.put("Category", categoryName);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 7 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        //Select by the fields 'category' and 'status'
        else if ((familyField.equals("")) && (categoryField!="") && (statusField!="")){
            String familyID, categoryID, dateID, statusID, locationID;
            String familyName, code, observations;
            String []date;
            String []locationName;
            try{
                //Get the category's ID and the status's ID
                categoryID = Integer.toString(getSpecificCategoryID(statusField));
                statusID = Integer.toString(getStatusID(statusField));
                //Get all records with these fields
                query = "SELECT id_location, id_family, id_date, code, observations FROM Historic WHERE id_category LIKE '"+categoryID+"' AND id_status LIKE '"+statusID+"'";
                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query);
                //Get all information
                while (result.next()){
                    locationID = result.getString(1);
                    familyID = result.getString(2);
                    dateID = result.getString(3);
                    code = result.getString(4);
                    observations = result.getString(5);

                    //Get some information
                    locationName = getLocationInformation(Integer.parseInt(locationID));
                    familyName = getFamilyName(Integer.parseInt(familyID));
                    date = getDateInformation(Integer.parseInt(dateID));

                    //Map
                    Map informationMap = new HashMap();
                    informationMap.put("LocationName", locationName[0]);
                    informationMap.put("DepartmentName", locationName[1]);
                    informationMap.put("RoomName", locationName[2]);
                    informationMap.put("Family", familyName);
                    informationMap.put("Category", categoryField);
                    informationMap.put("DateDay", date[0]);
                    informationMap.put("DateMonth", date[1]);
                    informationMap.put("DateYear", date[2]);
                    informationMap.put("Status", statusField);
                    informationMap.put("Code", code);
                    informationMap.put("Observations", observations);
                    informationFields.add(informationMap);
                }
            }catch (SQLException Ex){
                System.out.println("Error in the method 'getSomeHistoricFields'.. 8 IF - SQL error!!!");
                System.out.println(Ex);
                return false;
            }
        }
        return true;
    }

    //FIXME: VERIFY THE MOCKUPS TO SEE IF IT IS NECESSARY CREATE MORE METHODS HERE IN THE MODEL TO EXCHANGE INFORMATION BETWEEN THE USER AND THE DATABASE.. I THINK NO, BUT.. !!!

}

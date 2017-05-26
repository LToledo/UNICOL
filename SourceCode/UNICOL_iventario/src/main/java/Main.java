import Graphics.GUI;
import Model.ModelFunctions;

/**
 * Created by davinci on 8/5/16.
 */
public class Main {

    public static boolean debug = true;

    public static void main(String args[]) {
        //Connect to the database
        Model.ModelFunctions.databaseConnetion();

        //Initialize the GUI
        GUI grafica = new GUI();

        //Testing only!!!
        if (debug) {
            //boolean insert_location = Model.ModelFunctions.addnewLocation("central", "informatica", "escritorio");
            //boolean delete_location = Model.ModelFunctions.deleteLocation("central", "informatica", "escritorio");
            //boolean insert_family = Model.ModelFunctions.addNewFamily("Cable");
            //boolean delete_family = Model.ModelFunctions.deleteFamily("PC");
            //boolean insert_category = Model.ModelFunctions.addNewCategory("Desktop");
            //boolean delete_category = Model.ModelFunctions.deleteCategory("Laptop");
            //int id_date = Model.ModelFunctions.addNewDate(30,3,1993);
            //System.out.println("ID - " + id_date);
            //boolean verify_record = Model.ModelFunctions.verifyNewRecord("central", "informatica", "escritorio", "PC", "Laptop", 30, 3, 1993, "DaVinci", "holidays");
            //boolean insert_equipment = Model.ModelFunctions.addNewEquipement("central", "informatica", "escritorio", "PC", "Laptop", 30, 3, 1993, "DaVinci", "holidays");
        }
    }


}

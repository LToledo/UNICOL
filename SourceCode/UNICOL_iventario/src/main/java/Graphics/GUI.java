package Graphics;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by davinci on 8/5/16.
 */
public class GUI {

    //JLabels variables
    private static JLabel x;

    //JTextFields variables
    private static JTextField y;

    //JList variables
    private static JList familyListBox, categoryListBox;


    private static JScrollPane scrollPane;


    public GUI() {
        JFrame frame = new JFrame("UNICOL - Iventário");
        frame.setSize(2000, 1500);  //resolução da frame
        JPanel painel = new JPanel();
        painel.setLayout(null);

        //Call panels
        mainPanel(painel);

        frame.add(painel); //add painel to the frame
        frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE); //stop everything when stops the frame
        frame.setResizable(false); //keeping the same windows's size
        frame.setVisible(true); //keep everything visible
    }

    //family type painel
    public static void mainPanel(JPanel painel){

        //DATA REFERENT AT FAMILY
        //Items do add to the list
        ArrayList<String> family = new ArrayList<String>();
        //Get the information about all family names from database
        Model.ModelFunctions.allFamily(family);
        String []listDataFamily = new String[family.size()];
        //Initialize the static array with the information contained on the ArrayList
        populateArrayFromList(listDataFamily, family);

        //Create the listBox Control
        familyListBox = new JList(listDataFamily);
        scrollPane = new JScrollPane(familyListBox);
        scrollPane.setBounds(30, 90, 230, 20);
        //dataList.ensureIndexIsVisible(dataList.getSelectedIndex());
        painel.add(scrollPane);

        //Get the value selected
        familyListBox.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                String test = (String) familyListBox.getSelectedValue();
                System.out.println(test);
                // PanelTest.setValue(test);
            }
        });


        //DATA REFERENT AT CATEGORY
        //Items do add to the list
        ArrayList<String> category = new ArrayList<String>();
        //Get the information about all family names from database
        Model.ModelFunctions.allCategories(category);
        String []listDataCategory = new String[category.size()];
        //Initialize the static array with the information contained on the ArrayList
        populateArrayFromList(listDataCategory, category);

        //Create the listBox Control
        categoryListBox = new JList(listDataCategory);
        scrollPane = new JScrollPane(categoryListBox);
        scrollPane.setBounds(300, 90, 230, 20);
        //dataList.ensureIndexIsVisible(dataList.getSelectedIndex());
        painel.add(scrollPane);

        //Get the value selected
        categoryListBox.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                String test = (String) categoryListBox.getSelectedValue();
                System.out.println(test);
                // PanelTest.setValue(test);
            }
        });
    }

    //This method helps us to populate an static array with the information about a dynamic array (ArrayList)
    public static  <T> void populateArrayFromList(T[] arr, ArrayList<T> arrayList)
    {
        //System.out.println("Array size " + arr.length);
        //System.out.println("ArrayList size " + arrayList.size());
        for (int i = 0; i < arrayList.size(); i++)
        {
            arr[i] = arrayList.get(i);
        }
    }
}

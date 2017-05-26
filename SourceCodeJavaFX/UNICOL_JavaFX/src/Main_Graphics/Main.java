package Main_Graphics;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    /**
     * ISTO AINDA NÃO ESTÁ NADA DEFINITIVO.. ESTAVA SÓ A TESTAR E A APRENDER CERTAS COISAS QUE NÃO SEI!!!
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        Button btn = new Button();
        btn.setText("Inserir/Apagar Produto");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //PRINT THIS TO THE TERMINAL JUST TO TEST
                System.out.println("BOTAAAAA Inserir/Apagar Produto");
            }
        });
        //THIS IS JUST TO PUT THE BUTTON NICE
        DropShadow shadow = new DropShadow();
        //Adding the shadow when the mouse cursor is on
        btn.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn.setEffect(shadow);
                    }
                });
        //Removing the shadow when the mouse cursor is off
        btn.addEventHandler(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn.setEffect(null);
                    }
                });

        Button btn2 = new Button();
        btn2.setText("Ver Histórico");
        btn2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //PRINT THIS TO THE TERMINAL JUST TO TEST
                System.out.println("BOTAAAA Ver Histórico");
            }
        });
        //THIS IS JUST TO PUT THE BUTTON NICE
        //Adding the shadow when the mouse cursor is on
        btn2.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn2.setEffect(shadow);
                    }
                });
        //Removing the shadow when the mouse cursor is off
        btn2.addEventHandler(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn2.setEffect(null);
                    }
                });

        Button btn3 = new Button();
        btn3.setText("Procurar Produto");
        btn3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //PRINT THIS TO THE TERMINAL JUST TO TEST
                System.out.println("BOTAAAA Procurar Produto");
            }
        });
        //THIS IS JUST TO PUT THE BUTTON NICE
        //Adding the shadow when the mouse cursor is on
        btn3.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn3.setEffect(shadow);
                    }
                });
        //Removing the shadow when the mouse cursor is off
        btn3.addEventHandler(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn3.setEffect(null);
                    }
                });

        Button btn4 = new Button();
        btn4.setText("Ver Produtos Actuais");
        btn4.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //PRINT THIS TO THE TERMINAL JUST TO TEST
                System.out.println("BOTAAAA Ver Produtos Actuais");
            }
        });
        //THIS IS JUST TO PUT THE BUTTON NICE
        //Adding the shadow when the mouse cursor is on
        btn4.addEventHandler(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn4.setEffect(shadow);
                    }
                });
        //Removing the shadow when the mouse cursor is off
        btn4.addEventHandler(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override public void handle(MouseEvent e) {
                        btn4.setEffect(null);
                    }
                });

        Pane root = new Pane();
        btn.setLayoutX(250);
        btn.setLayoutY(220);
        btn2.setLayoutX(500);
        btn2.setLayoutY(220);
        btn3.setLayoutX(750);
        btn3.setLayoutY(220);
        btn4.setLayoutX(1000);
        btn4.setLayoutY(220);
        root.getChildren().add(btn);
        root.getChildren().add(btn2);
        root.getChildren().add(btn3);
        root.getChildren().add(btn4);
        BorderPane border = new BorderPane();
        border.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        border.setCenter(root);
        Scene scene = new Scene(border);
        primaryStage.setScene(scene);
        primaryStage.setTitle("UNICOL - Inventário Informático V1.1");
        primaryStage.show();

    }


    public static void main(String[] args) {
        //Connect to the database
        Model.ModelFunctions.databaseConnection();

        launch(args);
    }
}

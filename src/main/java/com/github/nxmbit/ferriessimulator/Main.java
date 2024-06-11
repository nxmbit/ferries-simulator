package com.github.nxmbit.ferriessimulator;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("simulation.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Symulacja Promów na Rzece");
        primaryStage.setScene(new Scene(root));
        //primaryStage.setScene(new Scene(root, 1280, 720));
        //primaryStage.setResizable(false); // Make the window non-resizable

        primaryStage.setOnCloseRequest(e -> {
            Controller controller = loader.getController();
            controller.stopSimulation();

            System.exit(0);
        });

        primaryStage.show();

        Controller controller = loader.getController();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

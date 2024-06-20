package com.github.nxmbit.ferriessimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("simulation.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Ferries Simulation");
        primaryStage.setScene(new Scene(root));

        primaryStage.setResizable(false); // Make the window non-resizable

        primaryStage.setOnCloseRequest(e -> {
            Controller controller = loader.getController();
            controller.stopSimulation();

            System.exit(0);
        });

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.github.nxmbit.ferriessimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FerriesSimulator extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("simulation.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        // Pass the HostServices to the controller
        controller.setHostServices(getHostServices());

        Image icon = new Image(getClass().getResourceAsStream("/com/github/nxmbit/ferriessimulator/icons/icon_rounded256.png"));
        primaryStage.getIcons().add(icon);

        primaryStage.setTitle("Ferries Simulation");
        primaryStage.setScene(new Scene(root));

        primaryStage.setResizable(false); // Make the window non-resizable

        primaryStage.setOnCloseRequest(e -> {
            controller.stopSimulation();
            System.exit(0);
        });

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

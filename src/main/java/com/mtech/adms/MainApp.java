package com.mtech.adms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/mtech/adms/fxml/LoginView.fxml")
        );

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(
                getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm()
        );

        primaryStage.setTitle("MTech Asset & Deployment Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
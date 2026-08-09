package com.mtech.adms.controller;

import com.mtech.adms.exception.AuthenticationException;
import com.mtech.adms.model.User;
import com.mtech.adms.service.UserService;
import com.mtech.adms.util.AppLogger;
import com.mtech.adms.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final UserService userService = new UserService();

    @FXML
    private void handleLogin() {
        hideError();

        String username = usernameField.getText();
        String password = passwordField.getText();

        loginButton.setDisable(true);

        try {
            User user = userService.login(username, password);
            SessionManager.login(user);
            navigateToMainView();

        } catch (AuthenticationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AppLogger.error("Unexpected error during login", e);
            showError("An unexpected error occurred. Please try again.");
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void navigateToMainView() {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/mtech/adms/fxml/MainView.fxml")
            );

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm()
            );
            stage.setScene(scene);

        } catch (IOException e) {
            AppLogger.error("Failed to load MainView after login", e);
            showError("Login succeeded, but failed to load the main screen.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
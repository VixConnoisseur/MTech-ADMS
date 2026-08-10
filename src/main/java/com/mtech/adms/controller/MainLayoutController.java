package com.mtech.adms.controller;

import com.mtech.adms.model.User;
import com.mtech.adms.util.AppLogger;
import com.mtech.adms.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label pageTitleLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label userInitialsLabel;

    @FXML private Button navDashboard;
    @FXML private Button navEmployees;
    @FXML private Button navProjects;
    @FXML private Button navSites;
    @FXML private Button navAssets;
    @FXML private Button navDeployments;
    @FXML private Button navReports;
    @FXML private Button navSettings;

    private List<Button> navButtons;

    @FXML
    private void initialize() {
        navButtons = List.of(navDashboard, navEmployees, navProjects, navSites,
                navAssets, navDeployments, navReports, navSettings);
        displayCurrentUser();
        showDashboard();
    }

    private void displayCurrentUser() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullName());
            userRoleLabel.setText(currentUser.getRoleName());
            userInitialsLabel.setText(getInitials(currentUser.getFullName()));
        }
    }

    private String getInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    /** Highlights the clicked nav button and clears the others. */
    private void setActiveNav(Button active) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-button-active");
        }
        active.getStyleClass().add("nav-button-active");
    }

    @FXML
    private void showDashboard() {
        setActiveNav(navDashboard);
        loadContent("/com/mtech/adms/fxml/DashboardView.fxml", "Dashboard");
    }

    @FXML
    private void showEmployees() {
        setActiveNav(navEmployees);
        pageTitleLabel.setText("Employees");
        AppLogger.info("Employees module not yet implemented (Phase 9).");
    }

    @FXML
    private void showProjects() {
        setActiveNav(navProjects);
        pageTitleLabel.setText("Projects");
        AppLogger.info("Projects module not yet implemented (Phase 10).");
    }

    @FXML
    private void showSites() {
        setActiveNav(navSites);
        pageTitleLabel.setText("Sites");
        AppLogger.info("Sites module not yet implemented (Phase 11).");
    }

    @FXML
    private void showAssets() {
        setActiveNav(navAssets);
        pageTitleLabel.setText("Assets");
        AppLogger.info("Assets module not yet implemented (Phase 13).");
    }

    @FXML
    private void showDeployments() {
        setActiveNav(navDeployments);
        pageTitleLabel.setText("Deployments");
        AppLogger.info("Deployments module not yet implemented (Phase 14).");
    }

    @FXML
    private void showReports() {
        setActiveNav(navReports);
        pageTitleLabel.setText("Reports");
        AppLogger.info("Reports module not yet implemented (Phase 20).");
    }

    @FXML
    private void showSettings() {
        setActiveNav(navSettings);
        pageTitleLabel.setText("Settings");
        AppLogger.info("Settings module not yet implemented.");
    }

    @FXML
    private void handleLogout() {
        AppLogger.info("User logged out: " +
                (SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUsername() : "unknown"));
        SessionManager.logout();

        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/mtech/adms/fxml/LoginView.fxml")
            );
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm()
            );
            stage.setScene(scene);

        } catch (IOException e) {
            AppLogger.error("Failed to return to login screen", e);
        }
    }

    private void loadContent(String fxmlPath, String title) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(content);
            pageTitleLabel.setText(title);

        } catch (IOException e) {
            AppLogger.error("Failed to load content: " + fxmlPath, e);
        }
    }
}
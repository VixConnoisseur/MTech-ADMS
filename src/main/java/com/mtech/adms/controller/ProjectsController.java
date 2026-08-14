package com.mtech.adms.controller;

import com.mtech.adms.model.Project;
import com.mtech.adms.service.ProjectService;
import com.mtech.adms.util.AppLogger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProjectsController {

    @FXML private TextField searchField;
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> colCode;
    @FXML private TableColumn<Project, String> colName;
    @FXML private TableColumn<Project, String> colClient;
    @FXML private TableColumn<Project, String> colStartDate;
    @FXML private TableColumn<Project, String> colStatus;
    @FXML private TableColumn<Project, Void> colActions;

    private final ProjectService projectService = new ProjectService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    private void initialize() {
        setupColumns();
        projectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadProjects(null);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadProjects(newVal));
    }

    private void setupColumns() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("projectCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("clientName"));

        colStartDate.setCellValueFactory(cellData -> {
            var date = cellData.getValue().getStartDate();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DATE_FMT) : "—"
            );
        });

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(formatStatus(status));
                badge.getStyleClass().add(badgeClassForStatus(status));
                setGraphic(badge);
                setText(null);
            }
        });

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button toggleBtn = new Button();
            private final HBox box = new HBox(6, editBtn, toggleBtn);

            {
                editBtn.getStyleClass().add("table-action-button");
                toggleBtn.getStyleClass().add("table-action-button");

                editBtn.setOnAction(e -> openForm(getTableRow().getItem()));
                toggleBtn.setOnAction(e -> handleToggleActive(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Project project = getTableRow().getItem();
                toggleBtn.setText(project.isActive() ? "Deactivate" : "Activate");
                toggleBtn.getStyleClass().removeAll("table-action-danger", "table-action-success");
                toggleBtn.getStyleClass().add(project.isActive() ? "table-action-danger" : "table-action-success");
                setGraphic(box);
            }
        });
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "ON_HOLD" -> "On Hold";
            case "PLANNING" -> "Planning";
            case "ACTIVE" -> "Active";
            case "COMPLETED" -> "Completed";
            case "CANCELLED" -> "Cancelled";
            default -> status;
        };
    }

    private String badgeClassForStatus(String status) {
        return switch (status) {
            case "ACTIVE" -> "badge-active";
            case "COMPLETED" -> "badge-info";
            case "CANCELLED" -> "badge-inactive";
            case "ON_HOLD" -> "badge-warning";
            default -> "badge-inactive";
        };
    }

    private void loadProjects(String keyword) {
        try {
            var projects = projectService.search(keyword);
            projectsTable.setItems(FXCollections.observableArrayList(projects));
        } catch (Exception e) {
            AppLogger.error("Failed to load projects", e);
        }
    }

    @FXML
    private void handleAddProject() {
        openForm(null);
    }

    private void openForm(Project project) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mtech/adms/fxml/ProjectFormDialog.fxml"));
            Parent root = loader.load();

            ProjectFormController controller = loader.getController();
            controller.setProject(project);
            controller.setOnSaved(() -> loadProjects(searchField.getText()));

            Stage dialogStage = new Stage();
            dialogStage.setTitle(project == null ? "Add Project" : "Edit Project");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(projectsTable.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            AppLogger.error("Failed to open project form", e);
        }
    }

    private void handleToggleActive(Project project) {
        if (project == null) {
            return;
        }
        boolean newStatus = !project.isActive();
        String action = newStatus ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm " + action);
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + action + " " + project.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                projectService.setActive(project.getId(), newStatus);
                loadProjects(searchField.getText());
            } catch (Exception e) {
                AppLogger.error("Failed to toggle project active status", e);
            }
        }
    }
}
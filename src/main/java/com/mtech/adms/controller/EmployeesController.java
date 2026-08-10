package com.mtech.adms.controller;

import com.mtech.adms.model.Employee;
import com.mtech.adms.service.EmployeeService;
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

import java.util.Optional;

public class EmployeesController {

    @FXML private TextField searchField;
    @FXML private TableView<Employee> employeesTable;
    @FXML private TableColumn<Employee, String> colCode;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, String> colContact;
    @FXML private TableColumn<Employee, String> colStatus;
    @FXML private TableColumn<Employee, Void> colActions;

    private final EmployeeService employeeService = new EmployeeService();

    @FXML
    private void initialize() {
        setupColumns();
        loadEmployees(null);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadEmployees(newVal));
    }

    private void setupColumns() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("employeeCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactNo"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusLabel"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("Active".equals(status) ? "badge-active" : "badge-inactive");
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
                Employee employee = getTableRow().getItem();
                toggleBtn.setText(employee.isActive() ? "Deactivate" : "Activate");
                toggleBtn.getStyleClass().removeAll("table-action-danger", "table-action-success");
                toggleBtn.getStyleClass().add(employee.isActive() ? "table-action-danger" : "table-action-success");
                setGraphic(box);
            }
        });
    }

    private void loadEmployees(String keyword) {
        try {
            var employees = employeeService.search(keyword);
            employeesTable.setItems(FXCollections.observableArrayList(employees));
        } catch (Exception e) {
            AppLogger.error("Failed to load employees", e);
        }
    }

    @FXML
    private void handleAddEmployee() {
        openForm(null);
    }

    private void openForm(Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mtech/adms/fxml/EmployeeFormDialog.fxml"));
            Parent root = loader.load();

            EmployeeFormController controller = loader.getController();
            controller.setEmployee(employee);
            controller.setOnSaved(() -> loadEmployees(searchField.getText()));

            Stage dialogStage = new Stage();
            dialogStage.setTitle(employee == null ? "Add Employee" : "Edit Employee");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(employeesTable.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            AppLogger.error("Failed to open employee form", e);
        }
    }

    private void handleToggleActive(Employee employee) {
        if (employee == null) {
            return;
        }
        boolean newStatus = !employee.isActive();
        String action = newStatus ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm " + action);
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + action + " " + employee.getFullName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                employeeService.setActive(employee.getId(), newStatus);
                loadEmployees(searchField.getText());
            } catch (Exception e) {
                AppLogger.error("Failed to toggle employee active status", e);
            }
        }
    }
}
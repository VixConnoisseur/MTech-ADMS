package com.mtech.adms.controller;

import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Employee;
import com.mtech.adms.service.EmployeeService;
import com.mtech.adms.util.AppLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmployeeFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField positionField;
    @FXML private TextField contactField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final EmployeeService employeeService = new EmployeeService();
    private Employee editingEmployee;
    private Runnable onSaved;

    public void setEmployee(Employee employee) {
        this.editingEmployee = employee;

        if (employee != null) {
            formTitleLabel.setText("Edit Employee");
            fullNameField.setText(employee.getFullName());
            positionField.setText(employee.getPosition());
            contactField.setText(employee.getContactNo());
        } else {
            formTitleLabel.setText("Add Employee");
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        hideError();

        Employee employee = (editingEmployee != null) ? editingEmployee : new Employee();
        employee.setFullName(fullNameField.getText());
        employee.setPosition(positionField.getText());
        employee.setContactNo(contactField.getText());

        try {
            if (editingEmployee == null) {
                employeeService.create(employee);
            } else {
                employeeService.update(employee);
            }

            if (onSaved != null) {
                onSaved.run();
            }
            closeDialog();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AppLogger.error("Failed to save employee", e);
            showError("An unexpected error occurred while saving.");
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
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
package com.mtech.adms.controller;

import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.AssetCategory;
import com.mtech.adms.service.AssetCategoryService;
import com.mtech.adms.util.AppLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AssetCategoryFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final AssetCategoryService categoryService = new AssetCategoryService();
    private AssetCategory editingCategory;
    private Runnable onSaved;

    public void setCategory(AssetCategory category) {
        this.editingCategory = category;

        if (category != null) {
            formTitleLabel.setText("Edit Category");
            nameField.setText(category.getName());
            descriptionField.setText(category.getDescription());
        } else {
            formTitleLabel.setText("Add Category");
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        hideError();

        AssetCategory category = (editingCategory != null) ? editingCategory : new AssetCategory();
        category.setName(nameField.getText());
        category.setDescription(descriptionField.getText());

        try {
            if (editingCategory == null) {
                categoryService.create(category);
            } else {
                categoryService.update(category);
            }

            if (onSaved != null) {
                onSaved.run();
            }
            closeDialog();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AppLogger.error("Failed to save asset category", e);
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
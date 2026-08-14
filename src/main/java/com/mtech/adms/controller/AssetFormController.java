package com.mtech.adms.controller;

import com.mtech.adms.dao.AssetCategoryDao;
import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Asset;
import com.mtech.adms.model.AssetCategory;
import com.mtech.adms.service.AssetService;
import com.mtech.adms.util.AppLogger;
import com.mtech.adms.util.Constants;
import com.mtech.adms.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class AssetFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField toolNameField;
    @FXML private TextField serialNumberField;
    @FXML private ComboBox<AssetCategory> categoryComboBox;
    @FXML private ComboBox<String> conditionComboBox;
    @FXML private DatePicker purchaseDatePicker;
    @FXML private TextField purchaseCostField;
    @FXML private TextArea notesField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final AssetService assetService = new AssetService();
    private final AssetCategoryDao categoryDao = new AssetCategoryDao();
    private Asset editingAsset;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(categoryDao.findAllActive()));

        conditionComboBox.setItems(FXCollections.observableArrayList(
                Constants.Condition.GOOD, Constants.Condition.FAIR, Constants.Condition.POOR
        ));
        conditionComboBox.getSelectionModel().select(Constants.Condition.GOOD);
    }

    public void setAsset(Asset asset) {
        this.editingAsset = asset;

        if (asset != null) {
            formTitleLabel.setText("Edit Asset — " + asset.getAssetId());
            toolNameField.setText(asset.getToolName());
            serialNumberField.setText(asset.getSerialNumber());
            conditionComboBox.getSelectionModel().select(asset.getConditionStatus());
            purchaseDatePicker.setValue(asset.getPurchaseDate());
            if (asset.getPurchaseCost() != null) {
                purchaseCostField.setText(asset.getPurchaseCost().toString());
            }
            notesField.setText(asset.getNotes());

            for (AssetCategory cat : categoryComboBox.getItems()) {
                if (cat.getId().equals(asset.getCategoryId())) {
                    categoryComboBox.getSelectionModel().select(cat);
                    break;
                }
            }
        } else {
            formTitleLabel.setText("Add Asset");
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        hideError();

        Asset asset = (editingAsset != null) ? editingAsset : new Asset();
        asset.setToolName(toolNameField.getText());
        asset.setSerialNumber(serialNumberField.getText());
        asset.setConditionStatus(conditionComboBox.getSelectionModel().getSelectedItem());
        asset.setPurchaseDate(purchaseDatePicker.getValue());
        asset.setNotes(notesField.getText());

        AssetCategory selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            asset.setCategoryId(selectedCategory.getId());
        }

        String costText = purchaseCostField.getText();
        if (costText != null && !costText.isBlank()) {
            try {
                asset.setPurchaseCost(new BigDecimal(costText.trim()));
            } catch (NumberFormatException e) {
                showError("Purchase cost must be a valid number.");
                return;
            }
        }

        try {
            if (editingAsset == null) {
                int userId = SessionManager.getCurrentUser().getId();
                assetService.create(asset, userId);
            } else {
                assetService.update(asset);
            }

            if (onSaved != null) {
                onSaved.run();
            }
            closeDialog();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AppLogger.error("Failed to save asset", e);
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
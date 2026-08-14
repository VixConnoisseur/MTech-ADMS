package com.mtech.adms.controller;

import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Asset;
import com.mtech.adms.service.AssetService;
import com.mtech.adms.util.AppLogger;
import com.mtech.adms.util.Constants;
import com.mtech.adms.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class AssetStatusController {

    @FXML private Label titleLabel;
    @FXML private Label currentStatusLabel;
    @FXML private ComboBox<String> newStatusCombo;
    @FXML private TextArea notesField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final AssetService assetService = new AssetService();
    private Asset asset;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        newStatusCombo.setItems(FXCollections.observableArrayList(
                Constants.AssetStatus.AVAILABLE,
                Constants.AssetStatus.DAMAGED,
                Constants.AssetStatus.UNDER_REPAIR,
                Constants.AssetStatus.MISSING,
                Constants.AssetStatus.LOST,
                Constants.AssetStatus.RETIRED
        ));
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        titleLabel.setText("Change Status — " + asset.getAssetId());
        currentStatusLabel.setText("Current status: " + asset.getStatus());
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        hideError();

        String newStatus = newStatusCombo.getSelectionModel().getSelectedItem();
        if (newStatus == null) {
            showError("Please select a new status.");
            return;
        }
        if (newStatus.equals(asset.getStatus())) {
            showError("Asset is already in this status.");
            return;
        }

        try {
            int userId = SessionManager.getCurrentUser().getId();
            assetService.changeStatus(asset.getId(), newStatus, notesField.getText(), userId);

            if (onSaved != null) {
                onSaved.run();
            }
            closeDialog();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AppLogger.error("Failed to change asset status", e);
            showError("An unexpected error occurred while changing status.");
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
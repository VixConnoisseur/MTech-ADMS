package com.mtech.adms.controller;

import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Site;
import com.mtech.adms.service.SiteService;
import com.mtech.adms.util.AppLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SiteFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField siteNameField;
    @FXML private TextField addressField;
    @FXML private TextField cityField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final SiteService siteService = new SiteService();
    private Site editingSite;
    private Runnable onSaved;

    public void setSite(Site site) {
        this.editingSite = site;

        if (site != null) {
            formTitleLabel.setText("Edit Site");
            siteNameField.setText(site.getSiteName());
            addressField.setText(site.getAddress());
            cityField.setText(site.getCity());
        } else {
            formTitleLabel.setText("Add Site");
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        hideError();

        Site site = (editingSite != null) ? editingSite : new Site();
        site.setSiteName(siteNameField.getText());
        site.setAddress(addressField.getText());
        site.setCity(cityField.getText());

        try {
            if (editingSite == null) {
                siteService.create(site);
            } else {
                siteService.update(site);
            }

            if (onSaved != null) {
                onSaved.run();
            }
            closeDialog();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AppLogger.error("Failed to save site", e);
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
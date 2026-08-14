package com.mtech.adms.controller;

import com.mtech.adms.dao.SiteDao;
import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Project;
import com.mtech.adms.model.Site;
import com.mtech.adms.service.ProjectService;
import com.mtech.adms.util.AppLogger;
import com.mtech.adms.util.Constants;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class ProjectFormController {

    @FXML private Label formTitleLabel;
    @FXML private TextField nameField;
    @FXML private TextField clientField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ListView<Site> sitesListView;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private final ProjectService projectService = new ProjectService();
    private final SiteDao siteDao = new SiteDao();
    private Project editingProject;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList(
                Constants.ProjectStatus.PLANNING,
                Constants.ProjectStatus.ACTIVE,
                Constants.ProjectStatus.ON_HOLD,
                Constants.ProjectStatus.COMPLETED,
                Constants.ProjectStatus.CANCELLED
        ));
        statusComboBox.getSelectionModel().select(Constants.ProjectStatus.PLANNING);

        sitesListView.setItems(FXCollections.observableArrayList(siteDao.findAllActive()));
        sitesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setProject(Project project) {
        this.editingProject = project;

        if (project != null) {
            formTitleLabel.setText("Edit Project");
            nameField.setText(project.getName());
            clientField.setText(project.getClientName());
            startDatePicker.setValue(project.getStartDate());
            endDatePicker.setValue(project.getEndDate());
            statusComboBox.getSelectionModel().select(project.getStatus());

            if (project.getSiteIds() != null) {
                MultipleSelectionModel<Site> selectionModel = sitesListView.getSelectionModel();
                for (Site site : sitesListView.getItems()) {
                    if (project.getSiteIds().contains(site.getId())) {
                        selectionModel.select(site);
                    }
                }
            }
        } else {
            formTitleLabel.setText("Add Project");
        }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void handleSave() {
        hideError();

        Project project = (editingProject != null) ? editingProject : new Project();
        project.setName(nameField.getText());
        project.setClientName(clientField.getText());
        project.setStartDate(startDatePicker.getValue());
        project.setEndDate(endDatePicker.getValue());
        project.setStatus(statusComboBox.getSelectionModel().getSelectedItem());

        List<Integer> selectedSiteIds = sitesListView.getSelectionModel().getSelectedItems()
                .stream().map(Site::getId).toList();
        project.setSiteIds(selectedSiteIds);

        try {
            if (editingProject == null) {
                projectService.create(project);
            } else {
                projectService.update(project);
            }

            if (onSaved != null) {
                onSaved.run();
            }
            closeDialog();

        } catch (ValidationException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            AppLogger.error("Failed to save project", e);
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
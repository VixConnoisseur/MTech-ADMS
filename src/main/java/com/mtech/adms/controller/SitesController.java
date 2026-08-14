package com.mtech.adms.controller;

import com.mtech.adms.model.Site;
import com.mtech.adms.service.SiteService;
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

public class SitesController {

    @FXML private TextField searchField;
    @FXML private TableView<Site> sitesTable;
    @FXML private TableColumn<Site, String> colSiteName;
    @FXML private TableColumn<Site, String> colAddress;
    @FXML private TableColumn<Site, String> colCity;
    @FXML private TableColumn<Site, String> colStatus;
    @FXML private TableColumn<Site, Void> colActions;

    private final SiteService siteService = new SiteService();

    @FXML
    private void initialize() {
        sitesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadSites(null);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadSites(newVal));
    }

    private void setupColumns() {
        colSiteName.setCellValueFactory(new PropertyValueFactory<>("siteName"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));

        colStatus.setCellValueFactory(cellData -> {
            String label = cellData.getValue().isActive() ? "Active" : "Inactive";
            return new javafx.beans.property.SimpleStringProperty(label);
        });
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
                Site site = getTableRow().getItem();
                toggleBtn.setText(site.isActive() ? "Deactivate" : "Activate");
                toggleBtn.getStyleClass().removeAll("table-action-danger", "table-action-success");
                toggleBtn.getStyleClass().add(site.isActive() ? "table-action-danger" : "table-action-success");
                setGraphic(box);
            }
        });
    }

    private void loadSites(String keyword) {
        try {
            var sites = siteService.search(keyword);
            sitesTable.setItems(FXCollections.observableArrayList(sites));
        } catch (Exception e) {
            AppLogger.error("Failed to load sites", e);
        }
    }

    @FXML
    private void handleAddSite() {
        openForm(null);
    }

    private void openForm(Site site) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mtech/adms/fxml/SiteFormDialog.fxml"));
            Parent root = loader.load();

            SiteFormController controller = loader.getController();
            controller.setSite(site);
            controller.setOnSaved(() -> loadSites(searchField.getText()));

            Stage dialogStage = new Stage();
            dialogStage.setTitle(site == null ? "Add Site" : "Edit Site");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(sitesTable.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            AppLogger.error("Failed to open site form", e);
        }
    }

    private void handleToggleActive(Site site) {
        if (site == null) {
            return;
        }
        boolean newStatus = !site.isActive();
        String action = newStatus ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm " + action);
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + action + " " + site.getSiteName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                siteService.setActive(site.getId(), newStatus);
                loadSites(searchField.getText());
            } catch (Exception e) {
                AppLogger.error("Failed to toggle site active status", e);
            }
        }
    }
}
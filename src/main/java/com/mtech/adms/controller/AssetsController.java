package com.mtech.adms.controller;

import com.mtech.adms.model.Asset;
import com.mtech.adms.service.AssetService;
import com.mtech.adms.util.AppLogger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AssetsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TableView<Asset> assetsTable;
    @FXML private TableColumn<Asset, String> colAssetId;
    @FXML private TableColumn<Asset, String> colToolName;
    @FXML private TableColumn<Asset, String> colCategory;
    @FXML private TableColumn<Asset, String> colCondition;
    @FXML private TableColumn<Asset, String> colStatus;
    @FXML private TableColumn<Asset, String> colLocation;
    @FXML private TableColumn<Asset, Void> colActions;

    private final AssetService assetService = new AssetService();

    @FXML
    private void initialize() {
        assetsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupStatusFilter();
        setupColumns();
        loadAssets();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadAssets());
        statusFilterCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> loadAssets());
    }

    private void setupStatusFilter() {
        statusFilterCombo.setItems(FXCollections.observableArrayList(
                "ALL", "AVAILABLE", "DEPLOYED", "RETURNED", "DAMAGED",
                "UNDER_REPAIR", "MISSING", "LOST", "RETIRED"
        ));
        statusFilterCombo.getSelectionModel().select("ALL");
    }

    private void setupColumns() {
        colAssetId.setCellValueFactory(new PropertyValueFactory<>("assetId"));
        colToolName.setCellValueFactory(new PropertyValueFactory<>("toolName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("conditionStatus"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("currentLocation"));

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
            private final Button statusBtn = new Button("Change Status");
            private final HBox box = new HBox(6, editBtn, statusBtn);

            {
                editBtn.getStyleClass().add("table-action-button");
                statusBtn.getStyleClass().add("table-action-button");

                editBtn.setOnAction(e -> openEditForm(getTableRow().getItem()));
                statusBtn.setOnAction(e -> openStatusDialog(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic((empty || getTableRow().getItem() == null) ? null : box);
            }
        });
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "UNDER_REPAIR" -> "Under Repair";
            case "AVAILABLE" -> "Available";
            case "DEPLOYED" -> "Deployed";
            case "RETURNED" -> "Returned";
            case "DAMAGED" -> "Damaged";
            case "MISSING" -> "Missing";
            case "LOST" -> "Lost";
            case "RETIRED" -> "Retired";
            default -> status;
        };
    }

    private String badgeClassForStatus(String status) {
        return switch (status) {
            case "AVAILABLE" -> "badge-active";
            case "DEPLOYED" -> "badge-info";
            case "RETURNED" -> "badge-info";
            case "UNDER_REPAIR" -> "badge-warning";
            case "DAMAGED", "MISSING", "LOST" -> "badge-danger";
            case "RETIRED" -> "badge-inactive";
            default -> "badge-inactive";
        };
    }

    private void loadAssets() {
        try {
            String keyword = searchField.getText();
            String status = statusFilterCombo.getSelectionModel().getSelectedItem();
            var assets = assetService.searchByStatus(keyword, status);
            assetsTable.setItems(FXCollections.observableArrayList(assets));
        } catch (Exception e) {
            AppLogger.error("Failed to load assets", e);
        }
    }

    @FXML
    private void handleAddAsset() {
        openEditForm(null);
    }

    private void openEditForm(Asset asset) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mtech/adms/fxml/AssetFormDialog.fxml"));
            Parent root = loader.load();

            AssetFormController controller = loader.getController();
            controller.setAsset(asset);
            controller.setOnSaved(this::loadAssets);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(asset == null ? "Add Asset" : "Edit Asset");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(assetsTable.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            AppLogger.error("Failed to open asset form", e);
        }
    }

    private void openStatusDialog(Asset asset) {
        if (asset == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mtech/adms/fxml/AssetStatusDialog.fxml"));
            Parent root = loader.load();

            AssetStatusController controller = loader.getController();
            controller.setAsset(asset);
            controller.setOnSaved(this::loadAssets);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Status — " + asset.getAssetId());
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(assetsTable.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            AppLogger.error("Failed to open status change dialog", e);
        }
    }
}
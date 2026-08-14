package com.mtech.adms.controller;

import com.mtech.adms.model.AssetCategory;
import com.mtech.adms.service.AssetCategoryService;
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

public class AssetCategoriesController {

    @FXML private TextField searchField;
    @FXML private TableView<AssetCategory> categoriesTable;
    @FXML private TableColumn<AssetCategory, String> colName;
    @FXML private TableColumn<AssetCategory, String> colDescription;
    @FXML private TableColumn<AssetCategory, String> colStatus;
    @FXML private TableColumn<AssetCategory, Void> colActions;

    private final AssetCategoryService categoryService = new AssetCategoryService();

    @FXML
    private void initialize() {
        categoriesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupColumns();
        loadCategories(null);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadCategories(newVal));
    }

    private void setupColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

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
                AssetCategory category = getTableRow().getItem();
                toggleBtn.setText(category.isActive() ? "Deactivate" : "Activate");
                toggleBtn.getStyleClass().removeAll("table-action-danger", "table-action-success");
                toggleBtn.getStyleClass().add(category.isActive() ? "table-action-danger" : "table-action-success");
                setGraphic(box);
            }
        });
    }

    private void loadCategories(String keyword) {
        try {
            var categories = categoryService.search(keyword);
            categoriesTable.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            AppLogger.error("Failed to load asset categories", e);
        }
    }

    @FXML
    private void handleAddCategory() {
        openForm(null);
    }

    private void openForm(AssetCategory category) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mtech/adms/fxml/AssetCategoryFormDialog.fxml"));
            Parent root = loader.load();

            AssetCategoryFormController controller = loader.getController();
            controller.setCategory(category);
            controller.setOnSaved(() -> loadCategories(searchField.getText()));

            Stage dialogStage = new Stage();
            dialogStage.setTitle(category == null ? "Add Category" : "Edit Category");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(categoriesTable.getScene().getWindow());

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/com/mtech/adms/css/application.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (Exception e) {
            AppLogger.error("Failed to open category form", e);
        }
    }

    private void handleToggleActive(AssetCategory category) {
        if (category == null) {
            return;
        }
        boolean newStatus = !category.isActive();
        String action = newStatus ? "activate" : "deactivate";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm " + action);
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + action + " " + category.getName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                categoryService.setActive(category.getId(), newStatus);
                loadCategories(searchField.getText());
            } catch (Exception e) {
                AppLogger.error("Failed to toggle category active status", e);
            }
        }
    }
}
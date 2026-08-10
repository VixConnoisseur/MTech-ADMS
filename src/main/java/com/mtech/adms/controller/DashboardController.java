package com.mtech.adms.controller;

import com.mtech.adms.model.DashboardStats;
import com.mtech.adms.model.RecentDeploymentSummary;
import com.mtech.adms.service.DashboardService;
import com.mtech.adms.util.AppLogger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label totalAssetsLabel;
    @FXML private Label availableAssetsLabel;
    @FXML private Label deployedAssetsLabel;
    @FXML private Label damagedAssetsLabel;
    @FXML private Label missingAssetsLabel;
    @FXML private Label underRepairAssetsLabel;
    @FXML private Label activeProjectsLabel;
    @FXML private Label activeSitesLabel;
    @FXML private Label activeEmployeesLabel;
    @FXML private Label overdueReturnsLabel;

    @FXML private TableView<RecentDeploymentSummary> recentDeploymentsTable;
    @FXML private TableColumn<RecentDeploymentSummary, String> colDeploymentCode;
    @FXML private TableColumn<RecentDeploymentSummary, String> colProject;
    @FXML private TableColumn<RecentDeploymentSummary, String> colSite;
    @FXML private TableColumn<RecentDeploymentSummary, String> colLeader;
    @FXML private TableColumn<RecentDeploymentSummary, String> colDeployDate;
    @FXML private TableColumn<RecentDeploymentSummary, String> colStatus;

    @FXML private VBox overdueAlertsBox;

    private final DashboardService dashboardService = new DashboardService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    @FXML
    private void initialize() {
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        colDeploymentCode.setCellValueFactory(new PropertyValueFactory<>("deploymentCode"));
        colProject.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        colSite.setCellValueFactory(new PropertyValueFactory<>("siteName"));
        colLeader.setCellValueFactory(new PropertyValueFactory<>("teamLeaderName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colDeployDate.setCellValueFactory(cellData -> {
            var date = cellData.getValue().getDeploymentDate();
            return new javafx.beans.property.SimpleStringProperty(
                    date != null ? date.format(DATE_FMT) : ""
            );
        });
    }

    private void loadDashboardData() {
        try {
            DashboardStats stats = dashboardService.getStats();
            populateStatCards(stats);

            List<RecentDeploymentSummary> recent = dashboardService.getRecentDeployments();
            recentDeploymentsTable.setItems(FXCollections.observableArrayList(recent));

            List<RecentDeploymentSummary> overdue = dashboardService.getOverdueDeployments();
            populateOverdueAlerts(overdue);

        } catch (Exception e) {
            AppLogger.error("Failed to load dashboard data", e);
        }
    }

    private void populateStatCards(DashboardStats stats) {
        totalAssetsLabel.setText(String.valueOf(stats.getTotalAssets()));
        availableAssetsLabel.setText(String.valueOf(stats.getAvailableAssets()));
        deployedAssetsLabel.setText(String.valueOf(stats.getDeployedAssets()));
        damagedAssetsLabel.setText(String.valueOf(stats.getDamagedAssets()));
        missingAssetsLabel.setText(String.valueOf(stats.getMissingAssets()));
        underRepairAssetsLabel.setText(String.valueOf(stats.getUnderRepairAssets()));
        activeProjectsLabel.setText(String.valueOf(stats.getActiveProjects()));
        activeSitesLabel.setText(String.valueOf(stats.getActiveSites()));
        activeEmployeesLabel.setText(String.valueOf(stats.getActiveEmployees()));
        overdueReturnsLabel.setText(String.valueOf(stats.getOverdueReturns()));
    }

    private void populateOverdueAlerts(List<RecentDeploymentSummary> overdue) {
        overdueAlertsBox.getChildren().clear();

        if (overdue.isEmpty()) {
            Label noneLabel = new Label("No overdue returns. Nice work.");
            noneLabel.getStyleClass().add("alert-empty");
            overdueAlertsBox.getChildren().add(noneLabel);
            return;
        }

        for (RecentDeploymentSummary dep : overdue) {
            VBox card = new VBox(2);
            card.getStyleClass().add("alert-item");
            card.setPadding(new Insets(10, 12, 10, 12));

            Label codeLabel = new Label(dep.getDeploymentCode() + " — " + dep.getProjectName());
            codeLabel.getStyleClass().add("alert-item-title");

            Label dueLabel = new Label("Due: " + dep.getExpectedReturnDate().format(DATE_FMT) +
                    "  •  " + dep.getSiteName());
            dueLabel.getStyleClass().add("alert-item-subtitle");

            card.getChildren().addAll(codeLabel, dueLabel);
            overdueAlertsBox.getChildren().add(card);
        }
    }
}
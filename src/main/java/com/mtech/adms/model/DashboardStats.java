package com.mtech.adms.model;

/**
 * Holds the aggregate counts shown on the Dashboard. Not tied to a
 * single database table - this is a computed summary object built
 * by DashboardDao from several tables at once.
 */
public class DashboardStats {

    private int totalAssets;
    private int availableAssets;
    private int deployedAssets;
    private int missingAssets;
    private int damagedAssets;
    private int underRepairAssets;
    private int activeProjects;
    private int activeSites;
    private int activeEmployees;
    private int overdueReturns;

    public int getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(int totalAssets) {
        this.totalAssets = totalAssets;
    }

    public int getAvailableAssets() {
        return availableAssets;
    }

    public void setAvailableAssets(int availableAssets) {
        this.availableAssets = availableAssets;
    }

    public int getDeployedAssets() {
        return deployedAssets;
    }

    public void setDeployedAssets(int deployedAssets) {
        this.deployedAssets = deployedAssets;
    }

    public int getMissingAssets() {
        return missingAssets;
    }

    public void setMissingAssets(int missingAssets) {
        this.missingAssets = missingAssets;
    }

    public int getDamagedAssets() {
        return damagedAssets;
    }

    public void setDamagedAssets(int damagedAssets) {
        this.damagedAssets = damagedAssets;
    }

    public int getUnderRepairAssets() {
        return underRepairAssets;
    }

    public void setUnderRepairAssets(int underRepairAssets) {
        this.underRepairAssets = underRepairAssets;
    }

    public int getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(int activeProjects) {
        this.activeProjects = activeProjects;
    }

    public int getActiveSites() {
        return activeSites;
    }

    public void setActiveSites(int activeSites) {
        this.activeSites = activeSites;
    }

    public int getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(int activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public int getOverdueReturns() {
        return overdueReturns;
    }

    public void setOverdueReturns(int overdueReturns) {
        this.overdueReturns = overdueReturns;
    }
}
package com.mtech.adms.model;

import java.time.LocalDate;

/**
 * A flattened, read-only view of one deployment for display in the
 * Dashboard's "Recent Deployments" and "Overdue Returns" tables.
 * Combines fields from deployments, projects, sites, and employees
 * via a JOIN - this is intentionally not the full Deployment model
 * (that comes in Phase 14) since the dashboard only needs a summary.
 */
public class RecentDeploymentSummary {

    private String deploymentCode;
    private String projectName;
    private String siteName;
    private String teamLeaderName;
    private LocalDate deploymentDate;
    private LocalDate expectedReturnDate;
    private String status;

    public String getDeploymentCode() {
        return deploymentCode;
    }

    public void setDeploymentCode(String deploymentCode) {
        this.deploymentCode = deploymentCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getTeamLeaderName() {
        return teamLeaderName;
    }

    public void setTeamLeaderName(String teamLeaderName) {
        this.teamLeaderName = teamLeaderName;
    }

    public LocalDate getDeploymentDate() {
        return deploymentDate;
    }

    public void setDeploymentDate(LocalDate deploymentDate) {
        this.deploymentDate = deploymentDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
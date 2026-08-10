package com.mtech.adms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Project {

    private Integer id;
    private String projectCode;
    private String name;
    private String clientName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private boolean active;
    private List<Integer> siteIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * IDs of sites linked to this project via project_sites.
     * Populated by the DAO on load; used by the form to pre-check
     * selected sites, and by the service to save the junction rows.
     */
    public List<Integer> getSiteIds() {
        return siteIds;
    }

    public void setSiteIds(List<Integer> siteIds) {
        this.siteIds = siteIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Project{id=" + id + ", code='" + projectCode + "', name='" + name + "'}";
    }
}
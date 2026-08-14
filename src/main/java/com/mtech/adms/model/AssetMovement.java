package com.mtech.adms.model;

import java.time.LocalDateTime;

/**
 * Represents one row in asset_movements - the permanent audit trail
 * of every status/location change for every asset.
 */
public class AssetMovement {

    private Integer id;
    private Integer assetId;
    private Integer deploymentId;
    private String fromStatus;
    private String toStatus;
    private String fromLocation;
    private String toLocation;
    private Integer movedBy;
    private String movedByName;
    private String notes;
    private LocalDateTime movedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAssetId() {
        return assetId;
    }

    public void setAssetId(Integer assetId) {
        this.assetId = assetId;
    }

    public Integer getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(Integer deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus) {
        this.fromStatus = fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public void setToStatus(String toStatus) {
        this.toStatus = toStatus;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    public Integer getMovedBy() {
        return movedBy;
    }

    public void setMovedBy(Integer movedBy) {
        this.movedBy = movedBy;
    }

    public String getMovedByName() {
        return movedByName;
    }

    public void setMovedByName(String movedByName) {
        this.movedByName = movedByName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getMovedAt() {
        return movedAt;
    }

    public void setMovedAt(LocalDateTime movedAt) {
        this.movedAt = movedAt;
    }
}
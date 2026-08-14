package com.mtech.adms.service;

import com.mtech.adms.dao.AssetDao;
import com.mtech.adms.dao.AssetMovementDao;
import com.mtech.adms.exception.RecordNotFoundException;
import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Asset;
import com.mtech.adms.model.AssetMovement;
import com.mtech.adms.util.Constants;

import java.util.List;
import java.util.Set;

public class AssetService {

    private final AssetDao assetDao;
    private final AssetMovementDao movementDao;

    private static final Set<String> MANUAL_STATUSES = Set.of(
            Constants.AssetStatus.AVAILABLE,
            Constants.AssetStatus.DAMAGED,
            Constants.AssetStatus.UNDER_REPAIR,
            Constants.AssetStatus.MISSING,
            Constants.AssetStatus.LOST,
            Constants.AssetStatus.RETIRED
    );

    public AssetService() {
        this.assetDao = new AssetDao();
        this.movementDao = new AssetMovementDao();
    }

    public List<Asset> getAll() {
        return assetDao.findAll();
    }

    public List<Asset> search(String keyword) {
        return assetDao.search(keyword);
    }

    public List<Asset> searchByStatus(String keyword, String status) {
        return assetDao.searchByStatus(keyword, status);
    }

    public Asset getById(int id) {
        return assetDao.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Asset not found: id " + id));
    }

    public List<AssetMovement> getMovementHistory(int assetId) {
        return movementDao.findByAssetId(assetId);
    }

    public Asset create(Asset asset, int createdByUserId) {
        validate(asset);

        asset.setAssetId(assetDao.getNextAssetId());
        asset.setStatus(Constants.AssetStatus.AVAILABLE);
        if (asset.getCurrentLocation() == null || asset.getCurrentLocation().isBlank()) {
            asset.setCurrentLocation(Constants.DEFAULT_WAREHOUSE_LOCATION);
        }
        if (asset.getConditionStatus() == null) {
            asset.setConditionStatus("GOOD");
        }

        return assetDao.insertWithInitialMovement(asset, createdByUserId);
    }

    public void update(Asset asset) {
        validate(asset);

        if (asset.getId() == null) {
            throw new ValidationException("Cannot update an asset without an ID.");
        }
        assetDao.update(asset);
    }

    /**
     * Changes an asset's status manually (not via deployment). Only
     * allows the statuses in MANUAL_STATUSES - DEPLOYED and RETURNED
     * are excluded since those only happen through the deployment
     * workflow (Phase 14), which will call AssetDao.changeStatus()
     * directly with its own logic.
     */
    public void changeStatus(int assetId, String newStatus, String notes, int movedByUserId) {
        if (!MANUAL_STATUSES.contains(newStatus)) {
            throw new ValidationException(
                    "Status '" + newStatus + "' cannot be set manually. " +
                            "DEPLOYED/RETURNED statuses are managed through the Deployment workflow.");
        }

        Asset asset = getById(assetId);
        String newLocation = Constants.AssetStatus.AVAILABLE.equals(newStatus)
                ? Constants.DEFAULT_WAREHOUSE_LOCATION
                : asset.getCurrentLocation();

        assetDao.changeStatus(assetId, newStatus, newLocation, movedByUserId, notes);
    }

    private void validate(Asset asset) {
        if (asset.getToolName() == null || asset.getToolName().isBlank()) {
            throw new ValidationException("Tool name is required.");
        }
        if (asset.getCategoryId() == null) {
            throw new ValidationException("Category is required.");
        }
    }
}
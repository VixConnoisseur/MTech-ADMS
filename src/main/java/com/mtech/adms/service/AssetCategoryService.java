package com.mtech.adms.service;

import com.mtech.adms.dao.AssetCategoryDao;
import com.mtech.adms.exception.RecordNotFoundException;
import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.AssetCategory;

import java.util.List;

public class AssetCategoryService {

    private final AssetCategoryDao categoryDao;

    public AssetCategoryService() {
        this.categoryDao = new AssetCategoryDao();
    }

    public List<AssetCategory> getAll() {
        return categoryDao.findAll();
    }

    public List<AssetCategory> getAllActive() {
        return categoryDao.findAllActive();
    }

    public List<AssetCategory> search(String keyword) {
        return categoryDao.search(keyword);
    }

    public AssetCategory getById(int id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Asset category not found: id " + id));
    }

    public AssetCategory create(AssetCategory category) {
        validate(category);
        category.setActive(true);
        return categoryDao.insert(category);
    }

    public void update(AssetCategory category) {
        validate(category);

        if (category.getId() == null) {
            throw new ValidationException("Cannot update a category without an ID.");
        }
        categoryDao.update(category);
    }

    public void setActive(int id, boolean active) {
        AssetCategory category = getById(id);
        category.setActive(active);
        categoryDao.update(category);
    }

    private void validate(AssetCategory category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new ValidationException("Category name is required.");
        }
        if (category.getName().trim().length() < 2) {
            throw new ValidationException("Category name must be at least 2 characters.");
        }
    }
}
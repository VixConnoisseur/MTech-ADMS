package com.mtech.adms.service;

import com.mtech.adms.dao.SiteDao;
import com.mtech.adms.exception.RecordNotFoundException;
import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Site;

import java.util.List;

public class SiteService {

    private final SiteDao siteDao;

    public SiteService() {
        this.siteDao = new SiteDao();
    }

    public List<Site> getAll() {
        return siteDao.findAll();
    }

    public List<Site> search(String keyword) {
        return siteDao.search(keyword);
    }

    public Site getById(int id) {
        return siteDao.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Site not found: id " + id));
    }

    public Site create(Site site) {
        validate(site);
        site.setActive(true);
        return siteDao.insert(site);
    }

    public void update(Site site) {
        validate(site);

        if (site.getId() == null) {
            throw new ValidationException("Cannot update a site without an ID.");
        }
        siteDao.update(site);
    }

    public void setActive(int id, boolean active) {
        Site site = getById(id);
        site.setActive(active);
        siteDao.update(site);
    }

    private void validate(Site site) {
        if (site.getSiteName() == null || site.getSiteName().isBlank()) {
            throw new ValidationException("Site name is required.");
        }
        if (site.getSiteName().trim().length() < 2) {
            throw new ValidationException("Site name must be at least 2 characters.");
        }
    }
}
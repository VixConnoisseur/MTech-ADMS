package com.mtech.adms.service;

import com.mtech.adms.dao.ProjectDao;
import com.mtech.adms.exception.RecordNotFoundException;
import com.mtech.adms.exception.ValidationException;
import com.mtech.adms.model.Project;
import com.mtech.adms.util.Constants;

import java.util.List;

public class ProjectService {

    private final ProjectDao projectDao;

    public ProjectService() {
        this.projectDao = new ProjectDao();
    }

    public List<Project> getAll() {
        return projectDao.findAll();
    }

    public List<Project> search(String keyword) {
        return projectDao.search(keyword);
    }

    public Project getById(int id) {
        return projectDao.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Project not found: id " + id));
    }

    public Project create(Project project) {
        validate(project);

        project.setProjectCode(projectDao.getNextProjectCode());
        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus(Constants.ProjectStatus.PLANNING);
        }
        project.setActive(true);

        return projectDao.insertWithSites(project);
    }

    public void update(Project project) {
        validate(project);

        if (project.getId() == null) {
            throw new ValidationException("Cannot update a project without an ID.");
        }
        projectDao.updateWithSites(project);
    }

    public void setActive(int id, boolean active) {
        Project project = getById(id);
        project.setActive(active);
        projectDao.updateWithSites(project);
    }

    private void validate(Project project) {
        if (project.getName() == null || project.getName().isBlank()) {
            throw new ValidationException("Project name is required.");
        }
        if (project.getStartDate() != null && project.getEndDate() != null
                && project.getEndDate().isBefore(project.getStartDate())) {
            throw new ValidationException("End date cannot be before start date.");
        }
    }
}
package com.mtech.adms.service;

import com.mtech.adms.dao.DashboardDao;
import com.mtech.adms.model.DashboardStats;
import com.mtech.adms.model.RecentDeploymentSummary;

import java.util.List;

/**
 * Thin service layer over DashboardDao. Kept as a separate class
 * (rather than having the Controller call DashboardDao directly)
 * to stay consistent with the Controller -> Service -> DAO pattern
 * used everywhere else, even though there's minimal business logic
 * here today. Future phases may add logic like "highlight cards red
 * if overdue returns exceed a threshold" here.
 */
public class DashboardService {

    private final DashboardDao dashboardDao;

    public DashboardService() {
        this.dashboardDao = new DashboardDao();
    }

    public DashboardStats getStats() {
        return dashboardDao.getStats();
    }

    public List<RecentDeploymentSummary> getRecentDeployments() {
        return dashboardDao.getRecentDeployments(5);
    }

    public List<RecentDeploymentSummary> getOverdueDeployments() {
        return dashboardDao.getOverdueDeployments(5);
    }
}
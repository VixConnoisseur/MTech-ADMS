package com.mtech.adms.util;

/**
 * Application-wide constant values. Centralizing these avoid
 * "magic strings" scattered across the codebase and makes future
 * changes (e.g. renaming a status) a one-line edit instead of a
 * find-and-replace across dozens of files.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Asset status values - must match the ENUM defined in the assets table
    public static final class AssetStatus {
        public static final String AVAILABLE = "AVAILABLE";
        public static final String DEPLOYED = "DEPLOYED";
        public static final String RETURNED = "RETURNED";
        public static final String DAMAGED = "DAMAGED";
        public static final String UNDER_REPAIR = "UNDER_REPAIR";
        public static final String MISSING = "MISSING";
        public static final String LOST = "LOST";
        public static final String RETIRED = "RETIRED";

        private AssetStatus() {
        }
    }

    // Deployment status values - must match the ENUM defined in the deployments table
    public static final class DeploymentStatus {
        public static final String ACTIVE = "ACTIVE";
        public static final String PARTIALLY_RETURNED = "PARTIALLY_RETURNED";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";

        private DeploymentStatus() {
        }
    }

    // Role names - must match the seed data in the roles table
    public static final class Role {
        public static final String ADMIN = "ADMIN";
        public static final String PROJECT_MANAGER = "PROJECT_MANAGER";
        public static final String INVENTORY_STAFF = "INVENTORY_STAFF";
        public static final String SUPERVISOR = "SUPERVISOR";
        public static final String VIEWER = "VIEWER";

        private Role() {
        }
    }

    // Deployment team roles - must match the ENUM in deployment_employees
    public static final class DeploymentRole {
        public static final String LEADER = "LEADER";
        public static final String MEMBER = "MEMBER";

        private DeploymentRole() {
        }
    }

    public static final class ProjectStatus {
        public static final String PLANNING = "PLANNING";
        public static final String ACTIVE = "ACTIVE";
        public static final String ON_HOLD = "ON_HOLD";
        public static final String COMPLETED = "COMPLETED";
        public static final String CANCELLED = "CANCELLED";

        private ProjectStatus() {
        }
    }

    public static final String DEFAULT_WAREHOUSE_LOCATION = "MTech Warehouse";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
}
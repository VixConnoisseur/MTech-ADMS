-- =====================================================================
-- MTech Asset & Deployment Management System (MTech-ADMS)
-- Database Schema
-- =====================================================================
-- Run this entire script against a fresh MySQL 8.0+ server.
-- It creates the database, all tables, keys, constraints, and indexes.
-- =====================================================================

DROP DATABASE IF EXISTS mtech_adms;
CREATE DATABASE mtech_adms
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mtech_adms;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 1. ROLES
-- =====================================================================
CREATE TABLE roles (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50)  NOT NULL,
    description   VARCHAR(255) NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB;

-- =====================================================================
-- 2. USERS
-- =====================================================================
CREATE TABLE users (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    role_id        INT          NOT NULL,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at  DATETIME     NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_users_role ON users(role_id);

-- =====================================================================
-- 3. EMPLOYEES
-- =====================================================================
CREATE TABLE employees (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT          NULL,
    employee_code  VARCHAR(20)  NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    contact_no     VARCHAR(20)  NULL,
    position       VARCHAR(50)  NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_employees_code    UNIQUE (employee_code),
    CONSTRAINT uq_employees_user_id UNIQUE (user_id),
    CONSTRAINT fk_employees_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_employees_full_name ON employees(full_name);
CREATE INDEX idx_employees_is_active ON employees(is_active);

-- =====================================================================
-- 4. PROJECTS
-- =====================================================================
CREATE TABLE projects (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    project_code  VARCHAR(20)  NOT NULL,
    name          VARCHAR(150) NOT NULL,
    client_name   VARCHAR(150) NULL,
    start_date    DATE         NULL,
    end_date      DATE         NULL,
    status        ENUM('PLANNING','ACTIVE','ON_HOLD','COMPLETED','CANCELLED')
                               NOT NULL DEFAULT 'PLANNING',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_projects_code UNIQUE (project_code)
) ENGINE=InnoDB;

CREATE INDEX idx_projects_status    ON projects(status);
CREATE INDEX idx_projects_is_active ON projects(is_active);

-- =====================================================================
-- 5. SITES
-- =====================================================================
CREATE TABLE sites (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    site_name   VARCHAR(150) NOT NULL,
    address     VARCHAR(255) NULL,
    city        VARCHAR(100) NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE INDEX idx_sites_site_name ON sites(site_name);
CREATE INDEX idx_sites_is_active ON sites(is_active);

-- =====================================================================
-- 6. PROJECT_SITES (junction: a project can span multiple sites)
-- =====================================================================
CREATE TABLE project_sites (
    project_id  INT      NOT NULL,
    site_id     INT      NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, site_id),
    CONSTRAINT fk_projsites_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_projsites_site
        FOREIGN KEY (site_id) REFERENCES sites(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 7. ASSET_CATEGORIES
-- =====================================================================
CREATE TABLE asset_categories (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    description  VARCHAR(255) NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_categories_name UNIQUE (name)
) ENGINE=InnoDB;

-- =====================================================================
-- 8. DEPLOYMENTS
-- (created before ASSETS because assets.current_deployment_id
--  references this table)
-- =====================================================================
CREATE TABLE deployments (
    id                     INT AUTO_INCREMENT PRIMARY KEY,
    deployment_code        VARCHAR(20)  NOT NULL,
    project_id             INT          NOT NULL,
    site_id                INT          NOT NULL,
    team_leader_id         INT          NOT NULL,
    deployment_date        DATE         NOT NULL,
    expected_return_date   DATE         NOT NULL,
    actual_return_date     DATE         NULL,
    status                 ENUM('ACTIVE','PARTIALLY_RETURNED','COMPLETED','CANCELLED')
                                        NOT NULL DEFAULT 'ACTIVE',
    notes                  TEXT         NULL,
    created_by             INT          NOT NULL,
    created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_deployments_code UNIQUE (deployment_code),
    CONSTRAINT fk_deployments_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_deployments_site
        FOREIGN KEY (site_id) REFERENCES sites(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_deployments_leader
        FOREIGN KEY (team_leader_id) REFERENCES employees(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_deployments_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_deployments_dates
        CHECK (expected_return_date >= deployment_date)
) ENGINE=InnoDB;

CREATE INDEX idx_deployments_status          ON deployments(status);
CREATE INDEX idx_deployments_deployment_date ON deployments(deployment_date);
CREATE INDEX idx_deployments_project         ON deployments(project_id);
CREATE INDEX idx_deployments_site            ON deployments(site_id);

-- =====================================================================
-- 9. ASSETS
-- =====================================================================
CREATE TABLE assets (
    id                     INT AUTO_INCREMENT PRIMARY KEY,
    asset_id               VARCHAR(20)  NOT NULL,
    tool_name              VARCHAR(150) NOT NULL,
    serial_number          VARCHAR(100) NULL,
    category_id            INT          NOT NULL,
    condition_status       ENUM('GOOD','FAIR','POOR')
                                        NOT NULL DEFAULT 'GOOD',
    status                 ENUM('AVAILABLE','DEPLOYED','RETURNED','DAMAGED',
                                 'UNDER_REPAIR','MISSING','LOST','RETIRED')
                                        NOT NULL DEFAULT 'AVAILABLE',
    current_deployment_id  INT          NULL,
    current_location       VARCHAR(150) NOT NULL DEFAULT 'MTech Warehouse',
    purchase_date          DATE         NULL,
    purchase_cost          DECIMAL(10,2) NULL,
    qr_code_path           VARCHAR(255) NULL,
    notes                  TEXT         NULL,
    created_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_assets_asset_id UNIQUE (asset_id),
    CONSTRAINT fk_assets_category
        FOREIGN KEY (category_id) REFERENCES asset_categories(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_assets_current_deployment
        FOREIGN KEY (current_deployment_id) REFERENCES deployments(id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_assets_status      ON assets(status);
CREATE INDEX idx_assets_category    ON assets(category_id);
CREATE INDEX idx_assets_tool_name   ON assets(tool_name);
CREATE INDEX idx_assets_current_dep ON assets(current_deployment_id);

-- =====================================================================
-- 10. DEPLOYMENT_EMPLOYEES (junction: leader + team members)
-- =====================================================================
CREATE TABLE deployment_employees (
    deployment_id       INT      NOT NULL,
    employee_id         INT      NOT NULL,
    role_in_deployment  ENUM('LEADER','MEMBER') NOT NULL DEFAULT 'MEMBER',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (deployment_id, employee_id),
    CONSTRAINT fk_depemp_deployment
        FOREIGN KEY (deployment_id) REFERENCES deployments(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_depemp_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_depemp_employee ON deployment_employees(employee_id);

-- =====================================================================
-- 11. DEPLOYMENT_ITEMS (junction: assets attached to a deployment)
-- =====================================================================
CREATE TABLE deployment_items (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    deployment_id       INT      NOT NULL,
    asset_id            INT      NOT NULL,
    deployed_condition  ENUM('GOOD','FAIR','POOR') NOT NULL DEFAULT 'GOOD',
    return_condition    ENUM('GOOD','DAMAGED','MISSING','UNDER_REPAIR') NULL,
    returned_at         DATETIME NULL,
    notes               TEXT     NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_depitems_dep_asset UNIQUE (deployment_id, asset_id),
    CONSTRAINT fk_depitems_deployment
        FOREIGN KEY (deployment_id) REFERENCES deployments(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_depitems_asset
        FOREIGN KEY (asset_id) REFERENCES assets(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_depitems_asset ON deployment_items(asset_id);

-- =====================================================================
-- 12. ASSET_MOVEMENTS (full historical audit trail)
-- =====================================================================
CREATE TABLE asset_movements (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    asset_id       INT          NOT NULL,
    deployment_id  INT          NULL,
    from_status    VARCHAR(20)  NULL,
    to_status      VARCHAR(20)  NOT NULL,
    from_location  VARCHAR(150) NULL,
    to_location    VARCHAR(150) NULL,
    moved_by       INT          NOT NULL,
    notes          TEXT         NULL,
    moved_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movements_asset
        FOREIGN KEY (asset_id) REFERENCES assets(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_movements_deployment
        FOREIGN KEY (deployment_id) REFERENCES deployments(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_movements_moved_by
        FOREIGN KEY (moved_by) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_movements_asset    ON asset_movements(asset_id);
CREATE INDEX idx_movements_moved_at ON asset_movements(moved_at);

-- =====================================================================
-- 13. PROJECT_EMPLOYEES (junction: general project staffing,
--     broader/longer-term than a single deployment)
-- =====================================================================
CREATE TABLE project_employees (
    project_id     INT      NOT NULL,
    employee_id    INT      NOT NULL,
    assigned_date  DATE     NOT NULL DEFAULT (CURRENT_DATE),
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, employee_id),
    CONSTRAINT fk_projemp_project
        FOREIGN KEY (project_id) REFERENCES projects(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_projemp_employee
        FOREIGN KEY (employee_id) REFERENCES employees(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =====================================================================
-- 14. ACTIVITY_LOGS (system-wide audit log)
-- =====================================================================
CREATE TABLE activity_logs (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT          NULL,
    action       VARCHAR(100) NOT NULL,
    entity_type  VARCHAR(50)  NULL,
    entity_id    INT          NULL,
    details      TEXT         NULL,
    ip_address   VARCHAR(45)  NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activitylogs_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_activitylogs_user       ON activity_logs(user_id);
CREATE INDEX idx_activitylogs_created_at ON activity_logs(created_at);

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- SEED DATA — Roles and Asset Categories
-- (minimal lookup data so the app has something to work with)
-- =====================================================================

INSERT INTO roles (name, description) VALUES
    ('ADMIN',            'Full system access, including user management'),
    ('PROJECT_MANAGER',  'Manages projects, sites, and deployments'),
    ('INVENTORY_STAFF',  'Manages assets, deployments, and returns'),
    ('SUPERVISOR',       'Field supervisor, views and confirms deployments'),
    ('VIEWER',           'Read-only access to reports and dashboards');

INSERT INTO asset_categories (name, description) VALUES
    ('Testing Equipment', 'Cable testers, tone generators, certifiers'),
    ('Power Tools',       'Drills, saws, crimpers (powered)'),
    ('Hand Tools',        'Manual tools: strippers, punch-down tools, etc.'),
    ('Safety Equipment',  'PPE and site safety gear'),
    ('Ladders & Access',  'Ladders, lifts, scaffolding components');

-- =====================================================================
-- END OF SCRIPT
-- =====================================================================

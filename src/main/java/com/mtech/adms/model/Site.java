package com.mtech.adms.model;

/**
 * Minimal Site model - just enough to support the site multi-select
 * in the Project form (Phase 10). Phase 11 (Site Management) will
 * expand this with the remaining fields (address, city, is_active
 * setters/getters already present here for that reuse) and its own
 * full CRUD screen.
 */
public class Site {

    private Integer id;
    private String siteName;
    private String address;
    private String city;
    private boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return siteName;
    }
}
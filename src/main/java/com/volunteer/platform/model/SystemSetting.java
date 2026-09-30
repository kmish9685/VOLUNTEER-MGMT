package com.volunteer.platform.model;

import jakarta.persistence.*;

/**
 * =====================================================================
 * SystemSetting Entity (Model Layer)
 * ---------------------------------------------------------------------
 * Stores configurable platform parameters in key-value format.
 * Examples:
 * - platform_name: Displayed in the navbar and title.
 * - allow_registrations: Controls whether new registrations are open.
 * - max_hours_per_log: Maximum hours a volunteer can log in one entry.
 * - auto_approve_opportunities: Bypasses admin review if true.
 *
 * Stored in the "system_settings" table in PostgreSQL (Supabase).
 * =====================================================================
 */
@Entity
@Table(name = "system_settings")
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(nullable = false, length = 255)
    private String settingValue;

    @Column(length = 255)
    private String description;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public SystemSetting() {
    }

    /**
     * Parameterized constructor for creating a new system setting.
     */
    public SystemSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    // ==========================================
    // GETTERS AND SETTERS (Explicit, No Lombok)
    // ==========================================

    /**
     * Returns the unique database ID of the setting record.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the setting record.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the lookup key of the configuration item.
     */
    public String getSettingKey() {
        return settingKey;
    }

    /**
     * Sets the lookup key of the configuration item.
     */
    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    /**
     * Returns the current string value of the configuration setting.
     */
    public String getSettingValue() {
        return settingValue;
    }

    /**
     * Sets the current string value of the configuration setting.
     */
    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    /**
     * Returns a human-friendly description of what this setting controls.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the human-friendly description of what this setting controls.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}

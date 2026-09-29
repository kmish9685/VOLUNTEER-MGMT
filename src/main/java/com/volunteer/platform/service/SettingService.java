package com.volunteer.platform.service;

import com.volunteer.platform.model.SystemSetting;
import com.volunteer.platform.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =====================================================================
 * SettingService (Business Logic Layer)
 * ---------------------------------------------------------------------
 * Manages all platform-level configuration settings stored in the database.
 * Provides helper methods to read settings as Strings, Booleans, and Integers,
 * and allows the Administrator to update setting values dynamically.
 * =====================================================================
 */
@Service
public class SettingService {

    private final SystemSettingRepository settingRepository;

    /**
     * Constructor for injecting the SystemSettingRepository dependency.
     */
    public SettingService(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    /**
     * Retrieves a setting value by key, returning a default value if not found.
     */
    public String getValue(String key, String defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }

    /**
     * Retrieves a setting value as a boolean (e.g. "allow_registrations", "auto_approve_opportunities").
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String val = getValue(key, null);
        if (val == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val.trim());
    }

    /**
     * Retrieves a setting value as an integer (e.g. "max_hours_per_log").
     */
    public int getInt(String key, int defaultValue) {
        String val = getValue(key, null);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns all system settings ordered alphabetically for the Admin settings page.
     */
    public List<SystemSetting> getAllSettings() {
        return settingRepository.findAllByOrderBySettingKeyAsc();
    }

    /**
     * Updates an existing setting's value in the database.
     */
    @Transactional
    public void updateSetting(String key, String newValue) {
        settingRepository.findBySettingKey(key).ifPresent(setting -> {
            setting.setSettingValue(newValue.trim());
            settingRepository.save(setting);
        });
    }
}

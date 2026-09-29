package com.volunteer.platform.repository;

import com.volunteer.platform.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * SystemSettingRepository (Data Access Layer)
 * ---------------------------------------------------------------------
 * Handles database operations for key-value system settings.
 * Supports retrieval by setting key, check for existence, and ordered listing.
 * =====================================================================
 */
@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    /**
     * Looks up a configuration setting by its unique setting key (e.g. "platform_name").
     */
    Optional<SystemSetting> findBySettingKey(String settingKey);

    /**
     * Checks if a setting with the given key already exists in the database.
     */
    boolean existsBySettingKey(String settingKey);

    /**
     * Retrieves all system settings ordered alphabetically by key.
     */
    List<SystemSetting> findAllByOrderBySettingKeyAsc();
}

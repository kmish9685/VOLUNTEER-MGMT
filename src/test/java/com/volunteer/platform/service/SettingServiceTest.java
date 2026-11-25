package com.volunteer.platform.service;

import com.volunteer.platform.model.SystemSetting;
import com.volunteer.platform.repository.SystemSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * =====================================================================
 * SettingServiceTest (pure unit test with Mockito)
 * ---------------------------------------------------------------------
 * No Spring, no database: the repository is replaced by a Mockito "mock"
 * that returns whatever we tell it to. This makes the test run in
 * milliseconds and tests ONLY the SettingService logic.
 * =====================================================================
 */
class SettingServiceTest {

    private SystemSettingRepository repository;
    private SettingService settingService;

    @BeforeEach
    void setUp() {
        repository = mock(SystemSettingRepository.class);
        settingService = new SettingService(repository);
    }

    @Test
    @DisplayName("getInt reads the number saved by the admin")
    void getIntReturnsSavedValue() {
        when(repository.findBySettingKey("max_hours_per_log"))
                .thenReturn(Optional.of(new SystemSetting("max_hours_per_log", " 8 ", "limit")));

        assertEquals(8, settingService.getInt("max_hours_per_log", 12));
    }

    @Test
    @DisplayName("getInt falls back to the default when the setting is missing")
    void getIntUsesDefaultWhenMissing() {
        when(repository.findBySettingKey("max_hours_per_log")).thenReturn(Optional.empty());

        assertEquals(12, settingService.getInt("max_hours_per_log", 12));
    }

    @Test
    @DisplayName("getInt falls back to the default when the saved value is not a number")
    void getIntUsesDefaultWhenNotANumber() {
        when(repository.findBySettingKey("max_hours_per_log"))
                .thenReturn(Optional.of(new SystemSetting("max_hours_per_log", "abc", "limit")));

        assertEquals(12, settingService.getInt("max_hours_per_log", 12));
    }

    @Test
    @DisplayName("updateSetting trims the new value and saves it")
    void updateSettingTrimsAndSaves() {
        SystemSetting setting = new SystemSetting("allow_registrations", "true", "open?");
        when(repository.findBySettingKey("allow_registrations")).thenReturn(Optional.of(setting));

        settingService.updateSetting("allow_registrations", "  false  ");

        assertEquals("false", setting.getSettingValue());
        verify(repository).save(setting);
    }
}

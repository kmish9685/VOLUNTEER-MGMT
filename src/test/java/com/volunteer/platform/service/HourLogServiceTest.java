package com.volunteer.platform.service;

import com.volunteer.platform.BaseIntegrationTest;
import com.volunteer.platform.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =====================================================================
 * HourLogServiceTest
 * ---------------------------------------------------------------------
 * Tests the rules for logging volunteer hours:
 * - only for ATTENDED events
 * - more than 0 and not more than the admin limit (default 12)
 * - only the volunteer who attended can log hours
 * =====================================================================
 */
@Transactional
class HourLogServiceTest extends BaseIntegrationTest {

    @Autowired
    private HourLogService hourLogService;

    private User volunteer;
    private Opportunity pastEvent;

    @BeforeEach
    void setUp() {
        User organization = createUser(Role.ORGANIZATION);
        volunteer = createUser(Role.VOLUNTEER);
        pastEvent = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().minusDays(1), 10);
    }

    @Test
    @DisplayName("20 hours is rejected (over the limit), 4 hours is accepted - same as the live demo")
    void hoursOverLimitRejectedAndValidHoursAccepted() {
        Registration attended = createRegistration(volunteer, pastEvent, RegistrationStatus.ATTENDED);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> hourLogService.logHours(attended.getId(), 20, "Too many hours", volunteer));
        assertTrue(ex.getMessage().contains("cannot exceed"));

        HourLog saved = hourLogService.logHours(attended.getId(), 4, "Planted trees", volunteer);
        assertEquals(4.0, saved.getHours());
        assertEquals(4.0, hourLogService.getTotalHoursForVolunteer(volunteer));
    }

    @Test
    @DisplayName("Hours cannot be logged if attendance is not ATTENDED")
    void hoursBlockedWhenNotAttended() {
        Registration registered = createRegistration(volunteer, pastEvent, RegistrationStatus.REGISTERED);

        assertThrows(IllegalStateException.class,
                () -> hourLogService.logHours(registered.getId(), 3, "Work", volunteer));
    }

    @Test
    @DisplayName("Zero hours is rejected")
    void zeroHoursRejected() {
        Registration attended = createRegistration(volunteer, pastEvent, RegistrationStatus.ATTENDED);

        assertThrows(IllegalArgumentException.class,
                () -> hourLogService.logHours(attended.getId(), 0, "Nothing", volunteer));
    }

    @Test
    @DisplayName("A volunteer cannot log hours on someone else's registration")
    void cannotLogHoursForAnotherVolunteer() {
        Registration attended = createRegistration(volunteer, pastEvent, RegistrationStatus.ATTENDED);
        User otherVolunteer = createUser(Role.VOLUNTEER);

        assertThrows(IllegalArgumentException.class,
                () -> hourLogService.logHours(attended.getId(), 3, "Work", otherVolunteer));
    }
}

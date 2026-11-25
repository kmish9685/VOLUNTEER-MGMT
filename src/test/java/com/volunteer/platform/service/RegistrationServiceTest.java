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
 * RegistrationServiceTest
 * ---------------------------------------------------------------------
 * Tests the sign-up and attendance business rules ("Smart Rules" slide).
 * @Transactional: every test is rolled back at the end, so the database
 * is clean for the next test.
 * =====================================================================
 */
@Transactional
class RegistrationServiceTest extends BaseIntegrationTest {

    @Autowired
    private RegistrationService registrationService;

    private User organization;
    private User volunteer;

    @BeforeEach
    void setUp() {
        organization = createUser(Role.ORGANIZATION);
        volunteer = createUser(Role.VOLUNTEER);
    }

    @Test
    @DisplayName("Volunteer can sign up for an approved, upcoming event")
    void signUpSucceedsForApprovedFutureEvent() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(5), 10);

        Registration registration = registrationService.signUp(event.getId(), volunteer);

        assertNotNull(registration.getId());
        assertEquals(RegistrationStatus.REGISTERED, registration.getStatus());
        assertEquals(9, registrationService.getRemainingSlots(event));
    }

    @Test
    @DisplayName("Same volunteer cannot sign up twice for the same event")
    void doubleSignUpIsBlocked() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(5), 10);
        registrationService.signUp(event.getId(), volunteer);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registrationService.signUp(event.getId(), volunteer));
        assertTrue(ex.getMessage().contains("already registered"));
    }

    @Test
    @DisplayName("Sign-up is blocked when all slots are full")
    void signUpBlockedWhenEventIsFull() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(5), 1);
        registrationService.signUp(event.getId(), createUser(Role.VOLUNTEER)); // takes the only slot

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registrationService.signUp(event.getId(), volunteer));
        assertTrue(ex.getMessage().contains("maximum volunteer capacity"));
    }

    @Test
    @DisplayName("Volunteer cannot sign up for a PENDING (not yet approved) event")
    void signUpBlockedForPendingEvent() {
        Opportunity event = createEvent(organization, OpportunityStatus.PENDING, LocalDate.now().plusDays(5), 10);

        assertThrows(IllegalStateException.class, () -> registrationService.signUp(event.getId(), volunteer));
    }

    @Test
    @DisplayName("Volunteer cannot sign up for an event that already happened")
    void signUpBlockedForPastEvent() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().minusDays(1), 10);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registrationService.signUp(event.getId(), volunteer));
        assertTrue(ex.getMessage().contains("already taken place"));
    }

    @Test
    @DisplayName("Blocked volunteer cannot sign up")
    void blockedVolunteerCannotSignUp() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(5), 10);
        volunteer.setStatus(UserStatus.BLOCKED);

        assertThrows(IllegalStateException.class, () -> registrationService.signUp(event.getId(), volunteer));
    }

    @Test
    @DisplayName("Cancelling frees the slot, and signing up again re-activates the same registration")
    void cancelThenSignUpAgain() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(5), 2);
        Registration first = registrationService.signUp(event.getId(), volunteer);

        registrationService.cancelRegistration(first.getId(), volunteer);
        assertEquals(2, registrationService.getRemainingSlots(event));

        Registration again = registrationService.signUp(event.getId(), volunteer);
        assertEquals(first.getId(), again.getId());
        assertEquals(RegistrationStatus.REGISTERED, again.getStatus());
    }

    @Test
    @DisplayName("Organization can mark attendance on or after the event date")
    void markAttendanceForPastEvent() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().minusDays(2), 10);
        Registration registration = createRegistration(volunteer, event, RegistrationStatus.REGISTERED);

        registrationService.markAttendance(registration.getId(), RegistrationStatus.ATTENDED, organization);

        assertEquals(RegistrationStatus.ATTENDED,
                registrationRepository.findById(registration.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Attendance cannot be marked before the event date")
    void markAttendanceBlockedForFutureEvent() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(3), 10);
        Registration registration = createRegistration(volunteer, event, RegistrationStatus.REGISTERED);

        assertThrows(IllegalStateException.class, () -> registrationService.markAttendance(
                registration.getId(), RegistrationStatus.ATTENDED, organization));
    }

    @Test
    @DisplayName("An organization cannot mark attendance for another organization's event")
    void markAttendanceBlockedForOtherOrganization() {
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().minusDays(1), 10);
        Registration registration = createRegistration(volunteer, event, RegistrationStatus.REGISTERED);
        User otherOrganization = createUser(Role.ORGANIZATION);

        assertThrows(IllegalArgumentException.class, () -> registrationService.markAttendance(
                registration.getId(), RegistrationStatus.ATTENDED, otherOrganization));
    }
}

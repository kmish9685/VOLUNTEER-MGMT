package com.volunteer.platform;

import com.volunteer.platform.model.*;
import com.volunteer.platform.repository.HourLogRepository;
import com.volunteer.platform.repository.OpportunityRepository;
import com.volunteer.platform.repository.RegistrationRepository;
import com.volunteer.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * =====================================================================
 * BaseIntegrationTest (Test Helper)
 * ---------------------------------------------------------------------
 * Every test class extends this class. It starts the full Spring Boot
 * application on an in-memory H2 database (see src/test/resources) and
 * gives small helper methods to create test users and events.
 *
 * Each helper uses a random email, so tests never clash with each other
 * or with the demo data loaded by DataLoader.
 * =====================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OpportunityRepository opportunityRepository;

    @Autowired
    protected RegistrationRepository registrationRepository;

    @Autowired
    protected HourLogRepository hourLogRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * Saves a new ACTIVE user with the given role and password.
     */
    protected User createUser(Role role, String rawPassword) {
        String email = role.name().toLowerCase() + "-" + UUID.randomUUID() + "@test.com";
        User user = new User("Test " + role.name(), email, passwordEncoder.encode(rawPassword),
                "9999999999", role, UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    /**
     * Saves a new ACTIVE user with a default password.
     */
    protected User createUser(Role role) {
        return createUser(role, "test123");
    }

    /**
     * Saves a new event (opportunity) for the given organization.
     */
    protected Opportunity createEvent(User organization, OpportunityStatus status, LocalDate date, int slots) {
        Opportunity event = new Opportunity("Test Event", "Event created by an automated test",
                "Test City", date, LocalTime.of(9, 0), LocalTime.of(12, 0), slots, status, organization);
        return opportunityRepository.save(event);
    }

    /**
     * Saves a registration directly (used to set up past events and attendance).
     */
    protected Registration createRegistration(User volunteer, Opportunity event, RegistrationStatus status) {
        return registrationRepository.save(new Registration(volunteer, event, status));
    }
}

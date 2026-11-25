package com.volunteer.platform.jdbc;

import com.volunteer.platform.BaseIntegrationTest;
import com.volunteer.platform.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =====================================================================
 * ParticipationReportDaoTest
 * ---------------------------------------------------------------------
 * Checks that the plain JDBC report query counts registrations and hours
 * correctly. NOT @Transactional: the DAO opens its own JDBC connection,
 * so the test data must be committed for the DAO to see it.
 * =====================================================================
 */
class ParticipationReportDaoTest extends BaseIntegrationTest {

    @Autowired
    private ParticipationReportDao participationReportDao;

    @Test
    @DisplayName("Report counts attended / absent correctly, ignores cancelled, and sums hours")
    void reportCountsAndHoursAreCorrect() {
        User organization = createUser(Role.ORGANIZATION);
        Opportunity busyEvent = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().minusDays(3), 5);
        Opportunity emptyEvent = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(3), 8);

        Registration attended = createRegistration(createUser(Role.VOLUNTEER), busyEvent, RegistrationStatus.ATTENDED);
        createRegistration(createUser(Role.VOLUNTEER), busyEvent, RegistrationStatus.ABSENT);
        createRegistration(createUser(Role.VOLUNTEER), busyEvent, RegistrationStatus.CANCELLED);
        hourLogRepository.save(new HourLog(attended, 4.0, "Cleaned the park"));

        List<ParticipationReportRow> rows = participationReportDao.findReportByOrganization(organization.getId());

        assertEquals(2, rows.size(), "One row per event of this organization");

        ParticipationReportRow busy = findRow(rows, busyEvent.getId());
        assertEquals("Test Event", busy.getTitle());
        assertEquals(5, busy.getTotalSlots());
        assertEquals(2, busy.getRegisteredCount(), "ATTENDED + ABSENT, cancelled is not counted");
        assertEquals(1, busy.getAttendedCount());
        assertEquals(1, busy.getAbsentCount());
        assertEquals(4.0, busy.getTotalHours());

        ParticipationReportRow empty = findRow(rows, emptyEvent.getId());
        assertEquals(0, empty.getRegisteredCount());
        assertEquals(0.0, empty.getTotalHours());
    }

    @Test
    @DisplayName("An organization with no events gets an empty report")
    void organizationWithoutEventsGetsEmptyReport() {
        User newOrganization = createUser(Role.ORGANIZATION);

        assertTrue(participationReportDao.findReportByOrganization(newOrganization.getId()).isEmpty());
    }

    /**
     * Finds the report row of one event (fails the test if it is missing).
     */
    private ParticipationReportRow findRow(List<ParticipationReportRow> rows, Long opportunityId) {
        return rows.stream()
                .filter(row -> row.getOpportunityId().equals(opportunityId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No report row for event " + opportunityId));
    }
}

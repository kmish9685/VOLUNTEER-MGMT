package com.volunteer.platform.repository;

import com.volunteer.platform.model.Opportunity;
import com.volunteer.platform.model.Registration;
import com.volunteer.platform.model.RegistrationStatus;
import com.volunteer.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * RegistrationRepository (Data Access Layer)
 * ---------------------------------------------------------------------
 * Handles database operations for volunteer event enrollments.
 * Supports roster queries, attendance tracking, slot calculations,
 * and participation statistics for dashboards and reports.
 * =====================================================================
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    /**
     * Retrieves all registrations made by a specific volunteer ordered from newest to oldest.
     */
    List<Registration> findByVolunteerOrderByRegisteredAtDesc(User volunteer);

    /**
     * Retrieves all volunteers registered for a given opportunity.
     */
    List<Registration> findByOpportunityOrderByRegisteredAtDesc(Opportunity opportunity);

    /**
     * Finds an enrollment by volunteer and opportunity (used to check prior signup).
     */
    Optional<Registration> findByVolunteerAndOpportunity(User volunteer, Opportunity opportunity);

    /**
     * Checks if a volunteer is already registered for an opportunity.
     */
    boolean existsByVolunteerAndOpportunity(User volunteer, Opportunity opportunity);

    /**
     * Checks if a volunteer has an active (non-cancelled) signup for an opportunity.
     */
    boolean existsByVolunteerAndOpportunityAndStatusNot(User volunteer, Opportunity opportunity, RegistrationStatus status);

    /**
     * Counts active (non-cancelled) registrations for an opportunity to calculate remaining slots.
     */
    long countByOpportunityAndStatusNot(Opportunity opportunity, RegistrationStatus status);

    /**
     * Counts registrations for an opportunity having an exact status (e.g., ATTENDED, ABSENT).
     */
    long countByOpportunityAndStatus(Opportunity opportunity, RegistrationStatus status);

    /**
     * Counts all registrations across the platform with a specific status.
     */
    long countByStatus(RegistrationStatus status);

    /**
     * Counts total registrations made by a volunteer.
     */
    long countByVolunteer(User volunteer);

    /**
     * Counts registrations of a volunteer with a specific status (e.g., ATTENDED).
     */
    long countByVolunteerAndStatus(User volunteer, RegistrationStatus status);

    /**
     * Counts total registrations across all events hosted by a given organization.
     */
    @Query("SELECT COUNT(r) FROM Registration r WHERE r.opportunity.organization = :org")
    long countByOrganization(@Param("org") User org);

    /**
     * Retrieves all registrations ordered by timestamp for the Admin participation monitor.
     */
    List<Registration> findAllByOrderByRegisteredAtDesc();

    /**
     * Checks whether a volunteer has any non-cancelled registration under an organization (for chat authorization).
     */
    @Query("SELECT COUNT(r) > 0 FROM Registration r WHERE r.volunteer = :volunteer AND r.opportunity.organization = :org AND r.status != 'CANCELLED'")
    boolean hasActiveRegistrationWithOrg(@Param("volunteer") User volunteer, @Param("org") User org);

    /**
     * Retrieves distinct volunteers who registered for any event of this organization (for messaging contact list).
     */
    @Query("SELECT DISTINCT r.volunteer FROM Registration r WHERE r.opportunity.organization = :org AND r.status != 'CANCELLED'")
    List<User> findDistinctVolunteersByOrganization(@Param("org") User org);

    /**
     * Retrieves distinct organizations whose events this volunteer registered for (for messaging contact list).
     */
    @Query("SELECT DISTINCT r.opportunity.organization FROM Registration r WHERE r.volunteer = :volunteer AND r.status != 'CANCELLED'")
    List<User> findDistinctOrganizationsByVolunteer(@Param("volunteer") User volunteer);
}

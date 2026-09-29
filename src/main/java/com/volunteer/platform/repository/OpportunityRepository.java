package com.volunteer.platform.repository;

import com.volunteer.platform.model.Opportunity;
import com.volunteer.platform.model.OpportunityStatus;
import com.volunteer.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * =====================================================================
 * OpportunityRepository (Data Access Layer)
 * ---------------------------------------------------------------------
 * Handles database operations for volunteering opportunities.
 * Supports filtering by review status, organization, date availability,
 * and search queries across title and location.
 * =====================================================================
 */
@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    /**
     * Retrieves all opportunities matching a specific status (e.g., PENDING or APPROVED).
     */
    List<Opportunity> findByStatus(OpportunityStatus status);

    /**
     * Retrieves all opportunities posted by a specific Organization.
     */
    List<Opportunity> findByOrganizationOrderByCreatedAtDesc(User organization);

    /**
     * Retrieves opportunities posted by an Organization filtered by status.
     */
    List<Opportunity> findByOrganizationAndStatusOrderByCreatedAtDesc(User organization, OpportunityStatus status);

    /**
     * Counts the number of opportunities having a given status across the platform.
     */
    long countByStatus(OpportunityStatus status);

    /**
     * Counts the total number of opportunities created by a specific Organization.
     */
    long countByOrganization(User organization);

    /**
     * Counts opportunities of a specific status created by an Organization.
     */
    long countByOrganizationAndStatus(User organization, OpportunityStatus status);

    /**
     * Retrieves approved opportunities scheduled on or after the current date, ordered by event date.
     */
    List<Opportunity> findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(OpportunityStatus status, LocalDate date);

    /**
     * Retrieves the top 6 upcoming approved opportunities to display on the public landing page.
     */
    List<Opportunity> findTop6ByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(OpportunityStatus status, LocalDate date);

    /**
     * Searches approved upcoming opportunities matching keyword in title or location.
     */
    @Query("SELECT o FROM Opportunity o WHERE o.status = :status AND o.eventDate >= :today AND " +
           "(LOWER(o.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(o.location) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY o.eventDate ASC")
    List<Opportunity> searchUpcomingApproved(@Param("status") OpportunityStatus status,
                                            @Param("today") LocalDate today,
                                            @Param("query") String query);
}

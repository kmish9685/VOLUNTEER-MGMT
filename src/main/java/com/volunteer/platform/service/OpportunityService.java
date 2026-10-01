package com.volunteer.platform.service;

import com.volunteer.platform.model.Opportunity;
import com.volunteer.platform.model.OpportunityStatus;
import com.volunteer.platform.model.User;
import com.volunteer.platform.repository.OpportunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * OpportunityService (Business Logic Layer)
 * ---------------------------------------------------------------------
 * Handles all business logic for volunteering opportunities:
 * - All new and edited opportunities go to PENDING status (no auto-approve)
 * - Organization update & delete permissions
 * - Admin approval and rejection workflow with feedback remarks
 * - Filtering upcoming approved events for volunteers and public landing
 * =====================================================================
 */
@SuppressWarnings("null")
@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final ActivityLogService activityLogService;

    /**
     * Constructor for injecting repository and audit services.
     */
    public OpportunityService(OpportunityRepository opportunityRepository,
                              ActivityLogService activityLogService) {
        this.opportunityRepository = opportunityRepository;
        this.activityLogService = activityLogService;
    }

    /**
     * Creates a new volunteering opportunity for an Organization.
     * Status is always set to PENDING — Admin must review and approve.
     */
    @Transactional
    public Opportunity createOpportunity(Opportunity opportunity, User organization) {
        if (opportunity.getTotalSlots() < 1) {
            throw new IllegalArgumentException("Total volunteer slots must be at least 1.");
        }
        if (opportunity.getEventDate() != null && opportunity.getEventDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past.");
        }

        opportunity.setStatus(OpportunityStatus.PENDING);
        opportunity.setOrganization(organization);

        Opportunity saved = opportunityRepository.save(opportunity);
        activityLogService.log(organization, "Organization posted new opportunity: " + saved.getTitle() + " (Pending Review)");
        return saved;
    }

    /**
     * Updates an opportunity's details. Resets status back to PENDING for re-review.
     */
    @Transactional
    public Opportunity updateOpportunity(Long id, Opportunity updatedData, User organization) {
        Opportunity existing = opportunityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found with id: " + id));

        if (!existing.getOrganization().getId().equals(organization.getId())) {
            throw new IllegalArgumentException("You are not authorized to edit this opportunity.");
        }

        if (updatedData.getTotalSlots() < 1) {
            throw new IllegalArgumentException("Total volunteer slots must be at least 1.");
        }
        if (updatedData.getEventDate() != null && updatedData.getEventDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past.");
        }

        existing.setTitle(updatedData.getTitle().trim());
        existing.setDescription(updatedData.getDescription().trim());
        existing.setLocation(updatedData.getLocation().trim());
        existing.setEventDate(updatedData.getEventDate());
        existing.setStartTime(updatedData.getStartTime());
        existing.setEndTime(updatedData.getEndTime());
        existing.setTotalSlots(updatedData.getTotalSlots());
        existing.setStatus(OpportunityStatus.PENDING);
        existing.setAdminRemark(null);

        Opportunity saved = opportunityRepository.save(existing);
        activityLogService.log(organization, "Organization updated opportunity: " + saved.getTitle());
        return saved;
    }

    /**
     * Deletes an opportunity belonging to the calling organization.
     */
    @Transactional
    public void deleteOpportunity(Long id, User organization) {
        Opportunity existing = opportunityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found with id: " + id));

        if (!existing.getOrganization().getId().equals(organization.getId())) {
            throw new IllegalArgumentException("You are not authorized to delete this opportunity.");
        }

        activityLogService.log(organization, "Organization deleted opportunity: " + existing.getTitle());
        opportunityRepository.delete(existing);
    }

    /**
     * Allows an Admin to review an opportunity (APPROVE or REJECT with a remark).
     */
    @Transactional
    public void reviewOpportunity(Long id, OpportunityStatus newStatus, String adminRemark, User admin) {
        Opportunity existing = opportunityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found with id: " + id));

        existing.setStatus(newStatus);
        existing.setAdminRemark(adminRemark != null ? adminRemark.trim() : null);

        opportunityRepository.save(existing);
        activityLogService.log(admin, "Admin reviewed opportunity [" + existing.getTitle() + "]: " + newStatus);
    }

    /**
     * Retrieves an opportunity by its database ID.
     */
    public Optional<Opportunity> findById(Long id) {
        return opportunityRepository.findById(id);
    }

    /**
     * Retrieves all opportunities currently in PENDING review state for the Admin.
     */
    public List<Opportunity> getPendingOpportunities() {
        return opportunityRepository.findByStatus(OpportunityStatus.PENDING);
    }

    /**
     * Retrieves all opportunities in the platform across all statuses.
     */
    public List<Opportunity> getAllOpportunities() {
        return opportunityRepository.findAll();
    }

    /**
     * Retrieves all opportunities posted by a specific Organization.
     */
    public List<Opportunity> getOpportunitiesByOrganization(User organization) {
        return opportunityRepository.findByOrganizationOrderByCreatedAtDesc(organization);
    }

    /**
     * Retrieves approved upcoming opportunities, filtered by optional search keyword.
     */
    public List<Opportunity> getUpcomingApprovedOpportunities(String query) {
        LocalDate today = LocalDate.now();
        if (query != null && !query.trim().isEmpty()) {
            return opportunityRepository.searchUpcomingApproved(OpportunityStatus.APPROVED, today, query.trim());
        }
        return opportunityRepository.findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(OpportunityStatus.APPROVED, today);
    }

    /**
     * Retrieves the top 6 upcoming approved opportunities for the public landing page.
     */
    public List<Opportunity> getTopUpcomingApprovedOpportunities() {
        return opportunityRepository.findTop6ByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
                OpportunityStatus.APPROVED, LocalDate.now());
    }

    /**
     * Counts the total number of opportunities by review status.
     */
    public long countByStatus(OpportunityStatus status) {
        return opportunityRepository.countByStatus(status);
    }

    /**
     * Counts the total number of opportunities posted by an organization.
     */
    public long countByOrganization(User organization) {
        return opportunityRepository.countByOrganization(organization);
    }

    /**
     * Counts opportunities of a specific status created by an organization.
     */
    public long countByOrganizationAndStatus(User organization, OpportunityStatus status) {
        return opportunityRepository.countByOrganizationAndStatus(organization, status);
    }
}

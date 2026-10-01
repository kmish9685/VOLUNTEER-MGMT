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
 * - All new opportunities go to PENDING status
 * - Organization delete permission
 * - Admin approval and rejection workflow with feedback remarks
 * - Filtering upcoming approved events for volunteers and public landing
 * =====================================================================
 */
@SuppressWarnings("null")
@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;

    /**
     * Constructor for injecting repository dependency.
     */
    public OpportunityService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
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

        return opportunityRepository.save(opportunity);
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
     * Retrieves all opportunities across all statuses for the Admin moderation list.
     */
    public List<Opportunity> getAllOpportunities() {
        return opportunityRepository.findAll();
    }

    /**
     * Retrieves all opportunities created by a specific organization.
     */
    public List<Opportunity> getOpportunitiesByOrganization(User organization) {
        return opportunityRepository.findByOrganizationOrderByCreatedAtDesc(organization);
    }

    /**
     * Retrieves all approved upcoming opportunities with optional search keyword.
     */
    public List<Opportunity> getUpcomingApprovedOpportunities(String keyword) {
        LocalDate today = LocalDate.now();
        if (keyword != null && !keyword.trim().isEmpty()) {
            return opportunityRepository.searchUpcomingApproved(OpportunityStatus.APPROVED, today, keyword.trim());
        }
        return opportunityRepository.findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
                OpportunityStatus.APPROVED, today);
    }

    /**
     * Fetches top upcoming approved opportunities for display on the landing page.
     */
    public List<Opportunity> getTopUpcomingApprovedOpportunities() {
        return opportunityRepository.findTop6ByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
                OpportunityStatus.APPROVED, LocalDate.now());
    }

    /**
     * Counts opportunities matching a specific status for dashboard KPIs.
     */
    public long countByStatus(OpportunityStatus status) {
        return opportunityRepository.countByStatus(status);
    }

    /**
     * Counts all opportunities created by an organization.
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

    /**
     * Counts active opportunities for an organization.
     */
    public long countActiveByOrganization(User organization) {
        return opportunityRepository.countByOrganizationAndStatus(organization, OpportunityStatus.APPROVED);
    }
}

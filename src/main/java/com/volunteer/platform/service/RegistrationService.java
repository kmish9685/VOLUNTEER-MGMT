package com.volunteer.platform.service;

import com.volunteer.platform.model.*;
import com.volunteer.platform.repository.OpportunityRepository;
import com.volunteer.platform.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * RegistrationService (Business Logic Layer)
 * ---------------------------------------------------------------------
 * Encapsulates all volunteer sign-up rules and attendance management:
 * - Prevents duplicate signups, full-capacity signups, and past event signups
 * - Allows volunteers to cancel registration before the event date
 * - Allows Organizations to mark attendance (ATTENDED / ABSENT) on/after event date
 * - Calculates remaining available slots per opportunity
 * =====================================================================
 */
@SuppressWarnings("null") // Spring Data JPA's findById(Long) is always called with non-null ids from path variables
@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final OpportunityRepository opportunityRepository;

    /**
     * Constructor for injecting repository dependencies.
     */
    public RegistrationService(RegistrationRepository registrationRepository,
                               OpportunityRepository opportunityRepository) {
        this.registrationRepository = registrationRepository;
        this.opportunityRepository = opportunityRepository;
    }

    /**
     * Enrolls a volunteer into an approved opportunity after validating all business constraints.
     */
    @Transactional
    public Registration signUp(Long opportunityId, User volunteer) {
        if (volunteer.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("Your account is blocked. You cannot sign up for events.");
        }

        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found with id: " + opportunityId));

        if (opportunity.getStatus() != OpportunityStatus.APPROVED) {
            throw new IllegalStateException("You can only sign up for approved opportunities.");
        }

        if (opportunity.getEventDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot sign up for events that have already taken place.");
        }

        long activeCount = registrationRepository.countByOpportunityAndStatusNot(opportunity, RegistrationStatus.CANCELLED);
        if (activeCount >= opportunity.getTotalSlots()) {
            throw new IllegalStateException("This opportunity has reached its maximum volunteer capacity.");
        }

        Optional<Registration> existingOpt = registrationRepository.findByVolunteerAndOpportunity(volunteer, opportunity);
        if (existingOpt.isPresent()) {
            Registration existing = existingOpt.get();
            if (existing.getStatus() != RegistrationStatus.CANCELLED) {
                throw new IllegalStateException("You are already registered for this opportunity.");
            }
            // Re-activate previously cancelled registration
            existing.setStatus(RegistrationStatus.REGISTERED);
            existing.setRegisteredAt(LocalDateTime.now());
            return registrationRepository.save(existing);
        }

        Registration newRegistration = new Registration(volunteer, opportunity, RegistrationStatus.REGISTERED);
        return registrationRepository.save(newRegistration);
    }

    /**
     * Allows a volunteer to withdraw/cancel their registration prior to the event date.
     */
    @Transactional
    public void cancelRegistration(Long registrationId, User volunteer) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + registrationId));

        if (!registration.getVolunteer().getId().equals(volunteer.getId())) {
            throw new IllegalArgumentException("You are not authorized to cancel this registration.");
        }

        if (registration.getOpportunity().getEventDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel registration for past events.");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
    }

    /**
     * Allows hosting organization to record attendance (ATTENDED or ABSENT) on or after event date.
     */
    @Transactional
    public void markAttendance(Long registrationId, RegistrationStatus newStatus, User organization) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + registrationId));

        if (!registration.getOpportunity().getOrganization().getId().equals(organization.getId())) {
            throw new IllegalArgumentException("You are not authorized to mark attendance for this event.");
        }

        if (registration.getOpportunity().getEventDate().isAfter(LocalDate.now())) {
            throw new IllegalStateException("Attendance can only be recorded on or after the scheduled event date.");
        }

        if (newStatus != RegistrationStatus.ATTENDED && newStatus != RegistrationStatus.ABSENT) {
            throw new IllegalArgumentException("Status must be set to either ATTENDED or ABSENT.");
        }

        registration.setStatus(newStatus);
        registrationRepository.save(registration);
    }

    /**
     * Calculates the remaining available slots for an opportunity.
     */
    public int getRemainingSlots(Opportunity opportunity) {
        long activeCount = registrationRepository.countByOpportunityAndStatusNot(opportunity, RegistrationStatus.CANCELLED);
        int remaining = opportunity.getTotalSlots() - (int) activeCount;
        return Math.max(0, remaining);
    }

    /**
     * Checks if a volunteer currently has an active signup for an event.
     */
    public boolean hasActiveRegistration(User volunteer, Opportunity opportunity) {
        return registrationRepository.existsByVolunteerAndOpportunityAndStatusNot(
                volunteer, opportunity, RegistrationStatus.CANCELLED);
    }

    /**
     * Retrieves all registrations for a specific volunteer.
     */
    public List<Registration> getRegistrationsByVolunteer(User volunteer) {
        return registrationRepository.findByVolunteerOrderByRegisteredAtDesc(volunteer);
    }

    /**
     * Retrieves all registrations for an opportunity.
     */
    public List<Registration> getRegistrationsForOpportunity(Opportunity opportunity) {
        return registrationRepository.findByOpportunityOrderByRegisteredAtDesc(opportunity);
    }

    /**
     * Alias for getRegistrationsForOpportunity.
     */
    public List<Registration> getRegistrationsByOpportunity(Opportunity opportunity) {
        return registrationRepository.findByOpportunityOrderByRegisteredAtDesc(opportunity);
    }

    /**
     * Counts registrations for an organization's events.
     */
    public long countByOrganization(User organization) {
        return registrationRepository.countByOrganization(organization);
    }

    /**
     * Counts registrations matching an opportunity and specific status.
     */
    public long countByOpportunityAndStatus(Opportunity opportunity, RegistrationStatus status) {
        return registrationRepository.countByOpportunityAndStatus(opportunity, status);
    }

    /**
     * Counts total registrations for a volunteer by status.
     */
    public long countByVolunteerAndStatus(User volunteer, RegistrationStatus status) {
        return registrationRepository.countByVolunteerAndStatus(volunteer, status);
    }

    /**
     * Counts total signups across all opportunities on the platform.
     */
    public long countTotalRegistrations() {
        return registrationRepository.count();
    }

    /**
     * Retrieves all platform registrations for Admin monitoring.
     */
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAllByOrderByRegisteredAtDesc();
    }
}

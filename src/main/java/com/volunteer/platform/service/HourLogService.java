package com.volunteer.platform.service;

import com.volunteer.platform.model.*;
import com.volunteer.platform.repository.HourLogRepository;
import com.volunteer.platform.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * HourLogService (Business Logic Layer)
 * ---------------------------------------------------------------------
 * Handles volunteer service hour submissions and organization approvals:
 * - Only permits hour submissions for registrations marked ATTENDED
 * - Enforces the "max_hours_per_log" system setting constraint
 * - Allows Organizations to review (APPROVE / REJECT) submitted hours
 * - Aggregates approved hours for platform, organization, and volunteer stats
 * =====================================================================
 */
@Service
public class HourLogService {

    private final HourLogRepository hourLogRepository;
    private final RegistrationRepository registrationRepository;
    private final SettingService settingService;
    private final ActivityLogService activityLogService;

    /**
     * Constructor injecting repositories, setting service, and audit service.
     */
    public HourLogService(HourLogRepository hourLogRepository,
                          RegistrationRepository registrationRepository,
                          SettingService settingService,
                          ActivityLogService activityLogService) {
        this.hourLogRepository = hourLogRepository;
        this.registrationRepository = registrationRepository;
        this.settingService = settingService;
        this.activityLogService = activityLogService;
    }

    /**
     * Submits service hours logged by a volunteer who attended an event.
     */
    @Transactional
    public HourLog logHours(Long registrationId, double hours, String workDescription, User volunteer) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found with id: " + registrationId));

        if (!registration.getVolunteer().getId().equals(volunteer.getId())) {
            throw new IllegalArgumentException("You are not authorized to log hours for this registration.");
        }

        if (registration.getStatus() != RegistrationStatus.ATTENDED) {
            throw new IllegalStateException("Hours can only be logged for events where your attendance was marked ATTENDED.");
        }

        if (hours <= 0) {
            throw new IllegalArgumentException("Logged hours must be greater than 0.");
        }

        int maxHours = settingService.getInt("max_hours_per_log", 12);
        if (hours > maxHours) {
            throw new IllegalArgumentException("Logged hours cannot exceed the system limit of " + maxHours + " hours per entry.");
        }

        Optional<HourLog> existingOpt = hourLogRepository.findFirstByRegistrationOrderByLoggedAtDesc(registration);
        HourLog logEntry;
        if (existingOpt.isPresent()) {
            logEntry = existingOpt.get();
            if (logEntry.getStatus() == HourLogStatus.APPROVED) {
                throw new IllegalStateException("Hours for this event have already been approved and cannot be modified.");
            }
            logEntry.setHours(hours);
            logEntry.setWorkDescription(workDescription != null ? workDescription.trim() : null);
            logEntry.setStatus(HourLogStatus.PENDING);
            logEntry.setLoggedAt(LocalDateTime.now());
        } else {
            logEntry = new HourLog(registration, hours, workDescription != null ? workDescription.trim() : null, HourLogStatus.PENDING);
        }

        HourLog saved = hourLogRepository.save(logEntry);
        activityLogService.log(volunteer, "Volunteer logged " + hours + " hours for: " +
                registration.getOpportunity().getTitle());
        return saved;
    }

    /**
     * Allows hosting organization to APPROVE or REJECT a submitted hour log entry.
     */
    @Transactional
    public void reviewHours(Long hourLogId, HourLogStatus newStatus, User organization) {
        HourLog logEntry = hourLogRepository.findById(hourLogId)
                .orElseThrow(() -> new IllegalArgumentException("Hour log not found with id: " + hourLogId));

        User eventOrg = logEntry.getRegistration().getOpportunity().getOrganization();
        if (!eventOrg.getId().equals(organization.getId())) {
            throw new IllegalArgumentException("You are not authorized to review hours for this event.");
        }

        if (newStatus != HourLogStatus.APPROVED && newStatus != HourLogStatus.REJECTED) {
            throw new IllegalArgumentException("Status must be set to either APPROVED or REJECTED.");
        }

        logEntry.setStatus(newStatus);
        hourLogRepository.save(logEntry);
        activityLogService.log(organization, "Organization " + newStatus + " " + logEntry.getHours() +
                " hours for volunteer: " + logEntry.getRegistration().getVolunteer().getFullName());
    }

    /**
     * Finds the existing hour log for a registration if one has been submitted.
     */
    public Optional<HourLog> getHourLogForRegistration(Registration registration) {
        return hourLogRepository.findFirstByRegistrationOrderByLoggedAtDesc(registration);
    }

    /**
     * Retrieves all pending hour logs for events belonging to an organization.
     */
    public List<HourLog> getPendingHoursForOrganization(User organization) {
        return hourLogRepository.findByOrganizationAndStatus(organization, HourLogStatus.PENDING);
    }

    /**
     * Retrieves all hour logs submitted by a volunteer.
     */
    public List<HourLog> getHoursByVolunteer(User volunteer) {
        return hourLogRepository.findByRegistrationVolunteerOrderByLoggedAtDesc(volunteer);
    }

    /**
     * Counts pending hour submissions awaiting an organization's review.
     */
    public long countPendingHoursForOrganization(User organization) {
        return hourLogRepository.countByOrganizationAndStatus(organization, HourLogStatus.PENDING);
    }

    /**
     * Calculates total sum of approved volunteer service hours across the platform.
     */
    public double getTotalApprovedHoursPlatform() {
        return hourLogRepository.sumHoursByStatus(HourLogStatus.APPROVED);
    }

    /**
     * Calculates total sum of approved volunteer service hours for a specific volunteer.
     */
    public double getTotalApprovedHoursForVolunteer(User volunteer) {
        return hourLogRepository.sumHoursByVolunteerAndStatus(volunteer, HourLogStatus.APPROVED);
    }

    /**
     * Calculates total sum of approved volunteer service hours generated by an organization.
     */
    public double getTotalApprovedHoursForOrganization(User organization) {
        return hourLogRepository.sumHoursByOrganizationAndStatus(organization, HourLogStatus.APPROVED);
    }

    /**
     * Calculates total sum of approved volunteer hours generated by a single opportunity event.
     */
    public double getTotalApprovedHoursForOpportunity(Opportunity opportunity) {
        return hourLogRepository.sumHoursByOpportunityAndStatus(opportunity, HourLogStatus.APPROVED);
    }
}

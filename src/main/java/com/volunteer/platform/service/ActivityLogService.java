package com.volunteer.platform.service;

import com.volunteer.platform.model.ActivityLog;
import com.volunteer.platform.model.User;
import com.volunteer.platform.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * =====================================================================
 * ActivityLogService (Business Logic Layer)
 * ---------------------------------------------------------------------
 * Centralizes system-wide audit logging. Whenever critical events occur
 * (such as registrations, opportunity postings, approvals, attendance,
 * and hour logging), this service records an entry into the activity log table.
 * =====================================================================
 */
@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    /**
     * Constructor injecting the ActivityLogRepository dependency.
     */
    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * Records a new audit log action associated with an optional user.
     */
    @Transactional
    public void log(User user, String action) {
        ActivityLog logEntry = new ActivityLog(user, action);
        activityLogRepository.save(logEntry);
    }

    /**
     * Retrieves the 10 most recent activity logs for display on the Admin dashboard.
     */
    public List<ActivityLog> getLatestLogs() {
        return activityLogRepository.findTop10ByOrderByCreatedAtDesc();
    }

    /**
     * Retrieves all activity logs ordered from newest to oldest for the full audit page.
     */
    public List<ActivityLog> getAllLogs() {
        return activityLogRepository.findAllByOrderByCreatedAtDesc();
    }
}

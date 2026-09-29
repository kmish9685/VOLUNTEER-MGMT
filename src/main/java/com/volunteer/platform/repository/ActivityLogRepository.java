package com.volunteer.platform.repository;

import com.volunteer.platform.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * ActivityLogRepository (Data Access Layer)
 * ---------------------------------------------------------------------
 * Handles database operations for the audit activity logs.
 * Used by the Admin to view real-time platform events and recent actions.
 * =====================================================================
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Retrieves the 10 most recent activity logs for display on the Admin dashboard.
     */
    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();

    /**
     * Retrieves all activity logs ordered newest to oldest for the full activity log page.
     */
    List<ActivityLog> findAllByOrderByCreatedAtDesc();
}

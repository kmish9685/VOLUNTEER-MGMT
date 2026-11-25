package com.volunteer.platform.jdbc;

import java.time.LocalDate;

/**
 * =====================================================================
 * ParticipationReportRow (Data Transfer Object)
 * ---------------------------------------------------------------------
 * One row of the Organization's participation report: one event and its
 * counts. Filled from a plain JDBC ResultSet by ParticipationReportDao.
 * =====================================================================
 */
public class ParticipationReportRow {

    private final Long opportunityId;
    private final String title;
    private final String location;
    private final LocalDate eventDate;
    private final int totalSlots;
    private final long registeredCount;
    private final long attendedCount;
    private final long absentCount;
    private final double totalHours;

    /**
     * Creates a report row with all values read from the database.
     */
    public ParticipationReportRow(Long opportunityId, String title, String location, LocalDate eventDate,
                                  int totalSlots, long registeredCount, long attendedCount,
                                  long absentCount, double totalHours) {
        this.opportunityId = opportunityId;
        this.title = title;
        this.location = location;
        this.eventDate = eventDate;
        this.totalSlots = totalSlots;
        this.registeredCount = registeredCount;
        this.attendedCount = attendedCount;
        this.absentCount = absentCount;
        this.totalHours = totalHours;
    }

    public Long getOpportunityId() { return opportunityId; }

    public String getTitle() { return title; }

    public String getLocation() { return location; }

    public LocalDate getEventDate() { return eventDate; }

    public int getTotalSlots() { return totalSlots; }

    public long getRegisteredCount() { return registeredCount; }

    public long getAttendedCount() { return attendedCount; }

    public long getAbsentCount() { return absentCount; }

    public double getTotalHours() { return totalHours; }
}

package com.volunteer.platform.jdbc;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * =====================================================================
 * ParticipationReportDao (Data Access Layer - plain JDBC)
 * ---------------------------------------------------------------------
 * Most of the app uses Spring Data JPA. This class shows DIRECT JDBC:
 *
 *   DataSource -> Connection -> PreparedStatement -> ResultSet
 *
 * It builds the Organization participation report with ONE SQL query
 * (GROUP BY), instead of running 4 separate count queries per event.
 *
 * - PreparedStatement with "?" placeholders prevents SQL injection.
 * - try-with-resources closes the ResultSet, statement and connection
 *   automatically, even if an exception happens.
 * =====================================================================
 */
@Repository
public class ParticipationReportDao {

    /**
     * One query: for each event of the organization, count registrations by status
     * and add up the logged hours (sub-query, so hours are never double-counted).
     */
    private static final String REPORT_SQL =
            "SELECT o.id, o.title, o.location, o.event_date, o.total_slots, " +
            "       COALESCE(SUM(CASE WHEN r.status IN ('REGISTERED', 'ATTENDED', 'ABSENT') THEN 1 ELSE 0 END), 0) AS registered_count, " +
            "       COALESCE(SUM(CASE WHEN r.status = 'ATTENDED' THEN 1 ELSE 0 END), 0) AS attended_count, " +
            "       COALESCE(SUM(CASE WHEN r.status = 'ABSENT' THEN 1 ELSE 0 END), 0) AS absent_count, " +
            "       (SELECT COALESCE(SUM(h.hours), 0) FROM hour_logs h " +
            "          JOIN registrations r2 ON h.registration_id = r2.id " +
            "         WHERE r2.opportunity_id = o.id) AS total_hours " +
            "FROM opportunities o " +
            "LEFT JOIN registrations r ON r.opportunity_id = o.id " +
            "WHERE o.organization_id = ? " +
            "GROUP BY o.id, o.title, o.location, o.event_date, o.total_slots, o.created_at " +
            "ORDER BY o.created_at DESC";

    private final DataSource dataSource;

    /**
     * Spring injects the same connection pool (HikariCP) that JPA uses.
     */
    public ParticipationReportDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Returns one report row per event posted by the given organization.
     *
     * @param organizationId id of the organization user (app_users.id)
     * @return list of rows, newest event first (empty list if no events)
     */
    public List<ParticipationReportRow> findReportByOrganization(Long organizationId) {
        List<ParticipationReportRow> rows = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(REPORT_SQL)) {

            statement.setLong(1, organizationId); // fills the "?" safely

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    rows.add(new ParticipationReportRow(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("location"),
                            rs.getObject("event_date", LocalDate.class),
                            rs.getInt("total_slots"),
                            rs.getLong("registered_count"),
                            rs.getLong("attended_count"),
                            rs.getLong("absent_count"),
                            rs.getDouble("total_hours")));
                }
            }
        } catch (SQLException ex) {
            // Turn the checked SQLException into an unchecked one with a clear message
            throw new IllegalStateException("Could not load participation report: " + ex.getMessage(), ex);
        }
        return rows;
    }
}

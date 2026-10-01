package com.volunteer.platform.controller;

import com.volunteer.platform.model.*;
import com.volunteer.platform.service.HourLogService;
import com.volunteer.platform.service.OpportunityService;
import com.volunteer.platform.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * =====================================================================
 * OrganizationController (Presentation Layer)
 * ---------------------------------------------------------------------
 * Handles all actions for hosting Organizations (/org/**):
 * - Org Dashboard with simple counts
 * - Opportunity CRUD (Post, Edit, Delete events)
 * - Volunteer Attendance Marking (ATTENDED / ABSENT on/after event date)
 * - Participation Summary Report with Print button
 * - Messages with registered volunteers
 * =====================================================================
 */
@Controller
@RequestMapping("/org")
public class OrganizationController {

    private final OpportunityService opportunityService;
    private final RegistrationService registrationService;
    private final HourLogService hourLogService;

    /**
     * Injects OpportunityService, RegistrationService, and HourLogService.
     */
    public OrganizationController(OpportunityService opportunityService,
                                  RegistrationService registrationService,
                                  HourLogService hourLogService) {
        this.opportunityService = opportunityService;
        this.registrationService = registrationService;
        this.hourLogService = hourLogService;
    }

    /**
     * Displays the Organization Dashboard with simple KPI counts.
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User org = (User) session.getAttribute("loggedInUser");
        model.addAttribute("totalOpportunities", opportunityService.countByOrganization(org));
        model.addAttribute("approvedOpportunities", opportunityService.countByOrganizationAndStatus(org, OpportunityStatus.APPROVED));
        model.addAttribute("pendingOpportunities", opportunityService.countByOrganizationAndStatus(org, OpportunityStatus.PENDING));
        model.addAttribute("totalVolunteersRegistered", registrationService.countByOrganization(org));
        model.addAttribute("totalHours", hourLogService.getTotalHoursForOrganization(org));
        return "organization/dashboard";
    }

    /**
     * Lists all opportunities created by this organization.
     */
    @GetMapping("/opportunities")
    public String listOpportunities(HttpSession session, Model model) {
        User org = (User) session.getAttribute("loggedInUser");
        model.addAttribute("opportunities", opportunityService.getOpportunitiesByOrganization(org));
        return "organization/opportunities";
    }

    /**
     * Displays the form to create a new volunteering opportunity.
     */
    @GetMapping("/opportunities/new")
    public String showCreateOpportunityForm(Model model) {
        model.addAttribute("opportunity", new Opportunity());
        return "organization/opportunity-form";
    }

    /**
     * Processes creation of a new opportunity.
     */
    @PostMapping("/opportunities/new")
    public String processCreateOpportunity(@ModelAttribute("opportunity") Opportunity opportunity,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        try {
            User org = (User) session.getAttribute("loggedInUser");
            opportunityService.createOpportunity(opportunity, org);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity submitted for Admin review!");
            return "redirect:/org/opportunities";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/org/opportunities/new";
        }
    }

    /**
     * Deletes an opportunity belonging to this organization.
     */
    @PostMapping("/opportunities/delete/{id}")
    public String deleteOpportunity(@PathVariable("id") Long id,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            User org = (User) session.getAttribute("loggedInUser");
            opportunityService.deleteOpportunity(id, org);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/org/opportunities";
    }

    /**
     * Displays the roster of registered volunteers for an opportunity to mark attendance.
     */
    @GetMapping("/volunteers/{opportunityId}")
    public String showRegisteredVolunteers(@PathVariable("opportunityId") Long opportunityId,
                                           HttpSession session,
                                           Model model,
                                           RedirectAttributes redirectAttributes) {
        User org = (User) session.getAttribute("loggedInUser");
        return opportunityService.findById(opportunityId).map(opp -> {
            if (!opp.getOrganization().getId().equals(org.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Access denied.");
                return "redirect:/org/opportunities";
            }
            model.addAttribute("opportunity", opp);
            model.addAttribute("registrations", registrationService.getRegistrationsByOpportunity(opp));
            model.addAttribute("isEventPastOrToday", !opp.getEventDate().isAfter(LocalDate.now()));
            return "organization/volunteers";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Opportunity not found.");
            return "redirect:/org/opportunities";
        });
    }

    /**
     * Marks a volunteer's attendance as ATTENDED or ABSENT.
     */
    @PostMapping("/attendance/mark")
    public String markAttendance(@RequestParam("registrationId") Long registrationId,
                                 @RequestParam("opportunityId") Long opportunityId,
                                 @RequestParam("status") RegistrationStatus status,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            User org = (User) session.getAttribute("loggedInUser");
            registrationService.markAttendance(registrationId, status, org);
            redirectAttributes.addFlashAttribute("successMessage", "Attendance updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/org/volunteers/" + opportunityId;
    }

    /**
     * Generates a participation summary report per event with Print button.
     */
    @GetMapping("/report")
    public String showParticipationReport(HttpSession session, Model model) {
        User org = (User) session.getAttribute("loggedInUser");
        List<Opportunity> opportunities = opportunityService.getOpportunitiesByOrganization(org);

        List<Map<String, Object>> reportRows = new ArrayList<>();
        for (Opportunity opp : opportunities) {
            Map<String, Object> row = new HashMap<>();
            row.put("opportunity", opp);
            row.put("totalSlots", opp.getTotalSlots());
            row.put("registeredCount", registrationService.countByOpportunityAndStatus(opp, RegistrationStatus.REGISTERED)
                    + registrationService.countByOpportunityAndStatus(opp, RegistrationStatus.ATTENDED)
                    + registrationService.countByOpportunityAndStatus(opp, RegistrationStatus.ABSENT));
            row.put("attendedCount", registrationService.countByOpportunityAndStatus(opp, RegistrationStatus.ATTENDED));
            row.put("absentCount", registrationService.countByOpportunityAndStatus(opp, RegistrationStatus.ABSENT));
            row.put("totalHours", hourLogService.getTotalHoursForOpportunity(opp));
            reportRows.add(row);
        }

        model.addAttribute("reportRows", reportRows);
        return "organization/report";
    }
}

package com.volunteer.platform.controller;

import com.volunteer.platform.model.*;
import com.volunteer.platform.service.HourLogService;
import com.volunteer.platform.service.OpportunityService;
import com.volunteer.platform.service.RegistrationService;
import com.volunteer.platform.service.SettingService;
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
 * VolunteerController (Presentation Layer)
 * ---------------------------------------------------------------------
 * Handles all volunteer actions (/volunteer/**):
 * - Volunteer Dashboard (total hours, registrations, upcoming events)
 * - Browse & Search approved upcoming opportunities
 * - Detailed opportunity view with capacity and organization bio
 * - Sign-up and Cancellation workflows
 * - Hour logging for attended events (hours count immediately)
 * - Complete participation history with total hours
 * =====================================================================
 */
@Controller
@RequestMapping("/volunteer")
public class VolunteerController {

    private final OpportunityService opportunityService;
    private final RegistrationService registrationService;
    private final HourLogService hourLogService;
    private final SettingService settingService;

    /**
     * Injects OpportunityService, RegistrationService, HourLogService, and SettingService.
     */
    public VolunteerController(OpportunityService opportunityService,
                               RegistrationService registrationService,
                               HourLogService hourLogService,
                               SettingService settingService) {
        this.opportunityService = opportunityService;
        this.registrationService = registrationService;
        this.hourLogService = hourLogService;
        this.settingService = settingService;
    }

    /**
     * Displays the Volunteer Dashboard with simple counts and upcoming events.
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User volunteer = (User) session.getAttribute("loggedInUser");
        model.addAttribute("totalHours", hourLogService.getTotalHoursForVolunteer(volunteer));
        model.addAttribute("totalEventsRegistered", registrationService.countByVolunteerAndStatus(volunteer, RegistrationStatus.REGISTERED));
        model.addAttribute("totalEventsAttended", registrationService.countByVolunteerAndStatus(volunteer, RegistrationStatus.ATTENDED));

        List<Registration> myRegistrations = registrationService.getRegistrationsByVolunteer(volunteer);
        List<Registration> upcoming = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Registration reg : myRegistrations) {
            if (reg.getStatus() == RegistrationStatus.REGISTERED && !reg.getOpportunity().getEventDate().isBefore(today)) {
                upcoming.add(reg);
            }
        }
        model.addAttribute("upcomingRegistrations", upcoming);
        return "volunteer/dashboard";
    }

    /**
     * Displays the opportunity browser for approved upcoming events.
     */
    @GetMapping("/browse")
    public String browseOpportunities(@RequestParam(value = "search", required = false) String search,
                                      Model model) {
        List<Opportunity> opportunities = opportunityService.getUpcomingApprovedOpportunities(search);
        List<Map<String, Object>> cards = new ArrayList<>();

        for (Opportunity opp : opportunities) {
            Map<String, Object> map = new HashMap<>();
            map.put("opportunity", opp);
            map.put("remainingSlots", registrationService.getRemainingSlots(opp));
            cards.add(map);
        }

        model.addAttribute("opportunityCards", cards);
        model.addAttribute("searchQuery", search);
        return "volunteer/browse";
    }

    /**
     * Displays detailed view of an opportunity with hosting organization details.
     */
    @GetMapping("/opportunity/{id}")
    public String showOpportunityDetails(@PathVariable("id") Long id,
                                         HttpSession session,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        User volunteer = (User) session.getAttribute("loggedInUser");
        return opportunityService.findById(id).map(opp -> {
            model.addAttribute("opportunity", opp);
            model.addAttribute("remainingSlots", registrationService.getRemainingSlots(opp));
            model.addAttribute("isRegistered", registrationService.hasActiveRegistration(volunteer, opp));
            model.addAttribute("isPastEvent", opp.getEventDate().isBefore(LocalDate.now()));
            return "volunteer/opportunity-details";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Opportunity not found.");
            return "redirect:/volunteer/browse";
        });
    }

    /**
     * Processes volunteer sign up for an opportunity.
     */
    @PostMapping("/signup/{id}")
    public String processSignUp(@PathVariable("id") Long opportunityId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            User volunteer = (User) session.getAttribute("loggedInUser");
            registrationService.signUp(opportunityId, volunteer);
            redirectAttributes.addFlashAttribute("successMessage", "Successfully signed up for the event!");
            return "redirect:/volunteer/dashboard";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/volunteer/opportunity/" + opportunityId;
        }
    }

    /**
     * Allows a volunteer to cancel their registration prior to the event date.
     */
    @PostMapping("/cancel/{registrationId}")
    public String processCancel(@PathVariable("registrationId") Long registrationId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            User volunteer = (User) session.getAttribute("loggedInUser");
            registrationService.cancelRegistration(registrationId, volunteer);
            redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/volunteer/dashboard";
    }

    /**
     * Displays the form for a volunteer to log hours for an attended event.
     */
    @GetMapping("/log-hours/{registrationId}")
    public String showLogHoursForm(@PathVariable("registrationId") Long registrationId,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User volunteer = (User) session.getAttribute("loggedInUser");
        return registrationService.getRegistrationsByVolunteer(volunteer).stream()
                .filter(r -> r.getId().equals(registrationId))
                .findFirst()
                .map(reg -> {
                    if (reg.getStatus() != RegistrationStatus.ATTENDED) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "You can only log hours for events marked ATTENDED.");
                        return "redirect:/volunteer/history";
                    }
                    model.addAttribute("registration", reg);
                    model.addAttribute("existingLog", hourLogService.getHourLogForRegistration(reg).orElse(null));
                    model.addAttribute("maxHoursAllowed", settingService.getInt("max_hours_per_log", 12));
                    return "volunteer/log-hours";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Registration not found.");
                    return "redirect:/volunteer/history";
                });
    }

    /**
     * Processes submission of volunteer service hours.
     * Hours are recorded immediately — no approval step required.
     */
    @PostMapping("/log-hours")
    public String processLogHours(@RequestParam("registrationId") Long registrationId,
                                 @RequestParam("hours") double hours,
                                 @RequestParam("workDescription") String workDescription,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            User volunteer = (User) session.getAttribute("loggedInUser");
            hourLogService.logHours(registrationId, hours, workDescription, volunteer);
            redirectAttributes.addFlashAttribute("successMessage", "Hours logged successfully!");
            return "redirect:/volunteer/history";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/volunteer/log-hours/" + registrationId;
        }
    }

    /**
     * Displays complete participation history with total hours.
     */
    @GetMapping("/history")
    public String showParticipationHistory(HttpSession session, Model model) {
        User volunteer = (User) session.getAttribute("loggedInUser");
        List<Registration> registrations = registrationService.getRegistrationsByVolunteer(volunteer);

        List<Map<String, Object>> historyRows = new ArrayList<>();
        for (Registration reg : registrations) {
            Map<String, Object> row = new HashMap<>();
            row.put("registration", reg);
            row.put("hourLog", hourLogService.getHourLogForRegistration(reg).orElse(null));
            historyRows.add(row);
        }

        model.addAttribute("historyRows", historyRows);
        model.addAttribute("totalHours", hourLogService.getTotalHoursForVolunteer(volunteer));
        return "volunteer/history";
    }
}

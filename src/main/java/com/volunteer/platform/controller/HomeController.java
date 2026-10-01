package com.volunteer.platform.controller;

import com.volunteer.platform.service.HourLogService;
import com.volunteer.platform.service.OpportunityService;
import com.volunteer.platform.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * =====================================================================
 * HomeController (Presentation Layer)
 * ---------------------------------------------------------------------
 * Handles public landing page requests (/).
 * Fetches featured upcoming volunteering events and high-level platform
 * statistics (volunteers, organizations, total community hours).
 * =====================================================================
 */
@Controller
public class HomeController {

    private final OpportunityService opportunityService;
    private final UserService userService;
    private final HourLogService hourLogService;

    /**
     * Injects services for opportunities, users, and hours.
     */
    public HomeController(OpportunityService opportunityService,
                          UserService userService,
                          HourLogService hourLogService) {
        this.opportunityService = opportunityService;
        this.userService = userService;
        this.hourLogService = hourLogService;
    }

    /**
     * Renders the public landing page with featured upcoming events and platform statistics.
     */
    @GetMapping("/")
    public String showLandingPage(Model model) {
        model.addAttribute("featuredOpportunities", opportunityService.getTopUpcomingApprovedOpportunities());
        model.addAttribute("totalVolunteers", userService.countVolunteers());
        model.addAttribute("totalOrganizations", userService.countOrganizations());
        model.addAttribute("totalHours", hourLogService.getTotalHoursPlatform());
        return "index";
    }
}

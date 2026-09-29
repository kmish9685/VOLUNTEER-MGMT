package com.volunteer.platform.controller;

import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import com.volunteer.platform.service.SettingService;
import com.volunteer.platform.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * =====================================================================
 * AuthController (Presentation Layer)
 * ---------------------------------------------------------------------
 * Manages user authentication:
 * - User login (credential verification & session establishment)
 * - Self-service registration for Volunteers and Organizations
 * - Session invalidation upon logout
 * - Intelligent role redirection to respective dashboards
 * =====================================================================
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final SettingService settingService;

    /**
     * Injects UserService and SettingService.
     */
    public AuthController(UserService userService, SettingService settingService) {
        this.userService = userService;
        this.settingService = settingService;
    }

    /**
     * Displays the login page. If already authenticated, redirects straight to dashboard.
     */
    @GetMapping("/login")
    public String showLoginPage(HttpSession session, Model model,
                                @RequestParam(value = "error", required = false) String errorParam) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return getDashboardRedirect(loggedInUser.getRole());
        }

        if ("auth_required".equals(errorParam)) {
            model.addAttribute("errorMessage", "Please log in to access this page.");
        } else if ("account_blocked".equals(errorParam)) {
            model.addAttribute("errorMessage", "Your account has been blocked. Please contact the administrator.");
        }

        return "auth/login";
    }

    /**
     * Processes login credentials and stores the authenticated user in HttpSession.
     */
    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.authenticate(email, password);
            session.setAttribute("loggedInUser", user);
            redirectAttributes.addFlashAttribute("successMessage", "Welcome back, " + user.getFullName() + "!");
            return getDashboardRedirect(user.getRole());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Displays the registration form. Checks if the platform currently allows new registrations.
     */
    @GetMapping("/register")
    public String showRegisterPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            return getDashboardRedirect(loggedInUser.getRole());
        }

        boolean allowRegistrations = settingService.getBoolean("allow_registrations", true);
        model.addAttribute("allowRegistrations", allowRegistrations);
        return "auth/register";
    }

    /**
     * Processes new user registration for Volunteer or Organization roles.
     */
    @PostMapping("/register")
    public String processRegister(@RequestParam("fullName") String fullName,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  @RequestParam("role") Role role,
                                  @RequestParam(value = "organizationDescription", required = false) String orgDesc,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(fullName, email, password, phone, role, orgDesc);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! Please log in with your email and password.");
            return "redirect:/login";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Invalidates the user session and redirects to the login screen.
     */
    @GetMapping("/logout")
    public String processLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out successfully.");
        return "redirect:/login";
    }

    /**
     * Determines the appropriate destination dashboard based on the user's role.
     */
    private String getDashboardRedirect(Role role) {
        if (role == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        } else if (role == Role.ORGANIZATION) {
            return "redirect:/org/dashboard";
        } else if (role == Role.VOLUNTEER) {
            return "redirect:/volunteer/dashboard";
        }
        return "redirect:/login";
    }
}

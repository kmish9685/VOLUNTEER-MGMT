package com.volunteer.platform.config;

import com.volunteer.platform.model.User;
import com.volunteer.platform.service.MessageService;
import com.volunteer.platform.service.SettingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * =====================================================================
 * GlobalModelAttributes (Presentation / Advice Layer)
 * ---------------------------------------------------------------------
 * A Spring MVC @ControllerAdvice that binds common attributes to all
 * Thymeleaf view models automatically:
 * - platformName: Read dynamically from the system_settings table
 * - currentUser: The currently authenticated User in HttpSession
 * - unreadCount: Total unread direct messages for the current user
 * =====================================================================
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final SettingService settingService;
    private final MessageService messageService;

    /**
     * Injects the SettingService and MessageService dependencies.
     */
    public GlobalModelAttributes(SettingService settingService, MessageService messageService) {
        this.settingService = settingService;
        this.messageService = messageService;
    }

    /**
     * Exposes the dynamic platform name to every template (e.g. for navbar and <title>).
     */
    @ModelAttribute("platformName")
    public String getPlatformName() {
        return settingService.getValue("platform_name", "VolunteerHub");
    }

    /**
     * Exposes the currently logged-in user object from the HTTP session.
     */
    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    /**
     * Exposes the unread messages count for displaying the notification badge in the navbar.
     */
    @ModelAttribute("unreadCount")
    public long getUnreadCount(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            return messageService.getUnreadMessageCount(user);
        }
        return 0;
    }
}

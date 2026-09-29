package com.volunteer.platform.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * =====================================================================
 * CustomErrorController (Presentation Layer)
 * ---------------------------------------------------------------------
 * Replaces the default Spring Whitelabel error page with a clean, friendly
 * HTML error page (error.html) handling 404 Not Found, 403 Forbidden,
 * and 500 Internal Server errors gracefully.
 * =====================================================================
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handles /error requests and provides friendly error details to the view.
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = 500;
        String title = "Unexpected Error";
        String message = "An unexpected error occurred while processing your request.";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                title = "Page Not Found (404)";
                message = "The page or resource you requested does not exist or has been moved.";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                title = "Access Denied (403)";
                message = "You do not have permission to access this resource.";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                title = "Internal Server Error (500)";
                message = "The server encountered an error. Please try again later.";
            }
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorTitle", title);
        model.addAttribute("errorMessage", message);
        return "error";
    }
}

package com.volunteer.platform.controller;

import com.volunteer.platform.model.Message;
import com.volunteer.platform.model.User;
import com.volunteer.platform.service.MessageService;
import com.volunteer.platform.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

/**
 * =====================================================================
 * MessageController (Presentation Layer)
 * ---------------------------------------------------------------------
 * Handles 1-on-1 direct messaging between Organizations and Volunteers:
 * - Displays inbox with allowed contact list on the left
 * - Displays active conversation thread on the right
 * - Handles sending messages and marking incoming messages as read
 * =====================================================================
 */
@Controller
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    /**
     * Injects MessageService and UserService.
     */
    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    /**
     * Displays the messaging inbox and active chat thread with a selected contact.
     */
    @GetMapping
    public String showInbox(@RequestParam(value = "withUser", required = false) Long withUserId,
                            HttpSession session,
                            Model model) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        List<User> contacts = messageService.getEligibleContacts(currentUser);
        model.addAttribute("contacts", contacts);

        User activeContact = null;
        if (withUserId != null) {
            activeContact = userService.findById(withUserId).orElse(null);
        } else if (!contacts.isEmpty()) {
            activeContact = contacts.get(0);
        }

        List<Message> conversation = Collections.emptyList();
        if (activeContact != null && messageService.canMessage(currentUser, activeContact)) {
            conversation = messageService.getConversation(currentUser, activeContact);
            model.addAttribute("activeContact", activeContact);
            model.addAttribute("conversation", conversation);
        } else if (activeContact != null) {
            model.addAttribute("chatError", "You are not authorized to message this user.");
        }

        return "messages/inbox";
    }

    /**
     * Processes sending a new message to the designated recipient.
     */
    @PostMapping("/send")
    public String sendMessage(@RequestParam("receiverId") Long receiverId,
                             @RequestParam("content") String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User sender = (User) session.getAttribute("loggedInUser");
            messageService.sendMessage(sender, receiverId, content);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/messages?withUser=" + receiverId;
    }
}

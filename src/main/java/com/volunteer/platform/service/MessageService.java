package com.volunteer.platform.service;

import com.volunteer.platform.model.Message;
import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import com.volunteer.platform.repository.MessageRepository;
import com.volunteer.platform.repository.RegistrationRepository;
import com.volunteer.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * =====================================================================
 * MessageService (Business Logic Layer)
 * ---------------------------------------------------------------------
 * Handles 1-on-1 direct messaging between Organizations and Volunteers:
 * - Enforces business rule: chat is permitted ONLY if the volunteer has
 *   registered for at least one opportunity hosted by that organization.
 * - Manages conversation retrieval, mark-as-read status, and unread counters.
 * =====================================================================
 */
@SuppressWarnings("null") // Spring Data JPA's findById(Long) is always called with non-null ids from path variables
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    /**
     * Constructor injecting message, registration, and user repositories.
     */
    public MessageService(MessageRepository messageRepository,
                          RegistrationRepository registrationRepository,
                          UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Verifies whether two users are authorized to exchange messages based on registration history.
     */
    public boolean canMessage(User userA, User userB) {
        if (userA == null || userB == null || userA.getId().equals(userB.getId())) {
            return false;
        }

        User volunteer = userA.getRole() == Role.VOLUNTEER ? userA : (userB.getRole() == Role.VOLUNTEER ? userB : null);
        User org = userA.getRole() == Role.ORGANIZATION ? userA : (userB.getRole() == Role.ORGANIZATION ? userB : null);

        if (volunteer != null && org != null) {
            return registrationRepository.hasActiveRegistrationWithOrg(volunteer, org);
        }

        return false;
    }

    /**
     * Retrieves the list of allowed contacts (volunteers for an Org, or organizations for a Volunteer).
     */
    public List<User> getEligibleContacts(User currentUser) {
        if (currentUser.getRole() == Role.ORGANIZATION) {
            return registrationRepository.findDistinctVolunteersByOrganization(currentUser);
        } else if (currentUser.getRole() == Role.VOLUNTEER) {
            return registrationRepository.findDistinctOrganizationsByVolunteer(currentUser);
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves chronological chat history between two users and marks incoming messages as read.
     */
    @Transactional
    public List<Message> getConversation(User currentUser, User otherUser) {
        if (!canMessage(currentUser, otherUser)) {
            throw new IllegalArgumentException("You are not authorized to view messages with this user.");
        }

        List<Message> unread = messageRepository.findBySenderAndReceiverAndIsReadFalse(otherUser, currentUser);
        if (!unread.isEmpty()) {
            for (Message m : unread) {
                m.setRead(true);
            }
            messageRepository.saveAll(unread);
        }

        return messageRepository.findConversation(currentUser, otherUser);
    }

    /**
     * Sends a new message from the current user to a designated recipient.
     */
    @Transactional
    public Message sendMessage(User sender, Long receiverId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be empty.");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found with id: " + receiverId));

        if (!canMessage(sender, receiver)) {
            throw new IllegalArgumentException("You are not authorized to send messages to this user.");
        }

        Message message = new Message(sender, receiver, content.trim());
        return messageRepository.save(message);
    }

    /**
     * Returns the total count of unread incoming messages for displaying in the navbar badge.
     */
    public long getUnreadMessageCount(User user) {
        if (user == null) {
            return 0;
        }
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }
}

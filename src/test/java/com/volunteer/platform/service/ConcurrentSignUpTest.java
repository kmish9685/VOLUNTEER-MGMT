package com.volunteer.platform.service;

import com.volunteer.platform.BaseIntegrationTest;
import com.volunteer.platform.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * =====================================================================
 * ConcurrentSignUpTest (Multithreading test)
 * ---------------------------------------------------------------------
 * Simulates 10 volunteers pressing "Sign Up" at exactly the same moment
 * for an event that has only 3 slots.
 *
 * - ExecutorService runs 10 threads in parallel.
 * - CountDownLatch holds all threads at the start line and releases
 *   them together, so they really race each other.
 *
 * Expected: exactly 3 succeed, 7 get "maximum capacity", and the
 * database has exactly 3 registrations (no overbooking).
 * This works because RegistrationService.signUp() locks the event row
 * (SELECT ... FOR UPDATE) inside its transaction.
 *
 * NOT @Transactional on purpose: every thread must commit its own
 * transaction, just like real users.
 * =====================================================================
 */
class ConcurrentSignUpTest extends BaseIntegrationTest {

    private static final int SLOTS = 3;
    private static final int VOLUNTEERS = 10;

    @Autowired
    private RegistrationService registrationService;

    @Test
    @DisplayName("10 volunteers sign up at the same time for 3 slots: exactly 3 succeed, no overbooking")
    void eventIsNeverOverbookedUnderConcurrentSignUps() throws Exception {
        User organization = createUser(Role.ORGANIZATION);
        Opportunity event = createEvent(organization, OpportunityStatus.APPROVED, LocalDate.now().plusDays(7), SLOTS);

        List<User> volunteers = new ArrayList<>();
        for (int i = 0; i < VOLUNTEERS; i++) {
            volunteers.add(createUser(Role.VOLUNTEER));
        }

        ExecutorService pool = Executors.newFixedThreadPool(VOLUNTEERS);
        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();
        List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());
        List<Future<?>> results = new ArrayList<>();

        for (User volunteer : volunteers) {
            results.add(pool.submit(() -> {
                try {
                    startSignal.await();                         // wait at the start line
                    registrationService.signUp(event.getId(), volunteer);
                    successCount.incrementAndGet();
                } catch (IllegalStateException fullEvent) {
                    rejectedCount.incrementAndGet();             // expected: "maximum capacity"
                } catch (Throwable other) {
                    unexpectedErrors.add(other);                 // anything else is a bug
                }
                return null;
            }));
        }

        startSignal.countDown(); // GO! all 10 threads start together

        for (Future<?> result : results) {
            result.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertTrue(unexpectedErrors.isEmpty(), () -> "Unexpected errors: " + unexpectedErrors);
        assertEquals(SLOTS, successCount.get(), "Exactly the available slots should be filled");
        assertEquals(VOLUNTEERS - SLOTS, rejectedCount.get(), "Everyone else should be told the event is full");
        assertEquals(SLOTS, registrationRepository.countByOpportunityAndStatusNot(event, RegistrationStatus.CANCELLED),
                "Database must contain exactly " + SLOTS + " registrations");
    }
}

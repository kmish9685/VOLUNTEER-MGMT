package com.volunteer.platform.config;

import com.volunteer.platform.model.*;
import com.volunteer.platform.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * =====================================================================
 * DataLoader (Configuration / Startup Layer)
 * ---------------------------------------------------------------------
 * Automatically runs once when the Spring Boot application starts.
 * If the database is empty, it populates demo data:
 * - 1 Admin, 2 Organizations, 2 Volunteers
 * - 3 System Settings (platform_name, allow_registrations, max_hours_per_log)
 * - 3 Opportunities (Approved Future, Approved Past, Pending)
 * - 2 Registrations (ATTENDED and ABSENT)
 * - 2 Messages between Volunteer and Organization
 * =====================================================================
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final RegistrationRepository registrationRepository;
    private final MessageRepository messageRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Injects all necessary repositories and the BCrypt password encoder.
     */
    public DataLoader(UserRepository userRepository,
                      OpportunityRepository opportunityRepository,
                      RegistrationRepository registrationRepository,
                      MessageRepository messageRepository,
                      SystemSettingRepository systemSettingRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        this.registrationRepository = registrationRepository;
        this.messageRepository = messageRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executes seed logic on application startup if database has no users.
     */
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            System.out.println(">>> Database is empty. Seeding initial demo data...");
            seedSystemSettings();
            seedUsersAndContent();
            printCredentialsSummary();
        } else {
            System.out.println(">>> Database already initialized with existing data.");
        }
    }

    /**
     * Seeds the 3 system settings used by the platform.
     */
    private void seedSystemSettings() {
        systemSettingRepository.save(new SystemSetting(
                "platform_name", "VolunteerHub", "Brand name displayed on navbar and page titles"));
        systemSettingRepository.save(new SystemSetting(
                "allow_registrations", "true", "Controls whether new public registrations are open"));
        systemSettingRepository.save(new SystemSetting(
                "max_hours_per_log", "12", "Maximum volunteer service hours permitted in a single log entry"));
    }

    /**
     * Seeds users across all three roles, along with opportunities, signups, and chats.
     */
    private void seedUsersAndContent() {
        // 1. Seed 1 Admin
        User admin = new User(
                "Platform Administrator",
                "admin@gmail.com",
                passwordEncoder.encode("admin123"),
                "+91 9876543210",
                Role.ADMIN,
                UserStatus.ACTIVE
        );
        userRepository.save(admin);

        // 2. Seed 2 Organizations
        User org1 = new User(
                "Green Earth Foundation",
                "greenearth@gmail.com",
                passwordEncoder.encode("org123"),
                "+91 9811122233",
                Role.ORGANIZATION,
                UserStatus.ACTIVE
        );
        org1.setOrganizationDescription("Dedicated to environmental sustainability, tree plantation drives, and urban waste management.");
        userRepository.save(org1);

        User org2 = new User(
                "Helping Hands NGO",
                "helpinghands@gmail.com",
                passwordEncoder.encode("org123"),
                "+91 9844455566",
                Role.ORGANIZATION,
                UserStatus.ACTIVE
        );
        org2.setOrganizationDescription("Empowering underprivileged children and elderly citizens through food security, education, and healthcare.");
        userRepository.save(org2);

        // 3. Seed 2 Volunteers (Rahul and Aman)
        User vol1 = new User(
                "Rahul Verma",
                "rahul@gmail.com",
                passwordEncoder.encode("vol123"),
                "+91 9877700011",
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
        userRepository.save(vol1);

        User vol2 = new User(
                "Aman Gupta",
                "aman@gmail.com",
                passwordEncoder.encode("vol123"),
                "+91 9877700033",
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
        userRepository.save(vol2);

        LocalDate today = LocalDate.now();

        // 4. Seed 3 Events:
        // a) "Community Tree Plantation Drive" by Green Earth, APPROVED, date today + 7 days
        Opportunity opp1 = new Opportunity(
                "Community Tree Plantation Drive",
                "Join us to plant 500 indigenous saplings in the city botanical corridor. Shovels, compost, and gloves provided.",
                "City Botanical Gardens, Sector 4",
                today.plusDays(7),
                LocalTime.of(8, 30),
                LocalTime.of(12, 30),
                25,
                OpportunityStatus.APPROVED,
                org1
        );
        opportunityRepository.save(opp1);

        // b) "Old Age Home Digital Literacy Workshop" by Helping Hands, APPROVED, date today - 4 days (past event)
        Opportunity opp2 = new Opportunity(
                "Old Age Home Digital Literacy Workshop",
                "Taught senior citizens basic smartphone skills, video calling family, and staying safe from online financial frauds.",
                "Anand Elderly Care Home, Civil Lines",
                today.minusDays(4),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0),
                10,
                OpportunityStatus.APPROVED,
                org2
        );
        opportunityRepository.save(opp2);

        // c) "Youth Skill Training Bootcamp" by Helping Hands, PENDING, date today + 20 days (demo admin approval)
        Opportunity opp3 = new Opportunity(
                "Youth Skill Training Bootcamp",
                "Introductory computer hardware and MS Office training for high-school dropouts from neighboring urban settlements.",
                "Community Center Hall B, Ashok Nagar",
                today.plusDays(20),
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                15,
                OpportunityStatus.PENDING,
                org2
        );
        opportunityRepository.save(opp3);

        // 5. Seed Registrations (only 2): Rahul ATTENDED, Aman ABSENT on event (b)
        Registration reg1 = new Registration(vol1, opp2, RegistrationStatus.ATTENDED);
        reg1.setRegisteredAt(LocalDateTime.now().minusDays(8));
        registrationRepository.save(reg1);

        Registration reg2 = new Registration(vol2, opp2, RegistrationStatus.ABSENT);
        reg2.setRegisteredAt(LocalDateTime.now().minusDays(7));
        registrationRepository.save(reg2);

        // 6. Hour logs: none (the volunteer logs hours live in the demo)

        // 7. Seed Messages (2 only, between Rahul and Helping Hands)
        Message msg1 = new Message(
                org2,
                vol1,
                "Hi Rahul! Thank you for participating in the Old Age Home Digital Literacy Workshop. Please remember to log your volunteer hours."
        );
        messageRepository.save(msg1);

        Message msg2 = new Message(
                vol1,
                org2,
                "Hello Helping Hands team! It was a great experience teaching the seniors. I will log my hours shortly."
        );
        messageRepository.save(msg2);
    }

    /**
     * Prints a formatted summary table of demo accounts and credentials in the console.
     */
    private void printCredentialsSummary() {
        System.out.println("============================================================================");
        System.out.println("   ONLINE VOLUNTEER MANAGEMENT PLATFORM - DEMO ACCOUNTS READY               ");
        System.out.println("============================================================================");
        System.out.println(" [ROLE]          [EMAIL]                    [PASSWORD]    [PURPOSE]         ");
        System.out.println(" ---------------------------------------------------------------------------");
        System.out.println(" ADMIN           admin@gmail.com            admin123      Full platform control");
        System.out.println(" ORGANIZATION    greenearth@gmail.com       org123        Post & manage events");
        System.out.println(" ORGANIZATION    helpinghands@gmail.com     org123        Post & manage events");
        System.out.println(" VOLUNTEER       rahul@gmail.com            vol123        Browse, signup, hours");
        System.out.println(" VOLUNTEER       aman@gmail.com             vol123        Browse, signup, hours");
        System.out.println("============================================================================");
    }
}

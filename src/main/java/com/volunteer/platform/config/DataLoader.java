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
 * - 1 Admin, 2 Organizations, 3 Volunteers
 * - All 4 System Settings
 * - 6 Opportunities (Pending, Approved Future, Approved Past, Rejected)
 * - Registrations, Attendance, Hour Logs, Messages, and Activity Logs
 * - Prints demo login credentials to the console for easy testing.
 * =====================================================================
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;
    private final RegistrationRepository registrationRepository;
    private final HourLogRepository hourLogRepository;
    private final MessageRepository messageRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Injects all necessary repositories and the BCrypt password encoder.
     */
    public DataLoader(UserRepository userRepository,
                      OpportunityRepository opportunityRepository,
                      RegistrationRepository registrationRepository,
                      HourLogRepository hourLogRepository,
                      MessageRepository messageRepository,
                      SystemSettingRepository systemSettingRepository,
                      ActivityLogRepository activityLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.opportunityRepository = opportunityRepository;
        this.registrationRepository = registrationRepository;
        this.hourLogRepository = hourLogRepository;
        this.messageRepository = messageRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.activityLogRepository = activityLogRepository;
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
     * Seeds default system settings required by the platform.
     */
    private void seedSystemSettings() {
        systemSettingRepository.save(new SystemSetting(
                "platform_name", "VolunteerHub", "Brand name displayed on navbar and page titles"));
        systemSettingRepository.save(new SystemSetting(
                "allow_registrations", "true", "Controls whether new public registrations are open"));
        systemSettingRepository.save(new SystemSetting(
                "max_hours_per_log", "12", "Maximum volunteer service hours permitted in a single log entry"));
        systemSettingRepository.save(new SystemSetting(
                "auto_approve_opportunities", "false", "Automatically approves newly posted opportunities if true"));
    }

    /**
     * Seeds users across all three roles, along with opportunities, signups, hour logs, and chats.
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
        activityLogRepository.save(new ActivityLog(admin, "System initialized with baseline configuration settings"));

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

        // 3. Seed 3 Volunteers
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
                "Priya Sharma",
                "priya@gmail.com",
                passwordEncoder.encode("vol123"),
                "+91 9877700022",
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
        userRepository.save(vol2);

        User vol3 = new User(
                "Aman Gupta",
                "aman@gmail.com",
                passwordEncoder.encode("vol123"),
                "+91 9877700033",
                Role.VOLUNTEER,
                UserStatus.ACTIVE
        );
        userRepository.save(vol3);

        LocalDate today = LocalDate.now();

        // 4. Seed ~6 Opportunities
        // Opp 1: Approved Future (Green Earth)
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

        // Opp 2: Approved Future (Green Earth)
        Opportunity opp2 = new Opportunity(
                "Beach Cleanup & Marine Plastic Awareness",
                "Morning cleanup drive along the coastline to remove plastic waste and educate local beach visitors on waste segregation.",
                "Sunset Coastal Promenade, North Gate",
                today.plusDays(14),
                LocalTime.of(7, 0),
                LocalTime.of(11, 0),
                30,
                OpportunityStatus.APPROVED,
                org1
        );
        opportunityRepository.save(opp2);

        // Opp 3: Approved Future (Helping Hands)
        Opportunity opp3 = new Opportunity(
                "Food Distribution & Hunger Relief Camp",
                "Distribute nutritious cooked meal packages and drinking water to homeless families and migrant daily wagers.",
                "Central Railway Station Shelter Point",
                today.plusDays(5),
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                20,
                OpportunityStatus.APPROVED,
                org2
        );
        opportunityRepository.save(opp3);

        // Opp 4: Approved Past (Helping Hands)
        Opportunity opp4 = new Opportunity(
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
        opportunityRepository.save(opp4);

        // Opp 5: Pending Review (Helping Hands)
        Opportunity opp5 = new Opportunity(
                "Slum Youth Skill Training Bootcamp",
                "Introductory computer hardware and MS Office training for high-school dropouts from neighboring urban settlements.",
                "Community Center Hall B, Ashok Nagar",
                today.plusDays(20),
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                15,
                OpportunityStatus.PENDING,
                org2
        );
        opportunityRepository.save(opp5);

        // Opp 6: Rejected with Remark (Green Earth)
        Opportunity opp6 = new Opportunity(
                "Night Street Animal Rescue Operation",
                "Rescue injured stray dogs and cats during late night traffic hours across highway bypass routes.",
                "Highway Bypass Junction, Ring Road",
                today.plusDays(10),
                LocalTime.of(21, 0),
                LocalTime.of(1, 0),
                8,
                OpportunityStatus.REJECTED,
                org1
        );
        opp6.setAdminRemark("Requires certified veterinary first responder supervision and safety permits before volunteers can participate.");
        opportunityRepository.save(opp6);

        // 5. Seed Registrations & Attendance
        // For Past Event (opp4):
        Registration reg1 = new Registration(vol1, opp4, RegistrationStatus.ATTENDED);
        reg1.setRegisteredAt(LocalDateTime.now().minusDays(8));
        registrationRepository.save(reg1);

        Registration reg2 = new Registration(vol2, opp4, RegistrationStatus.ATTENDED);
        reg2.setRegisteredAt(LocalDateTime.now().minusDays(7));
        registrationRepository.save(reg2);

        Registration reg3 = new Registration(vol3, opp4, RegistrationStatus.ABSENT);
        reg3.setRegisteredAt(LocalDateTime.now().minusDays(6));
        registrationRepository.save(reg3);

        // For Future Events:
        Registration reg4 = new Registration(vol1, opp1, RegistrationStatus.REGISTERED);
        registrationRepository.save(reg4);

        Registration reg5 = new Registration(vol2, opp1, RegistrationStatus.REGISTERED);
        registrationRepository.save(reg5);

        Registration reg6 = new Registration(vol3, opp3, RegistrationStatus.REGISTERED);
        registrationRepository.save(reg6);

        // 6. Seed Hour Logs
        // Rahul's attended event hours (APPROVED)
        HourLog hour1 = new HourLog(
                reg1,
                4.0,
                "Assisted 8 senior citizens in setting up WhatsApp video calling and enabled two-factor authentication on their phones.",
                HourLogStatus.APPROVED
        );
        hourLogRepository.save(hour1);

        // Priya's attended event hours (PENDING approval)
        HourLog hour2 = new HourLog(
                reg2,
                4.0,
                "Helped seniors write notes, browse YouTube audio playlists, and answered questions regarding online banking fraud alerts.",
                HourLogStatus.PENDING
        );
        hourLogRepository.save(hour2);

        // 7. Seed Direct Messages (between Green Earth and Rahul)
        Message msg1 = new Message(
                org1,
                vol1,
                "Hi Rahul! Thank you for signing up for the Community Tree Plantation Drive. Please remember to wear sturdy shoes and bring a reusable water bottle."
        );
        messageRepository.save(msg1);

        Message msg2 = new Message(
                vol1,
                org1,
                "Hello Green Earth team! Thank you for the update. Will shovels and gardening gloves be provided at the venue?"
        );
        messageRepository.save(msg2);

        Message msg3 = new Message(
                org1,
                vol1,
                "Yes, absolutely! We will provide all plantation tools, saplings, and gloves on site. See you at 8:30 AM!"
        );
        messageRepository.save(msg3);

        // 8. Seed Activity Logs
        activityLogRepository.save(new ActivityLog(vol1, "User registered as VOLUNTEER: Rahul Verma"));
        activityLogRepository.save(new ActivityLog(org1, "Opportunity posted: Community Tree Plantation Drive"));
        activityLogRepository.save(new ActivityLog(admin, "Opportunity APPROVED by Admin: Community Tree Plantation Drive"));
        activityLogRepository.save(new ActivityLog(vol1, "Signed up for: Community Tree Plantation Drive"));
        activityLogRepository.save(new ActivityLog(org2, "Attendance marked: ATTENDED for Rahul Verma in Old Age Home Workshop"));
        activityLogRepository.save(new ActivityLog(vol1, "Logged 4.0 hours for Old Age Home Digital Literacy Workshop"));
        activityLogRepository.save(new ActivityLog(org2, "Approved 4.0 volunteer hours for Rahul Verma"));
        activityLogRepository.save(new ActivityLog(admin, "Opportunity REJECTED with remark: Night Street Animal Rescue Operation"));
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
        System.out.println(" VOLUNTEER       priya@gmail.com            vol123        Browse, signup, hours");
        System.out.println(" VOLUNTEER       aman@gmail.com             vol123        Browse, signup, hours");
        System.out.println("============================================================================");
    }
}

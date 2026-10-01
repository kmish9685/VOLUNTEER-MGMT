# 📘 PROJECT EXPLANATION GUIDE
## Online Volunteer Management Platform — VolunteerHub
### Written in Simple English for Viva Examination

---

## 1. What Does This Project Do?

### The Problem
Community organizations (NGOs, foundations) want volunteers for events like tree plantations, food drives, and skill workshops. But right now:
- There is no single place where volunteers can find verified events
- There is no digital attendance or service-hour tracking
- Communication between volunteers and organizations is scattered

### Our Solution
We built **VolunteerHub** — a web application where:
1. **Organizations** post volunteering events
2. **Admin** reviews and approves events
3. **Volunteers** browse approved events and sign up
4. Organizations **mark attendance** on event day
5. Volunteers **log their service hours** (hours count immediately for attended events)
6. Everyone can **message each other**
7. Admin can **monitor everything** from a single dashboard

---

## 2. Tech Stack and WHY We Chose Each Technology

| Technology | What It Does | Why We Chose It |
|------------|-------------|-----------------|
| **Java 17** | Core programming language | Industry standard, strongly typed, easy to explain OOP concepts |
| **Spring Boot 3** | Framework that sets up everything automatically | Eliminates manual server/config setup; embedded Tomcat means just `run` |
| **Spring Web MVC** | Handles incoming HTTP requests via `@Controller` classes | Clean separation: URL → Controller → Service → View |
| **Thymeleaf** | Generates HTML on the server with dynamic Java data | Simple, readable syntax inside HTML; no need for separate API calls |
| **Spring Data JPA (Hibernate)** | Converts Java objects (entities) to SQL database rows | Write 0 SQL; use `findByEmail()` style methods |
| **PostgreSQL 15 (Supabase)** | Stores all application data in tables | Reliable, free cloud tier, industry standard |
| **spring-security-crypto** | Only for `BCryptPasswordEncoder` | Hashes passwords securely without the complexity of full Spring Security |
| **Bootstrap 5 (CDN)** | Ready-made CSS components | Professional look, responsive design, zero custom CSS skill needed |
| **H2 Database (optional)** | In-memory database for quick testing | Run the project instantly without Supabase setup |

---

## 3. Architecture — Layered MVC Pattern

```
BROWSER
   │
   │  HTTP Request (e.g. GET /volunteer/browse)
   ▼
CONTROLLER Layer (@Controller)
   │  Receives request, extracts parameters, calls the service
   │  (No business logic here — just delegate and return view name)
   ▼
SERVICE Layer (@Service)
   │  All business rules here:
   │  - Is the slot full? Is the event past? Is the user blocked?
   │  - Enforces settings (max_hours_per_log, allow_registrations, platform_name)
   │  - Calls the repository to read/write data
   ▼
REPOSITORY Layer (JpaRepository interfaces)
   │  Spring Data JPA generates SQL automatically
   │  - findByEmail(), countByStatus(), sumHoursBy...()
   ▼
DATABASE (PostgreSQL / Supabase / H2)
   │  Stores data in 7 tables
   │
   ▼
JPA fetches rows → converts to Java objects (Entities)
   │
   ▼
SERVICE sends data to CONTROLLER
   │
   ▼
CONTROLLER adds data to Model (model.addAttribute("key", value))
   │
   ▼
THYMELEAF template reads ${key} and generates HTML
   │
   ▼
BROWSER displays the final HTML page
```

---

## 4. Complete Request Trace — "What Happens When a Volunteer Clicks Sign Up"

1. **Browser** sends: `POST /volunteer/signup/3`
2. **`LoginInterceptor.preHandle()`** checks: Is user logged in? Is role = VOLUNTEER? ✅
3. **`VolunteerController.processSignUp(opportunityId=3, session)`** runs
4. It reads `volunteer = session.getAttribute("loggedInUser")`
5. Calls **`RegistrationService.signUp(3, volunteer)`**
6. Inside `signUp()`:
   - Checks if user is BLOCKED → throw exception if true
   - Calls `opportunityRepository.findById(3)` → loads the opportunity
   - Checks status is APPROVED, event date is future, slots not full
   - Checks no existing non-cancelled registration via `existsByVolunteerAndOpportunityAndStatusNot()`
   - Creates `new Registration(volunteer, opportunity, REGISTERED)` and saves it
   - Calls `activityLogService.log(volunteer, "Signed up for: Beach Cleanup")` → saves audit entry
7. Back in controller: adds `successMessage` via `RedirectAttributes`
8. Returns `"redirect:/volunteer/dashboard"`
9. Browser redirects to dashboard, Thymeleaf renders the page with the flash message visible

---

## 5. Complete Request Trace — "How Login Works"

1. **Browser** sends: `POST /login` with email and password fields
2. **`AuthController.processLogin(email, password, session, redirectAttributes)`** runs
3. Calls **`UserService.authenticate(email, password)`**
4. Inside `authenticate()`:
   - `userRepository.findByEmail(email)` → fetches User from DB
   - `passwordEncoder.matches(rawPassword, user.getPassword())` → BCrypt comparison
   - If no match → throws `IllegalArgumentException("Invalid email or password.")`
   - If status is BLOCKED → throws `IllegalStateException("Your account has been blocked...")`
   - Logs `"User logged in: Rahul Verma"` via `ActivityLogService`
5. Back in controller: `session.setAttribute("loggedInUser", user)` → stores user in session
6. Calls `getDashboardRedirect(role)` → returns `"redirect:/volunteer/dashboard"`
7. Browser redirects to the volunteer dashboard

---

## 6. Database Tables Explained

| Table | Java Entity | Purpose | Key Relationships |
|-------|------------|---------|-------------------|
| `app_users` | `User.java` | All accounts (Admin, Org, Volunteer) | — |
| `opportunities` | `Opportunity.java` | Volunteering events posted by Orgs | ManyToOne → app_users (organization_id) |
| `registrations` | `Registration.java` | Volunteer signups for events | ManyToOne → app_users + opportunities |
| `hour_logs` | `HourLog.java` | Service hours logged by volunteers | ManyToOne → registrations |
| `messages` | `Message.java` | 1-on-1 messages between Org & Volunteer | ManyToOne → app_users (sender + receiver) |
| `system_settings` | `SystemSetting.java` | Platform config key-value pairs | — |
| `activity_logs` | `ActivityLog.java` | Audit trail for Admin monitoring | ManyToOne → app_users (nullable) |

### ER Relationships in Plain English:
- One **User** (org) can have many **Opportunities**
- One **Opportunity** can have many **Registrations**
- One **Registration** (volunteer + opportunity) can have one **HourLog**
- One **User** (sender) can send many **Messages** to another User (receiver)

---

## 7. How Session-Based Login and the LoginInterceptor Work

### Session-Based Login
- When a user logs in successfully, Spring creates an `HttpSession` (a small data container tied to that browser)
- We store: `session.setAttribute("loggedInUser", user)` — stores the Java User object
- On every request, we retrieve it: `(User) session.getAttribute("loggedInUser")`
- When the user logs out: `session.invalidate()` — destroys the session

### LoginInterceptor (HandlerInterceptor)
- `LoginInterceptor` implements `HandlerInterceptor` from Spring MVC
- Spring calls `preHandle()` **before every controller method runs**
- Our logic in `preHandle()`:
  1. Extract session → get `loggedInUser`
  2. If null → user not logged in → redirect to `/login`
  3. If user is BLOCKED → invalidate session → redirect to `/login`
  4. If URL starts with `/admin` and role ≠ ADMIN → redirect to own dashboard
  5. Same for `/org` and `/volunteer`
  6. If all checks pass → return `true` → controller runs normally
- Registered in `WebConfig.addInterceptors()` to guard `/admin/**`, `/org/**`, `/volunteer/**`, `/messages/**`

---

## 8. How Each System Setting Affects Behaviour

| Setting Key | Value | What Java Code It Affects |
|------------|-------|--------------------------|
| `platform_name` | `VolunteerHub` | `GlobalModelAttributes.getPlatformName()` → exposes brand name to every Thymeleaf template |
| `allow_registrations` | `true` / `false` | `UserService.registerUser()` → throws exception if `false`; `register.html` shows "Closed" banner |
| `max_hours_per_log` | `12` (number) | `HourLogService.logHours()` → throws exception if submitted hours exceed this limit |

---

## 9. Feature → Code Mapping Table

| Feature | Controller Method | Service Method | Template |
|---------|------------------|----------------|----------|
| Public landing page | `HomeController.showLandingPage()` | `getTopUpcomingApprovedOpportunities()` | `index.html` |
| Login | `AuthController.processLogin()` | `UserService.authenticate()` | `auth/login.html` |
| Register | `AuthController.processRegister()` | `UserService.registerUser()` | `auth/register.html` |
| Admin dashboard | `AdminController.showDashboard()` | Multiple count services | `admin/dashboard.html` |
| User management | `AdminController.listUsers()` | `UserService.searchAndFilterUsers()` | `admin/users.html` |
| Create user form | `AdminController.showCreateUserForm()` | `UserService.createUserByAdmin()` | `admin/user-form.html` |
| Edit user form | `AdminController.showEditUserForm()` | `UserService.updateUserByAdmin()` | `admin/user-form.html` |
| Block/Unblock user | `AdminController.toggleUserStatus()` | `UserService.toggleUserStatus()` | Redirect to `/admin/users` |
| Delete user | `AdminController.deleteUser()` | `UserService.deleteUser()` | Redirect to `/admin/users` |
| Review opportunities | `AdminController.showReviewOpportunities()` | `OpportunityService.getPendingOpportunities()`, `getAllOpportunities()` | `admin/review-opportunities.html` (pending on top, all below) |
| Review decision | `AdminController.reviewOpportunity()` | `OpportunityService.reviewOpportunity()` | Redirect to `/admin/opportunities/review` |
| Platform monitoring | `AdminController.showMonitoring()` | `RegistrationService.getAllRegistrations()`, `ActivityLogService.getAllLogs()` | `admin/monitoring.html` (registrations + audit logs) |
| Edit settings | `AdminController.showSettings()` | `SettingService.getAllSettings()` | `admin/settings.html` |
| Update setting | `AdminController.updateSetting()` | `SettingService.updateSetting()` | Redirect to `/admin/settings` |
| Org dashboard | `OrganizationController.showDashboard()` | Multiple count services | `organization/dashboard.html` |
| My events | `OrganizationController.listOpportunities()` | `OpportunityService.getOpportunitiesByOrganization()` | `organization/opportunities.html` |
| Post event | `OrganizationController.processCreateOpportunity()` | `OpportunityService.createOpportunity()` | `organization/opportunity-form.html` |
| Edit event | `OrganizationController.processEditOpportunity()` | `OpportunityService.updateOpportunity()` | `organization/opportunity-form.html` |
| View volunteer roster | `OrganizationController.viewVolunteers()` | `RegistrationService.getRegistrationsForOpportunity()` | `organization/volunteers.html` |
| Mark attendance | `OrganizationController.markAttendance()` | `RegistrationService.markAttendance()` | Redirect to `/org/volunteers/{id}` |
| Participation report | `OrganizationController.showParticipationReport()` | `hourLogService.getTotalHoursForOpportunity()` | `organization/report.html` |
| Volunteer dashboard | `VolunteerController.showDashboard()` | `hourLogService.getTotalHoursForVolunteer()` | `volunteer/dashboard.html` |
| Browse events | `VolunteerController.browseOpportunities()` | `OpportunityService.getUpcomingApprovedOpportunities()` | `volunteer/browse.html` |
| Event details | `VolunteerController.showOpportunityDetails()` | `OpportunityService.getOpportunityById()` | `volunteer/opportunity-details.html` |
| Sign up | `VolunteerController.processSignUp()` | `RegistrationService.signUp()` | Redirect to `/volunteer/dashboard` |
| Cancel registration | `VolunteerController.processCancel()` | `RegistrationService.cancelRegistration()` | Redirect to `/volunteer/dashboard` |
| Log hours | `VolunteerController.processLogHours()` | `HourLogService.logHours()` | `volunteer/log-hours.html` |
| Participation history | `VolunteerController.showParticipationHistory()` | Multiple services | `volunteer/history.html` |
| Send message | `MessageController.sendMessage()` | `MessageService.sendMessage()` | Redirect to `/messages?userId=...` |
| View conversation | `MessageController.showInbox()` | `MessageService.getConversation()` | `messages/inbox.html` |

---

## 10. Folder Structure — What Is In Each Folder

```
config/    → Cross-cutting concerns (interceptor, global model, startup data)
model/     → Plain Java classes representing database tables (@Entity) + Enums
repository/→ Interfaces that talk to the database (Spring generates the SQL)
service/   → ALL business rules and validation (the brain of the app)
controller/→ HTTP handlers — receive request, call service, return view name
templates/ → HTML files that Thymeleaf fills with Java data
static/    → CSS and JavaScript that don't change (served directly to browser)
```

---

## 11. 25 Likely Viva Questions with Short Clear Answers

**Q1. What is Spring Boot?**
Spring Boot is a framework built on top of Spring that auto-configures everything (like an embedded Tomcat server, JPA settings) so you can focus on writing business code instead of XML configuration.

**Q2. What is JPA / Hibernate?**
JPA (Java Persistence API) is a standard for mapping Java objects to database tables. Hibernate is the most popular implementation. Instead of writing SQL, you write Java classes with `@Entity`, and Hibernate generates the SQL automatically.

**Q3. What does `@Entity` do?**
It tells JPA that this Java class represents a database table. Every field in the class becomes a column in the table.

**Q4. What is Spring Data JPA?**
It provides ready-made repository interfaces (like `JpaRepository`) with built-in methods like `save()`, `findById()`, `findAll()`. You just define method signatures like `findByEmail(String email)` and Spring generates the implementation.

**Q5. What is BCrypt and why do we hash passwords?**
BCrypt is a one-way hashing algorithm. We never store plain passwords — we store the hash. When a user logs in, we use `passwordEncoder.matches(rawInput, storedHash)` to compare. Even if the database is leaked, passwords cannot be recovered.

**Q6. What is a session?**
An HTTP session is a server-side storage area tied to a browser tab/user. When you log in, the server creates a session with a unique ID and stores your user object there. The session ID is sent to the browser as a cookie, so every subsequent request can retrieve your data.

**Q7. What is `@Controller` vs `@RestController`?**
`@Controller` methods return a **view name** (like `"admin/dashboard"`) which Thymeleaf renders into HTML. `@RestController` methods return **data** (like JSON) directly, used for REST APIs. We use `@Controller` because we do server-side rendering.

**Q8. What is Thymeleaf?**
Thymeleaf is a Java template engine. It takes normal HTML files and lets you insert Java data using special attributes like `th:text="${user.fullName}"`. The server processes these and sends pure HTML to the browser.

**Q9. What is `@ManyToOne`?**
It defines a many-to-one relationship between entities. For example, many `Registrations` can belong to one `Opportunity`. JPA creates a foreign key column (`opportunity_id`) in the registrations table automatically.

**Q10. What is `HandlerInterceptor`?**
A `HandlerInterceptor` intercepts HTTP requests before they reach a controller. We use `LoginInterceptor` to check if the user is logged in and has the correct role before allowing access to protected URLs.

**Q11. What is `@Transactional`?**
It tells Spring to wrap the method in a database transaction. If the method throws an exception, all database changes made inside it are automatically rolled back, preventing partial/corrupt data.

**Q12. What is `RedirectAttributes`?**
It lets us pass messages across a redirect (which reloads the page). When we do `redirectAttributes.addFlashAttribute("successMessage", "Done!")`, that message survives the redirect and is displayed on the next page.

**Q13. What is Dependency Injection?**
Instead of creating objects with `new ServiceClass()`, Spring creates and provides them automatically through constructor injection. This makes code loosely coupled and easier to test. Every `@Service`, `@Repository`, and `@Controller` is managed by Spring's container.

**Q14. What is `@Component`?**
It marks a class as a Spring-managed component. Spring will auto-detect it during startup and create a single shared instance (bean). `@Service`, `@Repository`, and `@Controller` are specializations of `@Component`.

**Q15. What does `ddl-auto=update` do?**
It tells Hibernate to automatically compare the Java `@Entity` classes with the existing database tables and add/modify columns to match. It creates the tables on first run without any manual SQL.

**Q16. What is a `@ModelAttribute`?**
In a controller, `@ModelAttribute` binds a form's POST parameters directly into a Java object. In `@ControllerAdvice`, it exposes a value to every template model automatically (like `platformName`).

**Q17. What is the difference between `@GetMapping` and `@PostMapping`?**
`@GetMapping` handles HTTP GET requests (loading a page). `@PostMapping` handles HTTP POST requests (submitting a form). GET is idempotent (no side effects); POST causes data changes.

**Q18. What is `Optional` in Java?**
`Optional<T>` is a wrapper that may or may not contain a value. `findById()` returns `Optional<User>` to avoid null pointer exceptions. We use `.orElseThrow()` to throw a clean error if not found.

**Q19. What is `@ControllerAdvice`?**
A `@ControllerAdvice` class applies to all controllers globally. We use it in `GlobalModelAttributes` to automatically add `platformName`, `currentUser`, and `unreadCount` to every page's model without repeating code.

**Q20. Why don't you use Lombok?**
For a viva, we must be able to explain every line of code. Lombok hides getters/setters/constructors using annotations, making code invisible. Writing them explicitly means you can point to and explain each part during the viva.

**Q21. What is the purpose of `@PrePersist`?**
It marks a method that runs automatically just before JPA saves a new entity to the database. We use it to auto-set `createdAt = LocalDateTime.now()` so timestamps are always correct.

**Q22. How does the unread message badge work?**
`GlobalModelAttributes.getUnreadCount()` runs on every request, calls `messageService.getUnreadMessageCount(user)`, which executes `messageRepository.countByReceiverAndIsReadFalse(user)` — a simple SQL COUNT query. The result is exposed as `${unreadCount}` in every Thymeleaf template.

**Q23. How do we prevent a volunteer from signing up twice?**
In `RegistrationService.signUp()`, we call `registrationRepository.existsByVolunteerAndOpportunityAndStatusNot(volunteer, opportunity, CANCELLED)`. If it returns `true`, we throw an `IllegalStateException("You are already registered...")`. The unique constraint `(volunteer_id, opportunity_id)` in the database also enforces this at the SQL level.

**Q24. How does volunteer service hour tracking work?**
Once an organization marks a volunteer as `ATTENDED` for an event, the volunteer can log their contributed hours via `HourLogService.logHours()`. The service validates that hours do not exceed `max_hours_per_log` from `SettingService`. Hours count immediately toward the volunteer's service record and organization impact reports without needing a separate approval step.

**Q25. What is the request lifecycle in Spring MVC?**
Browser → Tomcat (embedded) → `DispatcherServlet` → `LoginInterceptor.preHandle()` → `@Controller` method → `@Service` method → `@Repository` → Database → back up the chain → `Model` filled with data → `DispatcherServlet` picks the view → Thymeleaf processes the HTML template → HTML sent to Browser.

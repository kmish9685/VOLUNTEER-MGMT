# 🤝 VolunteerHub — Online Volunteer Management Platform

> **Semester Live Project** | B.Tech CSE (AI/ML) 3rd Semester  
> Built with **Java 17 + Spring Boot 3 + Thymeleaf + MySQL 8 + Bootstrap 5**

---

## 📌 Project Summary

VolunteerHub is a web platform that **connects NGOs and Community Organizations with Volunteers**. It supports three user roles — Admin, Organization, and Volunteer — each with a dedicated dashboard and a complete set of workflow features including opportunity management, volunteer registrations, attendance marking, service hour tracking, and direct messaging.

---

## ⚙️ Prerequisites (Install Before Running)

| Tool | Version | Why Needed |
|------|---------|------------|
| **JDK 17** | 17+ | Java runtime to compile and run the Spring Boot app |
| **Apache Maven** | 3.6+ | Build tool to download dependencies and package the project |
| **MySQL** | 8.0+ | Primary database where all project data is stored |
| **Git** | Any | To clone the repository |

> **Note:** Maven is bundled in the `mvnw.cmd` wrapper in this project. If Maven is not in your PATH, the wrapper will auto-detect it at `%USERPROFILE%\.m2\apache-maven-3.9.6\`.

---

## 🚀 Setup & Run (Step by Step)

### Step 1 – Clone the project
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
cd "VOLUNTEER MGMT"
```

### Step 2 – Configure MySQL
Open `src/main/resources/application.properties` and update if your MySQL credentials are different:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/volunteer_db?createDatabaseIfNotExist=true
spring.datasource.username=root      # Change if different
spring.datasource.password=root      # Change if different
```
> The database `volunteer_db` is created automatically on first run.

### Step 3 – Run the application
```powershell
# On Windows (using the provided wrapper):
.\mvnw.cmd spring-boot:run

# OR — Run with H2 in-memory database (no MySQL needed):
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

### Step 4 – Open in Browser
```
http://localhost:8080/
```

> ✅ On first startup, all 7 database tables are auto-created and the demo data is seeded automatically!

---

## 🔑 Demo Login Credentials

| Role | Email | Password | Access |
|------|-------|----------|--------|
| **ADMIN** | admin@gmail.com | admin123 | Full platform control |
| **ORGANIZATION** | greenearth@gmail.com | org123 | Post & manage events |
| **ORGANIZATION** | helpinghands@gmail.com | org123 | Post & manage events |
| **VOLUNTEER** | rahul@gmail.com | vol123 | Browse, signup, log hours |
| **VOLUNTEER** | priya@gmail.com | vol123 | Browse, signup, log hours |
| **VOLUNTEER** | aman@gmail.com | vol123 | Browse, signup, log hours |

---

## ✨ Feature List by Role

### 🔴 ADMIN (`/admin/...`)
- Dashboard with KPI counters (volunteers, orgs, signups, approved hours)
- User Management — Create, edit, block/unblock, delete any user
- Review Opportunities — Approve or Reject with feedback remark
- View all opportunities, all registrations, complete audit log
- System Settings — Edit 4 platform-wide configuration values

### 🔵 ORGANIZATION (`/org/...`)
- Dashboard with personal event metrics
- Post, edit, delete volunteering opportunities
- View registered volunteer roster per event
- Mark attendance (ATTENDED / ABSENT) on or after event date
- Review and approve/reject volunteer hour submissions
- Printable participation summary report (`window.print()`)
- Direct messaging with registered volunteers

### 🟢 VOLUNTEER (`/volunteer/...`)
- Dashboard with verified hours, upcoming events
- Browse approved upcoming opportunities with live slot counter
- Search by event title or location
- View event details and hosting organization info
- Sign up and cancel registration (before event date)
- Log service hours (after attendance confirmed)
- Complete participation history with hour log status
- Direct messaging with hosting organizations

### 💬 MESSAGING (`/messages/...`)
- Restricted chat — volunteer and org can message **only if a registration exists**
- Unread message badge in navbar
- Chat auto-scrolls to latest message on page load

---

## 🗂️ Project Folder Structure

```
src/main/java/com/volunteer/platform/
├── VolunteerPlatformApplication.java   ← Spring Boot entry point + BCrypt bean
├── config/                             ← Interceptor, WebConfig, GlobalModelAttributes, DataLoader
├── model/                              ← 7 JPA entities + 5 enums (NO Lombok)
├── repository/                         ← 7 Spring Data JPA interfaces
├── service/                            ← ALL business logic rules
└── controller/                         ← 6 controllers (call services, return views)

src/main/resources/
├── application.properties              ← MySQL config
├── application-h2.properties           ← Fallback H2 config
├── static/css/custom.css              ← Emerald theme, badges, chat bubbles
└── templates/
    ├── fragments/layout.html           ← Shared navbar, alerts, footer
    ├── index.html                      ← Public landing page
    ├── error.html                      ← Custom error page
    ├── auth/                           ← login.html, register.html
    ├── admin/                          ← 8 admin pages
    ├── organization/                   ← 6 org pages
    ├── volunteer/                      ← 5 volunteer pages
    └── messages/                       ← inbox.html (chat UI)
```

---

## 🧰 Tech Stack Summary

| Technology | Purpose |
|------------|---------|
| Java 17 | Core programming language |
| Spring Boot 3.3.4 | Auto-configuration and embedded Tomcat server |
| Spring Web MVC | Handles HTTP requests via @Controller |
| Thymeleaf | Server-side HTML template engine |
| Spring Data JPA (Hibernate) | ORM — maps Java classes to MySQL tables |
| MySQL 8 | Relational database |
| `spring-security-crypto` | BCrypt password hashing only (NOT full Spring Security) |
| Bootstrap 5 (CDN) | Responsive UI components and layout grid |
| Bootstrap Icons (CDN) | Icon library for visual indicators |
| Spring Boot DevTools | Auto-reload during development |

---

## 🛡️ How to Run for Viva Demo

1. Start MySQL service
2. Run `.\mvnw.cmd spring-boot:run` in PowerShell
3. Open `http://localhost:8080/`
4. Use the quick demo login buttons on the login page to demonstrate all 3 roles
5. Walk through: Register → Admin Approve → Volunteer Signup → Attendance → Log Hours → Approve Hours → Messages → Admin Dashboard

---

## 📝 Notes for Viva

- No Lombok — all getters/setters/constructors are **hand-written and visible**
- No full Spring Security — authentication is **plain `HttpSession`** for easy explanation
- Every class and public method has a **plain-English comment block**
- The `Settings` page has a sidebar explaining **exactly which Java code** each setting affects

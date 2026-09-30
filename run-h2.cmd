@echo off
:: ============================================================
:: VolunteerHub - OPTIONAL quick run with H2 in-memory database
:: No Supabase/PostgreSQL needed, but ALL DATA IS LOST when the app stops.
:: Use run.cmd (Supabase) for the real demo.
:: ============================================================

if "%JAVA_HOME%"=="" (
    echo [ERROR] JAVA_HOME is not set. Install JDK 17 and set JAVA_HOME, then try again.
    pause
    exit /b 1
)

echo Starting VolunteerHub with H2 (temporary data)...
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2

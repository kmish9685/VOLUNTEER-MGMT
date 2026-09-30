@echo off
:: ============================================================
:: VolunteerHub - Run with Supabase PostgreSQL (main database)
:: Make sure db-secret.properties in the project root has your
:: Supabase host, username, and password filled in.
:: ============================================================

if "%JAVA_HOME%"=="" (
    echo [ERROR] JAVA_HOME is not set. Install JDK 17 and set JAVA_HOME, then try again.
    pause
    exit /b 1
)

echo ==========================================================
echo  Starting VolunteerHub with Supabase (PostgreSQL)...
echo  Open your browser at: http://localhost:8080/
echo  Press Ctrl+C to stop the server.
echo ==========================================================

call mvnw.cmd spring-boot:run
pause

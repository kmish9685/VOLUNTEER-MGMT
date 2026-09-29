@echo off
:: ============================================================
:: VolunteerHub - Run with MySQL (main database)
:: Make sure MySQL is running and the username/password in
:: src\main\resources\application.properties are correct.
:: ============================================================

if "%JAVA_HOME%"=="" (
    echo [ERROR] JAVA_HOME is not set. Install JDK 17 and set JAVA_HOME, then try again.
    pause
    exit /b 1
)

echo ==========================================================
echo  Starting VolunteerHub with MySQL database...
echo  Open your browser at: http://localhost:8080/
echo  Press Ctrl+C to stop the server.
echo ==========================================================

call mvnw.cmd spring-boot:run
pause

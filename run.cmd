@echo off
:: ============================================================
:: VolunteerHub - Easy Run Script
:: Double-click this file OR run it from any terminal
:: It automatically sets JAVA_HOME so mvnw.cmd works correctly
:: ============================================================

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot"

echo ==========================================================
echo  Starting VolunteerHub with H2 in-memory database...
echo  Open your browser at: http://localhost:8080/
echo  Press Ctrl+C to stop the server.
echo ==========================================================

call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2

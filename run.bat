@echo off
echo =====================================================================
echo  StayNest - Starting Backend Server
echo  Firebase Cloud Firestore Database
echo =====================================================================
echo.
echo  Opening in browser: http://localhost:8080
echo  Firebase status:    http://localhost:8080/api/firebase/status
echo.
echo  Press Ctrl+C to stop the server.
echo =====================================================================
echo.
"D:\Program Files\Java\jdk-25.0.4.1\bin\java.exe" -jar target\staynest-1.0.0.jar
pause

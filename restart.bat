@echo off
REM Mogako Docker Restart Script for Windows
REM This script stops, rebuilds, and restarts all Docker services

echo 🚀 Mogako Docker Restart Script
echo ================================

REM Check if docker-compose is available
where docker-compose >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ docker-compose is not installed or not in PATH
    pause
    exit /b 1
)

echo.
echo 🔄 Stopping and removing containers...
docker-compose down

echo.
echo 🔄 Building new images...
docker-compose build --no-cache

echo.
echo 🔄 Starting services...
docker-compose up -d

echo.
echo 🔄 Waiting for services to start...
timeout /t 5 /nobreak >nul

echo.
echo 🔄 Checking service status...
docker-compose ps

echo.
echo 🔄 Showing recent logs...
docker-compose logs --tail=10

echo.
echo ✅ Restart completed!
echo.
echo 📋 Available services:
echo    • PostgreSQL: localhost:5432
echo    • Auth API: http://localhost:8080
echo    • Auth API Docs: http://localhost:8080/docs
echo    • Calendar API: http://localhost:8081
echo    • Calendar API Docs: http://localhost:8081/docs
echo.
echo 📝 Useful commands:
echo    • View logs: docker-compose logs -f [service-name]
echo    • Stop services: docker-compose down
echo    • Check status: docker-compose ps
echo.
pause
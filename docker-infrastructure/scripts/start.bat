@echo off
echo ========================================
echo Starting Fleet Management Infrastructure
echo ========================================

:: Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

:: Navigate to docker infrastructure directory
cd /d "%~dp0\.."

:: Start infrastructure
echo Starting infrastructure services...
docker-compose up -d

echo.
echo Waiting for services to initialize...
timeout /t 60 /nobreak >nul

echo.
echo ========================================
echo Fleet Management Infrastructure Started!
echo ========================================
echo.
echo Access URLs:
echo - Database Management: http://localhost:8081
echo - Monitoring: http://localhost:3000
echo - Kafka Management: http://localhost:8070
echo - Redis Management: http://localhost:8082
echo.
pause
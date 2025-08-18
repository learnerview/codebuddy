@echo off
chcp 65001 >nul
echo 🚀 Welcome to CodeBuddy Setup!
echo ================================

REM Check if Java is installed
echo 📋 Checking prerequisites...

java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java is not installed. Please install Java 17 or higher first.
    echo    Download from: https://adoptium.net/
    pause
    exit /b 1
)

echo ✅ Java detected

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed. Please install Maven 3.6 or higher first.
    echo    Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo ✅ Maven detected

REM Check if MySQL is running
echo 🔍 Checking MySQL connection...

REM Try to connect to MySQL with default credentials
mysql -u root -p123321 -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL connection successful
    set MYSQL_USER=root
    set MYSQL_PASS=123321
    goto :create_database
)

REM Try without password
mysql -u root -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ MySQL connection successful (no password)
    set MYSQL_USER=root
    set MYSQL_PASS=
    goto :create_database
)

echo ❌ Cannot connect to MySQL. Please ensure MySQL is running:
echo    Windows: Start MySQL service from Services
echo    Or run: net start mysql
echo.
echo Also ensure MySQL client is in your PATH
pause
exit /b 1

:create_database
REM Create database if it doesn't exist
echo 🗄️  Setting up database...

if "%MYSQL_PASS%"=="" (
    mysql -u %MYSQL_USER% -e "CREATE DATABASE IF NOT EXISTS codebuddy_db;"
) else (
    mysql -u %MYSQL_USER% -p%MYSQL_PASS% -e "CREATE DATABASE IF NOT EXISTS codebuddy_db;"
)

if %errorlevel% equ 0 (
    echo ✅ Database 'codebuddy_db' is ready
) else (
    echo ❌ Failed to create database
    pause
    exit /b 1
)

REM Update config.properties if needed
echo ⚙️  Updating configuration...
set CONFIG_FILE=src\main\resources\config.properties

if exist "%CONFIG_FILE%" (
    if "%MYSQL_PASS%"=="" (
        powershell -Command "(Get-Content '%CONFIG_FILE%') -replace 'db\.password=.*', 'db.password=' | Set-Content '%CONFIG_FILE%'"
    ) else (
        powershell -Command "(Get-Content '%CONFIG_FILE%') -replace 'db\.password=.*', 'db.password=123321' | Set-Content '%CONFIG_FILE%'"
    )
    echo ✅ Configuration updated
) else (
    echo ❌ Configuration file not found: %CONFIG_FILE%
    pause
    exit /b 1
)

REM Build the project
echo 🔨 Building project...
mvn clean install

if %errorlevel% equ 0 (
    echo ✅ Project built successfully
) else (
    echo ❌ Build failed. Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo 🎉 Setup completed successfully!
echo ================================
echo.
echo To run CodeBuddy:
echo   GUI Mode: mvn javafx:run
echo   CLI Mode: mvn exec:java -Dexec.mainClass="org.codebuddy.cli.CLIApp"
echo.
echo Default login credentials:
echo   Username: admin
echo   Password: admin123
echo.
echo Happy coding! 🚀
pause 
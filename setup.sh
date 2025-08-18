#!/bin/bash

# CodeBuddy Setup Script
# This script helps set up the CodeBuddy project with MySQL

echo "🚀 Welcome to CodeBuddy Setup!"
echo "================================"

# Check if Java is installed
echo "📋 Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher first."
    echo "   Download from: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java version $JAVA_VERSION detected. Java 17 or higher is required."
    exit 1
fi
echo "✅ Java $JAVA_VERSION detected"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven 3.6 or higher first."
    echo "   Ubuntu/Debian: sudo apt install maven"
    echo "   macOS: brew install maven"
    exit 1
fi
echo "✅ Maven detected"

# Check if MySQL is running
echo "🔍 Checking MySQL connection..."

if ! command -v mysql &> /dev/null; then
    echo "❌ MySQL client is not installed. Please install MySQL first."
    echo "   Ubuntu/Debian: sudo apt install mysql-server"
    echo "   macOS: brew install mysql"
    exit 1
fi

# Try to connect to MySQL
if mysql -u root -p123321 -e "SELECT 1" &> /dev/null; then
    echo "✅ MySQL connection successful"
    MYSQL_USER="root"
    MYSQL_PASS="123321"
elif mysql -u root -e "SELECT 1" &> /dev/null; then
    echo "✅ MySQL connection successful (no password)"
    MYSQL_USER="root"
    MYSQL_PASS=""
else
    echo "❌ Cannot connect to MySQL. Please ensure MySQL is running:"
    echo "   Ubuntu/Debian: sudo systemctl start mysql"
    echo "   macOS: brew services start mysql"
    echo "   Windows: Start MySQL service from Services"
    exit 1
fi

# Create database if it doesn't exist
echo "🗄️  Setting up database..."
if [ -z "$MYSQL_PASS" ]; then
    mysql -u "$MYSQL_USER" -e "CREATE DATABASE IF NOT EXISTS codebuddy_db;"
else
    mysql -u "$MYSQL_USER" -p"$MYSQL_PASS" -e "CREATE DATABASE IF NOT EXISTS codebuddy_db;"
fi

if [ $? -eq 0 ]; then
    echo "✅ Database 'codebuddy_db' is ready"
else
    echo "❌ Failed to create database"
    exit 1
fi

# Update config.properties if needed
echo "⚙️  Updating configuration..."
CONFIG_FILE="src/main/resources/config.properties"

if [ -f "$CONFIG_FILE" ]; then
    if [ -z "$MYSQL_PASS" ]; then
        sed -i 's/db.password=.*/db.password=/' "$CONFIG_FILE"
    else
        sed -i 's/db.password=.*/db.password=123321/' "$CONFIG_FILE"
    fi
    echo "✅ Configuration updated"
else
    echo "❌ Configuration file not found: $CONFIG_FILE"
    exit 1
fi

# Build the project
echo "🔨 Building project..."
mvn clean install

if [ $? -eq 0 ]; then
    echo "✅ Project built successfully"
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "🎉 Setup completed successfully!"
echo "================================"
echo ""
echo "To run CodeBuddy:"
echo "  GUI Mode: mvn javafx:run"
echo "  CLI Mode: mvn exec:java -Dexec.mainClass=\"org.codebuddy.cli.CLIApp\""
echo ""
echo "Default login credentials:"
echo "  Username: admin"
echo "  Password: admin123"
echo ""
echo "Happy coding! 🚀" 
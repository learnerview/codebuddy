# 🚀 CodeBuddy Quick Start Guide

Get CodeBuddy running in **5 minutes** with this quick start guide!

## ⚡ Quick Setup (5 minutes)

### 1. **Prerequisites Check** (1 min)
```bash
# Check Java version (must be 17+)
java -version

# Check Maven
mvn -version

# Check MySQL
mysql --version
```

**Missing something?** Install from:
- **Java**: [Eclipse Temurin](https://adoptium.net/)
- **Maven**: [Maven](https://maven.apache.org/download.cgi)
- **MySQL**: [MySQL](https://dev.mysql.com/downloads/mysql/)

### 2. **Start MySQL** (1 min)
```bash
# Ubuntu/Debian
sudo systemctl start mysql

# macOS
brew services start mysql

# Windows
# Start MySQL service from Services
```

### 3. **Clone & Setup** (2 min)
```bash
git clone https://github.com/learnerview/codebuddy.git
cd codebuddy

# Run setup script
./setup.sh          # Linux/macOS
setup.bat           # Windows
```

### 4. **Launch** (1 min)
```bash
# GUI Mode (recommended)
mvn javafx:run

# CLI Mode
mvn exec:java -Dexec.mainClass="org.codebuddy.cli.CLIApp"
```

## 🔑 First Login
- **Username**: `admin`
- **Password**: `admin123`

## 🎯 What You'll See
1. **Login Screen** → Enter credentials
2. **Main Dashboard** → View your problems and stats
3. **Sample Data** → Pre-loaded problems to explore
4. **Add Problem** → Start tracking your own solutions

## 🚨 Common Quick Fixes

### "MySQL Connection Failed"
```bash
# Check if MySQL is running
sudo systemctl status mysql

# Start if stopped
sudo systemctl start mysql
```

### "Java Version Error"
```bash
# Install Java 17+
sudo apt install openjdk-17-jdk  # Ubuntu/Debian
brew install openjdk@17          # macOS
```

### "Maven Not Found"
```bash
# Install Maven
sudo apt install maven           # Ubuntu/Debian
brew install maven               # macOS
```

## 📱 Your First Problem
1. Click **"Add Problem"**
2. Fill in:
   - Name: `Hello World`
   - Platform: `LeetCode`
   - Difficulty: `Easy`
   - Time: `5`
   - Notes: `First problem!`
3. Click **Save**

## 🔍 Explore Features
- **Analytics**: Menu → Analytics
- **Theme Toggle**: Dark/Light mode button
- **Export Data**: File → Export to CSV/JSON
- **Search**: Use search bar to find problems

## 📚 Need More Help?
- **Full Documentation**: [README.md](README.md)
- **Troubleshooting**: [README.md#-troubleshooting](README.md#-troubleshooting)
- **Issues**: [GitHub Issues](https://github.com/learnerview/codebuddy/issues)

---

**🎉 You're all set! Happy coding with CodeBuddy! 🚀** 
# CodeBuddy: Competitive Practice Tracker

> **Note:** This project was built with the help of an **AI code editor**, where I contributed by handling **database connections (MySQL)** and crafting the **prompts** to guide the implementation. The project has been thoroughly tested and debugged to ensure full functionality.

Welcome to **CodeBuddy**! 🎯 This is a desktop application designed to track competitive programming practice, analyze problem-solving progress, and keep motivation high through comprehensive analytics and progress tracking.

CodeBuddy helps competitive programmers maintain consistency, track their learning journey, and gain insights into their problem-solving patterns across different platforms and difficulty levels.

---

## 🚀 What is CodeBuddy?

**CodeBuddy** is a **JavaFX-based desktop application** designed specifically for competitive programmers. It provides:

- **Problem Management**: Log and organize solved problems with detailed metadata
- **Real-time Analytics**: Get insights on your practice patterns and progress
- **Streak Tracking**: Maintain motivation with visual progress indicators
- **Multi-platform Support**: Track problems from LeetCode, CodeChef, CodeForces, HackerRank, and more
- **Data Portability**: Import/export your data in CSV or JSON formats

The application is built with a clean architecture using Java 17, JavaFX 17, and MySQL, ensuring reliability and performance.

---

## ✨ Features

### 🔖 Problem Management
- **Add/Edit/Delete Problems** with comprehensive details:
  - Problem name and description
  - Platform (LeetCode, CodeChef, CodeForces, HackerRank, Other)
  - Difficulty level (Easy/Medium/Hard)
  - Time taken to solve
  - Personal notes and solution links
  - Automatic timestamp tracking
- **Smart Filtering**: Filter problems by platform, difficulty, and search terms
- **Recent Activity Table**: Customizable view of your latest problems

### 📊 Analytics Dashboard
- **Streak Tracker**: Monitor consecutive days of problem-solving
- **Difficulty Distribution**: Visual breakdown of Easy/Medium/Hard problems
- **Platform Analysis**: Compare your performance across different platforms
- **Monthly Progress**: Track your problem-solving trends over time
- **Time Analysis**: Understand your time management patterns

### 🧑‍💻 User Management
- **Secure Authentication**: Login and registration system with password hashing
- **User Isolation**: Each user's data is securely separated
- **Session Management**: Persistent login sessions

### 🎨 User Experience
- **Theme Support**: Dark, Light, and High-Contrast modes
- **Responsive Design**: Clean, modern JavaFX interface
- **Keyboard Shortcuts**: Efficient navigation and operation
- **Accessibility**: High-contrast mode for better visibility

### 📂 Data Management
- **CSV Export/Import**: Backup and restore your data
- **JSON Export/Import**: Alternative data format support
- **Data Validation**: Ensures data integrity during import operations

---

## 🧩 Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   JavaFX UI     │    │   Controllers   │    │   Services      │
│   (FXML Views)  │◄──►│   (Event Hand.) │◄──►│   (Business     │
└─────────────────┘    └─────────────────┘    │    Logic)       │
                                              └─────────────────┘
                                                       │
                                              ┌─────────────────┐
                                              │   DAO Layer     │
                                              │   (Data Access) │
                                              └─────────────────┘
                                                       │
                                              ┌─────────────────┐
                                              │   MySQL         │
                                              │   Database      │
                                              └─────────────────┘
```

**Technology Stack:**
- **Frontend**: JavaFX 17 with FXML-based views
- **Backend**: Java 17 with DAO pattern and service layer
- **Database**: MySQL 8.0+ with proper schema design
- **Build Tool**: Maven for dependency management
- **Testing**: JUnit 5 for unit testing

---

## 📋 Prerequisites

**Before running CodeBuddy, you MUST have the following installed:**

### 1. **Java 17 or Higher**
```bash
# Check your Java version
java -version
```
Download from: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)

### 2. **MySQL Server 8.0 or Higher**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# macOS (using Homebrew)
brew install mysql

# Windows
# Download from: https://dev.mysql.com/downloads/mysql/
```

### 3. **Maven 3.6 or Higher**
```bash
# Ubuntu/Debian
sudo apt install maven

# macOS (using Homebrew)
brew install maven

# Windows
# Download from: https://maven.apache.org/download.cgi
```

---

## 🖥️ Installation & Setup

### Step 1: Clone the Repository
```bash
git clone https://github.com/learnerview/codebuddy.git
cd codebuddy
```

### Step 2: Configure MySQL
1. **Start MySQL Server:**
   ```bash
   # Ubuntu/Debian
   sudo systemctl start mysql
   
   # macOS
   brew services start mysql
   
   # Windows
   # Start MySQL service from Services
   ```

2. **Create Database and User:**
   ```sql
   -- Connect to MySQL as root
   mysql -u root -p
   
   -- Create database
   CREATE DATABASE codebuddy_db;
   
   -- Create user (optional, you can use root)
   CREATE USER 'codebuddy'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON codebuddy_db.* TO 'codebuddy'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

3. **Update Configuration** (if using custom credentials):
   Edit `src/main/resources/config.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/codebuddy_db
   db.user=your_username
   db.password=your_password
   ```

### Step 3: Build the Project
```bash
mvn clean install
```

### Step 4: Run the Application
```bash
# GUI Mode (default)
mvn javafx:run

# CLI Mode
mvn exec:java -Dexec.mainClass="org.codebuddy.cli.CLIApp"
```

---

## 📝 Usage Guide

### First Launch
1. **Start the application** - The database will be automatically initialized
2. **Default credentials**: `admin` / `admin123`
3. **Sample data** will be loaded automatically for demonstration

### Adding Problems
1. Click **"Add Problem"** button or use the File menu
2. Fill in the problem details:
   - **Name**: Problem title (e.g., "Two Sum")
   - **Platform**: Select from LeetCode, CodeChef, etc.
   - **Difficulty**: Easy, Medium, or Hard
   - **Time**: Minutes taken to solve
   - **Notes**: Your solution approach or insights
   - **Link**: URL to the problem (optional)
3. Click **Save** - the problem appears in your table immediately

### Using Analytics
1. Navigate to **Analytics** from the menu
2. View your progress through various charts:
   - **Platform Distribution**: Where you solve most problems
   - **Difficulty Breakdown**: Your comfort level with different difficulties
   - **Monthly Progress**: Your consistency over time
   - **Streak Information**: Current and maximum streaks

### Data Management
- **Export**: Save your data to CSV or JSON for backup
- **Import**: Restore data from previously exported files
- **Filtering**: Use the left sidebar to filter problems by platform/difficulty
- **Search**: Use the search bar to find specific problems

---

## 🚨 Troubleshooting

### Common Issues and Solutions

#### 1. **"Failed to initialize database"**
**Cause**: MySQL connection issues
**Solution**:
```bash
# Check if MySQL is running
sudo systemctl status mysql

# Verify connection
mysql -u root -p -h localhost

# Check firewall settings
sudo ufw status
```

#### 2. **"Access denied for user"**
**Cause**: Incorrect database credentials
**Solution**:
```bash
# Reset MySQL root password if needed
sudo mysql -u root
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
EXIT;
```

#### 3. **"Port 3306 already in use"**
**Cause**: Another MySQL instance or service using the port
**Solution**:
```bash
# Find what's using the port
sudo netstat -tlnp | grep :3306

# Stop conflicting service or change MySQL port
```

#### 4. **JavaFX Runtime Errors**
**Cause**: JavaFX modules not properly configured
**Solution**:
```bash
# Ensure you're using Java 17+
java -version

# Clean and rebuild
mvn clean install
```

#### 5. **"No suitable driver found"**
**Cause**: MySQL connector dependency issue
**Solution**:
```bash
# Clean Maven cache and rebuild
mvn clean install -U
```

---

## 🧪 Testing

Run the test suite to verify everything is working:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProblemDaoTest

# Run with detailed output
mvn test -Dtest=ProblemDaoTest -Dsurefire.useFile=false
```

---

## 🔧 Development

### Project Structure
```
src/
├── main/
│   ├── java/org/codebuddy/
│   │   ├── cli/          # Command-line interface
│   │   ├── core/         # Business logic and data access
│   │   │   ├── dao/      # Data Access Objects
│   │   │   ├── models/   # Domain models
│   │   │   └── services/ # Business services
│   │   ├── gui/          # JavaFX user interface
│   │   └── Main.java     # Application entry point
│   └── resources/
│       ├── fxml/         # FXML layout files
│       ├── css/          # Stylesheets
│       └── config.properties
└── test/                 # Unit tests
```

### Adding New Features
1. **Models**: Add new entities in `core/models/`
2. **DAO**: Implement data access in `core/dao/`
3. **Services**: Add business logic in `core/services/`
4. **UI**: Create FXML files and controllers in `gui/`
5. **Tests**: Write unit tests in `test/`

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add Javadoc for public methods
- Handle exceptions gracefully
- Write unit tests for new functionality

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

### Reporting Issues
1. Check the [troubleshooting section](#-troubleshooting) first
2. Search existing issues to avoid duplicates
3. Provide detailed information:
   - Operating system and version
   - Java version
   - MySQL version
   - Error messages and stack traces
   - Steps to reproduce

### Suggesting Features
1. Open an issue with the "enhancement" label
2. Describe the feature and its benefits
3. Provide use cases and examples

### Submitting Code
1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Make your changes and add tests
4. Commit with clear messages: `git commit -m "Add feature: description"`
5. Push and create a pull request

### Areas for Improvement
- **Online Sync**: Cloud-based data synchronization
- **More Platforms**: Support for additional coding platforms
- **Advanced Analytics**: Machine learning insights
- **Mobile App**: Companion mobile application
- **API Integration**: Direct integration with coding platforms

---

## 📄 License

This project is open source under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **JavaFX Team** for the excellent UI framework
- **MySQL Team** for the robust database system
- **Open Source Community** for the various libraries used
- **AI Coding Assistants** for helping accelerate development

---

## 📞 Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/learnerview/codebuddy/issues)
- **Documentation**: This README and inline code comments
- **Community**: Feel free to ask questions in issues

---

**Built with ❤️ by [learnerview](https://github.com/learnerview)**

*CodeBuddy - Making competitive programming practice more organized and motivating! 🚀* 

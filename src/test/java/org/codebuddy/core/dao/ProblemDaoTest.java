package org.codebuddy.core.dao;

import org.codebuddy.core.models.Difficulty;
import org.codebuddy.core.models.Platform;
import org.codebuddy.core.models.Problem;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProblemDaoTest {
    private ProblemDao problemDao;

    @BeforeEach
    void setup() throws SQLException {
        // Initialize database if not already done
        if (!DatabaseManager.isInitialized()) {
            DatabaseManager.initializeDatabase();
        }
        
        // Clear test data
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM problems WHERE name LIKE 'Test%'");
        }
        problemDao = new ProblemDao();
    }

    @AfterEach
    void cleanup() throws SQLException {
        // Clear test data
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM problems WHERE name LIKE 'Test%'");
        }
    }

    @AfterAll
    static void finalCleanup() {
        // Don't close the connection as it's used by the entire application
        // DatabaseManager.closeConnection();
    }

    @Test
    void testSaveAndRetrieveProblem() throws SQLException {
        Problem problem = new Problem(0, "Test Problem", Platform.LEETCODE, Difficulty.EASY, 10, LocalDateTime.now(), "Test notes", "http://test.com", 1);
        problemDao.saveProblem(problem);
        
        List<Problem> problems = problemDao.getAllProblems();
        assertFalse(problems.isEmpty(), "Problem list should not be empty");
        
        // Find our test problem
        Problem found = problems.stream()
            .filter(p -> p.getName().equals("Test Problem"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(found, "Test problem should be found");
        assertEquals("Test Problem", found.getName(), "Problem name should match");
        assertEquals(Platform.LEETCODE, found.getPlatform(), "Platform should match");
        assertEquals(Difficulty.EASY, found.getDifficulty(), "Difficulty should match");
        assertEquals(10, found.getTimeTakenMin(), "Time taken should match");
    }

    @Test
    void testGetProblemsForUser() throws SQLException {
        Problem problem = new Problem(0, "Test User Problem", Platform.CODECHEF, Difficulty.MEDIUM, 25, LocalDateTime.now(), "User test", "http://test.com", 1);
        problemDao.saveProblem(problem);
        
        List<Problem> userProblems = problemDao.getAllProblemsForUser(1);
        assertFalse(userProblems.isEmpty(), "User problems list should not be empty");
        
        Problem found = userProblems.stream()
            .filter(p -> p.getName().equals("Test User Problem"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(found, "User problem should be found");
        assertEquals(1, found.getUserId(), "User ID should match");
    }
}
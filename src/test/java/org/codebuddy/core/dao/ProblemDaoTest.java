package org.codebuddy.core.dao;

import org.codebuddy.core.models.Difficulty;
import org.codebuddy.core.models.Platform;
import org.codebuddy.core.models.Problem;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProblemDaoTest {
    private ProblemDao problemDao;

    @BeforeEach
    void setup() throws SQLException {
        // Clear test data
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM problems");
        }
        problemDao = new ProblemDao();
    }

    @AfterEach
    void cleanup() throws SQLException {
        DatabaseManager.closeConnection();
    }

    @Test
    void testSaveAndRetrieveProblem() throws SQLException {
        Problem problem = new Problem(0, "Test Problem", Platform.LEETCODE, Difficulty.EASY, 10, java.time.LocalDate.now(), "Test notes", "http://test.com", 1);
        problemDao.saveProblem(problem);
        List<Problem> problems = problemDao.getAllProblems();
        assertFalse(problems.isEmpty(), "Problem list should not be empty");
        assertEquals("Two Sum", problems.get(0).getName(), "Problem name mismatch");
    }
}
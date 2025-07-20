package org.codebuddy.core.dao;

import org.codebuddy.core.models.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProblemDao {
    public void saveProblem(Problem problem) throws SQLException {
        String problemSql = "INSERT INTO problems (name, platform, difficulty, time_taken_min, solved_date, notes, link, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(problemSql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, problem.getName());
            stmt.setString(2, problem.getPlatform().name());
            stmt.setString(3, problem.getDifficulty().name());
            stmt.setInt(4, problem.getTimeTakenMin());
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(problem.getSolvedDate()));
            stmt.setString(6, problem.getNotes());
            stmt.setString(7, problem.getLink());
            stmt.setInt(8, problem.getUserId());
            stmt.executeUpdate();
        }
    }

    public void updateProblem(Problem problem) throws SQLException {
        String sql = "UPDATE problems SET name=?, platform=?, difficulty=?, time_taken_min=?, notes=?, link=?, user_id=? WHERE problem_id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, problem.getName());
            stmt.setString(2, problem.getPlatform().name());
            stmt.setString(3, problem.getDifficulty().name());
            stmt.setInt(4, problem.getTimeTakenMin());
            stmt.setString(5, problem.getNotes());
            stmt.setString(6, problem.getLink());
            stmt.setInt(7, problem.getUserId());
            stmt.setInt(8, problem.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteProblem(int problemId) throws SQLException {
        String sql = "DELETE FROM problems WHERE problem_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, problemId);
            stmt.executeUpdate();
        }
    }

    public List<Problem> getAllProblems() throws SQLException {
        String sql = "SELECT * FROM problems";
        List<Problem> problems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                problems.add(new Problem(
                        rs.getInt("problem_id"),
                        rs.getString("name"),
                        Platform.valueOf(rs.getString("platform")),
                        Difficulty.valueOf(rs.getString("difficulty")),
                        rs.getInt("time_taken_min"),
                        rs.getTimestamp("solved_date").toLocalDateTime(),
                        rs.getString("notes"),
                        rs.getString("link"),
                        rs.getInt("user_id")
                ));
            }
        }
        return problems;
    }

    public List<Problem> getAllProblemsForUser(int userId) throws SQLException {
        String sql = "SELECT * FROM problems WHERE user_id = ?";
        List<Problem> problems = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    problems.add(new Problem(
                            rs.getInt("problem_id"),
                            rs.getString("name"),
                            Platform.valueOf(rs.getString("platform")),
                            Difficulty.valueOf(rs.getString("difficulty")),
                            rs.getInt("time_taken_min"),
                            rs.getTimestamp("solved_date").toLocalDateTime(),
                            rs.getString("notes"),
                            rs.getString("link"),
                            rs.getInt("user_id")
                    ));
                }
            }
        }
        return problems;
    }

    public int getProblemsSolvedToday(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM problems WHERE user_id = ? AND solved_date = CURDATE()";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
    public int getCurrentStreak(int userId) throws SQLException {
        String sql = "SELECT solved_date FROM problems WHERE user_id = ? GROUP BY solved_date ORDER BY solved_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                int streak = 0;
                java.time.LocalDate today = java.time.LocalDate.now();
                while (rs.next()) {
                    java.time.LocalDate date = rs.getDate(1).toLocalDate();
                    if (date.equals(today.minusDays(streak))) {
                        streak++;
                    } else {
                        break;
                    }
                }
                return streak;
            }
        }
    }
}
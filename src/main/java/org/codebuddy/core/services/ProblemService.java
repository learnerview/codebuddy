// src/main/java/org/codebuddy/core/services/ProblemService.java
package org.codebuddy.core.services;

import org.codebuddy.core.dao.ProblemDao;
import org.codebuddy.core.models.Problem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.SQLException;
import java.util.List;

public class ProblemService {
    private final ProblemDao problemDao = new ProblemDao();
    private final ObservableList<Problem> problems = FXCollections.observableArrayList();

    public ProblemService() {
        try {
            refreshProblems();
        } catch (SQLException e) {
            // Handle error as needed
        }
    }

    public void addProblem(Problem problem) throws SQLException {
        problemDao.saveProblem(problem);
        refreshProblems();
    }

    public void deleteProblem(int problemId) throws SQLException {
        problemDao.deleteProblem(problemId);
        refreshProblems();
    }

    public void updateProblem(Problem problem) throws SQLException {
        problemDao.updateProblem(problem);
        refreshProblems();
    }

    public List<Problem> getAllProblems() throws SQLException {
        return problemDao.getAllProblems();
    }

    public List<Problem> getAllProblemsForUser(int userId) throws SQLException {
        return problemDao.getAllProblemsForUser(userId);
    }

    public ObservableList<Problem> getObservableProblems() {
        return problems;
    }

    public void refreshProblems() throws SQLException {
        problems.setAll(problemDao.getAllProblems());
    }

    public int getProblemsSolvedToday(int userId) throws SQLException {
        return problemDao.getProblemsSolvedToday(userId);
    }
    public int getCurrentStreak(int userId) throws SQLException {
        return problemDao.getCurrentStreak(userId);
    }
}
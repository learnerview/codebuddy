package org.codebuddy.gui;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.codebuddy.core.models.Problem;
import org.codebuddy.core.models.Platform;
import org.codebuddy.core.models.Difficulty;
import org.codebuddy.core.models.User;
import org.codebuddy.core.services.ProblemService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javafx.stage.Stage;

public class AnalyticsViewController {
    @FXML private PieChart platformPieChart;
    @FXML private PieChart difficultyPieChart;
    @FXML private BarChart<String, Number> problemsPerMonthChart;
    @FXML private TableView<Problem> recentProblemsTable;
    @FXML private TableColumn<Problem, String> nameColumn;
    @FXML private TableColumn<Problem, String> platformColumn;
    @FXML private TableColumn<Problem, String> difficultyColumn;
    @FXML private TableColumn<Problem, String> dateColumn;
    @FXML private TableColumn<Problem, Integer> timeColumn;
    @FXML private TableColumn<Problem, String> notesColumn;
    @FXML private TableColumn<Problem, String> linkColumn;
    @FXML private Label streakLabel;
    @FXML private Label totalSolvedLabel;
    @FXML private Button refreshButton;
    @FXML private Button closeButton;
    @FXML private ComboBox<Integer> recentRowsCombo;

    private ProblemService problemService;
    private User loggedInUser;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int TABLE_ROW_HEIGHT = 32;
    private static final int TABLE_HEADER_HEIGHT = 32;

    public void setLoggedInUser(User user, ProblemService service) {
        this.loggedInUser = user;
        this.problemService = service;
        refreshAnalytics();
    }

    @FXML
    public void initialize() {
        setupRecentProblemsTable();
        if (recentRowsCombo != null) {
            recentRowsCombo.getItems().setAll(5, 10, 15, 20);
            recentRowsCombo.getSelectionModel().select(Integer.valueOf(5));
            recentRowsCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshAnalytics());
        }
        if (refreshButton != null) refreshButton.setOnAction(e -> refreshAnalytics());
        if (closeButton != null) closeButton.setOnAction(e -> closeWindow());
    }

    private void refreshAnalytics() {
        try {
            List<Problem> userProblems = getUserProblems();
            populatePlatformPieChart(userProblems);
            populateDifficultyPieChart(userProblems);
            populateProblemsPerMonthChart(userProblems);
            int numRows = 5;
            if (recentRowsCombo != null && recentRowsCombo.getValue() != null) {
                numRows = recentRowsCombo.getValue();
            }
            populateRecentProblemsTable(userProblems, numRows);
            updateStreakAndTotal(userProblems);
        } catch (Exception e) {
            showError("Failed to load analytics", e.getMessage());
        }
    }

    private List<Problem> getUserProblems() {
        if (problemService == null || loggedInUser == null) return Collections.emptyList();
        return problemService.getObservableProblems().stream()
                .filter(p -> p.getUserId() == loggedInUser.getId())
                .sorted(Comparator.comparing(Problem::getSolvedDate).reversed())
                .collect(Collectors.toList());
    }

    private void populatePlatformPieChart(List<Problem> problems) {
        Map<String, Long> byPlatform = problems.stream().collect(Collectors.groupingBy(
                p -> p.getPlatform().getDisplayName(), Collectors.counting()));
        long total = problems.size();
        platformPieChart.getData().clear();
        if (total == 0) {
            platformPieChart.setTitle("No Data");
            return;
        }
        platformPieChart.setTitle("Problems by Platform");
        byPlatform.forEach((platform, count) -> {
            double percent = (count * 100.0 / total);
            PieChart.Data data = new PieChart.Data(platform + String.format(" (%.1f%%)", percent), count);
            platformPieChart.getData().add(data);
        });
    }

    private void populateDifficultyPieChart(List<Problem> problems) {
        Map<String, Long> byDifficulty = problems.stream().collect(Collectors.groupingBy(
                p -> p.getDifficulty().getDisplayName(), Collectors.counting()));
        long total = problems.size();
        difficultyPieChart.getData().clear();
        if (total == 0) {
            difficultyPieChart.setTitle("No Data");
            return;
        }
        difficultyPieChart.setTitle("Problems by Difficulty");
        byDifficulty.forEach((difficulty, count) -> {
            double percent = (count * 100.0 / total);
            PieChart.Data data = new PieChart.Data(difficulty + String.format(" (%.1f%%)", percent), count);
            difficultyPieChart.getData().add(data);
        });
    }

    private void populateProblemsPerMonthChart(List<Problem> problems) {
        Map<String, Long> perMonth = problems.stream().collect(Collectors.groupingBy(
                p -> p.getSolvedDate().getYear() + "-" + String.format("%02d", p.getSolvedDate().getMonthValue()), Collectors.counting()));
        List<String> last12Months = getLast12Months();
        problemsPerMonthChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (String month : last12Months) {
            long count = perMonth.getOrDefault(month, 0L);
            series.getData().add(new XYChart.Data<>(month, count));
        }
        problemsPerMonthChart.getData().add(series);
    }

    private List<String> getLast12Months() {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 11; i >= 0; i--) {
            LocalDate d = now.minusMonths(i);
            months.add(d.getYear() + "-" + String.format("%02d", d.getMonthValue()));
        }
        return months;
    }

    private void populateRecentProblemsTable(List<Problem> problems, int numRows) {
        List<Problem> limited = new ArrayList<>(problems.stream().limit(numRows).collect(Collectors.toList()));
        // Fill with empty Problem objects if needed
        while (limited.size() < numRows) {
            limited.add(new Problem());
        }
        ObservableList<Problem> recent = FXCollections.observableArrayList(limited);
        recentProblemsTable.setItems(recent);
        int rowCount = Math.max(recent.size(), numRows);
        recentProblemsTable.setPrefHeight(TABLE_HEADER_HEIGHT + rowCount * TABLE_ROW_HEIGHT);
    }

    private void setupRecentProblemsTable() {
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() != null && cell.getValue().getName() != null ? cell.getValue().getName() : ""
        ));
        platformColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() != null && cell.getValue().getPlatform() != null ? cell.getValue().getPlatform().getDisplayName() : ""
        ));
        difficultyColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() != null && cell.getValue().getDifficulty() != null ? cell.getValue().getDifficulty().getDisplayName() : ""
        ));
        dateColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() != null && cell.getValue().getSolvedDate() != null ? cell.getValue().getSolvedDate().format(DATE_TIME_FORMATTER) : ""
        ));
        timeColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(
            cell.getValue() != null && cell.getValue().getTimeTakenMin() != 0 ? cell.getValue().getTimeTakenMin() : 0
        ).asObject());
        notesColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() != null && cell.getValue().getNotes() != null ? cell.getValue().getNotes() : ""
        ));
        linkColumn.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue() != null && cell.getValue().getLink() != null ? cell.getValue().getLink() : ""
        ));
        linkColumn.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    String url = link.getText();
                    if (url != null && !url.isBlank()) {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                        } catch (Exception ex) {
                            // Ignore
                        }
                    }
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                } else {
                    link.setText(item);
                    setGraphic(link);
                }
            }
        });
    }

    private void updateStreakAndTotal(List<Problem> problems) {
        // Streak: consecutive days with at least one problem solved
        List<LocalDate> dates = problems.stream()
                .map(p -> p.getSolvedDate().toLocalDate())
                .distinct()
                .sorted()
                .toList();
        int streak = 0, maxStreak = 0;
        LocalDate prev = null;
        for (LocalDate d : dates) {
            if (prev == null || d.equals(prev.plusDays(1))) {
                streak++;
            } else {
                streak = 1;
            }
            if (streak > maxStreak) maxStreak = streak;
            prev = d;
        }
        streakLabel.setText("Current Streak: " + streak + " day" + (streak == 1 ? "" : "s"));
        totalSolvedLabel.setText("Total Solved: " + problems.size());
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String header, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }
} 
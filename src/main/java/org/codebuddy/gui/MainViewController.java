package org.codebuddy.gui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.codebuddy.core.models.*;
import org.codebuddy.core.services.ProblemService;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Alert;
import org.codebuddy.core.models.User;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.*;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.time.LocalDateTime;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.util.Stack;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import javafx.stage.Window;

public class MainViewController {
    @FXML private MenuItem addProblemMenuItem;
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem reloadMenuItem;
    @FXML private MenuItem analyticsMenuItem;
    @FXML private MenuItem themeMenuItem;
    @FXML private ListView<String> platformListView;
    @FXML private ListView<String> difficultyListView;
    @FXML private TableView<Problem> problemTable;
    @FXML private TableColumn<Problem, String> nameColumn;
    @FXML private TableColumn<Problem, String> platformColumn;
    @FXML private TableColumn<Problem, String> difficultyColumn;
    @FXML private TableColumn<Problem, Integer> timeColumn;
    @FXML private TableColumn<Problem, String> dateColumn;
    @FXML private TableColumn<Problem, String> notesColumn;
    @FXML private TableColumn<Problem, String> linkColumn;
    @FXML private TableColumn<Problem, Void> actionsColumn;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private Label progressLabel;
    @FXML private Label streakLabel;
    @FXML private Label loggedInUserLabel;
    @FXML private Button logoutButton;
    private FilteredList<Problem> filteredList;

    private final ProblemService problemService = new ProblemService();
    private final ObservableList<Problem> problems = FXCollections.observableArrayList();
    private final ObservableList<Problem> filteredProblems = FXCollections.observableArrayList();
    private boolean darkTheme = false;
    private static final String LIGHT_STYLESHEET = "/fxml/style.css";
    private static final String DARK_STYLESHEET = "/fxml/dark-style.css";
    private User loggedInUser;
    private boolean darkMode = false;
    @FXML private MenuItem toggleDarkModeMenuItem;
    @FXML private MenuItem clearFiltersMenuItem;
    @FXML private Button toggleDarkModeButton;
    @FXML private Button clearFiltersButton;
    @FXML private Button searchButton;
    private String lastSearchText = "";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Problem lastDeletedProblem;
    private PauseTransition undoTimer;
    @FXML private Button undoButton;
    @FXML private Button redoButton;

    private Stack<Action> undoStack = new Stack<>();
    private Stack<Action> redoStack = new Stack<>();

    private enum ActionType { ADD, DELETE, EDIT }
    private static class Action {
        ActionType type;
        Problem before;
        Problem after;
        Action(ActionType type, Problem before, Problem after) {
            this.type = type;
            this.before = before;
            this.after = after;
        }
    }

    @FXML private HBox snackbar;
    @FXML private Label snackbarLabel;
    @FXML private Button snackbarActionBtn;
    private PauseTransition snackbarTimer;
    @FXML private Button highContrastButton;
    private boolean highContrast = false;
    private static final String HIGH_CONTRAST_STYLESHEET = "/fxml/high-contrast.css";
    @FXML private MenuItem keyboardShortcutsMenuItem;
    @FXML private MenuItem sendFeedbackMenuItem;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        if (loggedInUserLabel != null && user != null) {
            loggedInUserLabel.setText("Logged in as: " + user.getUsername());
        }
        loadProblemsForUser();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadFilters();
        loadProblems();
        setupFilterListeners();
        statusLabel.setText("Ready");
        if (searchField != null) {
            searchField.setOnAction(e -> handleSearch());
            searchField.requestFocus();
        }
        lastSearchText = "";
        addButtonEffects(toggleDarkModeButton);
        addButtonEffects(clearFiltersButton);
        addButtonEffects(searchButton);
        if (undoButton != null) undoButton.setOnAction(e -> handleUndo());
        if (redoButton != null) redoButton.setOnAction(e -> handleRedo());
        if (snackbarActionBtn != null) snackbarActionBtn.setOnAction(e -> {
            if (snackbarAction != null) snackbarAction.run();
            hideSnackbar();
        });
        if (highContrastButton != null) highContrastButton.setOnAction(e -> handleHighContrastToggle());
        if (keyboardShortcutsMenuItem != null) keyboardShortcutsMenuItem.setOnAction(e -> showKeyboardShortcutsDialog());
        if (sendFeedbackMenuItem != null) sendFeedbackMenuItem.setOnAction(e -> showSendFeedbackDialog());
    }

    private Runnable snackbarAction;
    private void showSnackbar(String message) {
        showSnackbar(message, null, null);
    }
    private void showSnackbar(String message, String actionText, Runnable action) {
        snackbarLabel.setText(message);
        snackbar.setVisible(true);
        snackbar.setManaged(true);
        snackbar.setStyle("-fx-background-color: #323232; -fx-padding: 10; -fx-background-radius: 8; -fx-opacity: 1; -fx-translate-y: 0;");
        if (actionText != null && action != null) {
            snackbarActionBtn.setText(actionText);
            snackbarActionBtn.setVisible(true);
            snackbarActionBtn.setManaged(true);
            snackbarAction = action;
        } else {
            snackbarActionBtn.setVisible(false);
            snackbarActionBtn.setManaged(false);
            snackbarAction = null;
        }
        if (snackbarTimer != null) snackbarTimer.stop();
        snackbarTimer = new PauseTransition(Duration.seconds(3));
        snackbarTimer.setOnFinished(e -> hideSnackbar());
        snackbarTimer.play();
    }
    private void hideSnackbar() {
        snackbar.setVisible(false);
        snackbar.setManaged(false);
        snackbarLabel.setText("");
        snackbarActionBtn.setVisible(false);
        snackbarActionBtn.setManaged(false);
        snackbarAction = null;
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        platformColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPlatform().getDisplayName()));
        difficultyColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDifficulty().getDisplayName()));
        timeColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getTimeTakenMin()).asObject());
        dateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSolvedDate().format(DATE_TIME_FORMATTER)));
        notesColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNotes()));
        linkColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getLink()));
        linkColumn.setCellFactory(col -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(e -> {
                    String url = link.getText();
                    if (url != null && !url.isBlank()) {
                        try {
                            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                        } catch (Exception ex) {
                            // Optionally show error
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
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5, editBtn, deleteBtn);
            {
                editBtn.setOnAction(e -> {
                    Problem problem = getTableView().getItems().get(getIndex());
                    handleEditProblem(problem);
                });
                deleteBtn.setOnAction(e -> {
                    Problem problem = getTableView().getItems().get(getIndex());
                    handleDeleteProblem(problem);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
        problemTable.setItems(filteredProblems);
        // Highlighting for search
        nameColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(highlightMatch(item, lastSearchText));
                }
            }
        });
        platformColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(highlightMatch(item, lastSearchText));
                }
            }
        });
    }

    private void loadFilters() {
        platformListView.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(org.codebuddy.core.models.Platform.values()).map(org.codebuddy.core.models.Platform::getDisplayName).collect(Collectors.toList())
        ));
        difficultyListView.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(Difficulty.values()).map(Difficulty::getDisplayName).collect(Collectors.toList())
        ));
    }

    private void loadProblems() {
        try {
            problems.setAll(problemService.getAllProblems());
            applyFilters();
            statusLabel.setText("Loaded " + problems.size() + " problems");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load problems");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadProblemsForUser() {
        try {
            List<Problem> problems = problemService.getAllProblemsForUser(loggedInUser.getId());
            System.out.println("[MainViewController] Problems loaded for user " + loggedInUser.getUsername() + ":");
            for (Problem p : problems) {
                System.out.println("  - " + p.getName() + " (" + p.getPlatform() + ", " + p.getDifficulty() + ", " + p.getSolvedDate() + ")");
            }
            filteredList = new FilteredList<>(FXCollections.observableArrayList(problems), p -> true);
            problemTable.setItems(filteredList);
            setupSearchFilter();
            updateProgressAndStreak();
            statusLabel.setText("Loaded " + problems.size() + " problems for user " + loggedInUser.getUsername());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load problems for user");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void updateProgressAndStreak() {
        try {
            int today = problemService.getProblemsSolvedToday(loggedInUser.getId());
            int streak = problemService.getCurrentStreak(loggedInUser.getId());
            progressLabel.setText("Problems solved today: " + today);
            streakLabel.setText("Current streak: " + streak + (streak == 1 ? " day" : " days"));
        } catch (Exception e) {
            progressLabel.setText("Problems solved today: ?");
            streakLabel.setText("Current streak: ?");
        }
    }

    private void setupFilterListeners() {
        platformListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyCombinedFilters());
        difficultyListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyCombinedFilters());
    }

    private void applyFilters() {
        String selectedPlatform = platformListView.getSelectionModel().getSelectedItem();
        String selectedDifficulty = difficultyListView.getSelectionModel().getSelectedItem();
        filteredProblems.setAll(problems.stream().filter(p ->
                (selectedPlatform == null || p.getPlatform().getDisplayName().equals(selectedPlatform)) &&
                (selectedDifficulty == null || p.getDifficulty().getDisplayName().equals(selectedDifficulty))
        ).collect(Collectors.toList()));
    }

    private void applyCombinedFilters() {
        String selectedPlatform = platformListView.getSelectionModel().getSelectedItem();
        String selectedDifficulty = difficultyListView.getSelectionModel().getSelectedItem();
        String searchText = lastSearchText.toLowerCase();
        try {
            List<Problem> problems = problemService.getAllProblemsForUser(loggedInUser.getId());
            List<Problem> filtered = problems.stream().filter(p ->
                (selectedPlatform == null || p.getPlatform().getDisplayName().equals(selectedPlatform)) &&
                (selectedDifficulty == null || p.getDifficulty().getDisplayName().equals(selectedDifficulty)) &&
                (searchText.isEmpty() || p.getName().toLowerCase().contains(searchText) || p.getPlatform().name().toLowerCase().contains(searchText))
            ).sorted((a, b) -> {
                boolean aExact = a.getName().equalsIgnoreCase(searchText) || a.getPlatform().name().equalsIgnoreCase(searchText);
                boolean bExact = b.getName().equalsIgnoreCase(searchText) || b.getPlatform().name().equalsIgnoreCase(searchText);
                if (aExact && !bExact) return -1;
                if (!aExact && bExact) return 1;
                return 0;
            }).toList();
            filteredList = new FilteredList<>(FXCollections.observableArrayList(filtered), p -> true);
            problemTable.setItems(filteredList);
            statusLabel.setText("Filters/search applied");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Filter/search failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lower = newVal.toLowerCase();
            filteredList.setPredicate(problem ->
                problem.getName().toLowerCase().contains(lower) ||
                problem.getPlatform().name().toLowerCase().contains(lower)
            );
        });
    }

    @FXML
    private void handleAddProblem(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AddProblemDialog.fxml"));
            Parent root = loader.load();
            AddProblemDialogController controller = loader.getController();
            controller.setLoggedInUser(loggedInUser);
            controller.setProblemService(problemService);
            Stage dialogStage = new Stage();
            controller.setDialogStage(dialogStage);
            dialogStage.setTitle("Add New Problem");
            Scene scene = new Scene(root, 400, 420);
            scene.getStylesheets().add(getClass().getResource(LIGHT_STYLESHEET).toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            if (controller.isSaved()) {
                Problem added = controller.getSavedProblem();
                if (added != null) {
                    undoStack.push(new Action(ActionType.ADD, null, added));
                    redoStack.clear();
                }
                loadProblemsForUser();
                statusLabel.setText("Problem added successfully");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to add problem");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void handleReload(ActionEvent event) {
        loadProblems();
    }

    @FXML
    private void handleAnalytics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AnalyticsView.fxml"));
            Parent root = loader.load();
            AnalyticsViewController analyticsController = loader.getController();
            analyticsController.setLoggedInUser(loggedInUser, problemService);
            Stage stage = new Stage();
            stage.setTitle("Analytics & Reports");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 900, 600);
            // Inherit current theme
            if (highContrast) {
                scene.getStylesheets().add(getClass().getResource(HIGH_CONTRAST_STYLESHEET).toExternalForm());
            } else if (darkMode) {
                scene.getStylesheets().add(getClass().getResource("/fxml/dark-style.css").toExternalForm());
            } else {
                scene.getStylesheets().add(getClass().getResource(LIGHT_STYLESHEET).toExternalForm());
            }
            stage.setScene(scene);
            stage.setMinWidth(700);
            stage.setMinHeight(500);
            stage.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to open Analytics");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void handleThemeToggle(ActionEvent event) {
        Scene scene = themeMenuItem.getParentPopup().getOwnerWindow().getScene();
        if (darkTheme) {
            scene.getStylesheets().remove(getClass().getResource(DARK_STYLESHEET).toExternalForm());
            scene.getStylesheets().add(getClass().getResource(LIGHT_STYLESHEET).toExternalForm());
            darkTheme = false;
        } else {
            scene.getStylesheets().remove(getClass().getResource(LIGHT_STYLESHEET).toExternalForm());
            scene.getStylesheets().add(getClass().getResource(DARK_STYLESHEET).toExternalForm());
            darkTheme = true;
        }
    }

    @FXML
    private void handleExportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Problems to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(problemTable.getScene().getWindow());
        if (file == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Name,Platform,Difficulty,TimeTaken,Date,Notes,Link\n");
            for (Problem p : filteredList) {
                writer.write(String.format("%s,%s,%s,%d,%s,%s,%s\n",
                        escapeCsv(p.getName()),
                        escapeCsv(p.getPlatform().name()),
                        escapeCsv(p.getDifficulty().name()),
                        p.getTimeTakenMin(),
                        p.getSolvedDate(),
                        escapeCsv(p.getNotes()),
                        escapeCsv(p.getLink())
                ));
            }
            statusLabel.setText("Exported to " + file.getAbsolutePath());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Export failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void handleImportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Problems from CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(problemTable.getScene().getWindow());
        if (file == null) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            int imported = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length < 7) continue;
                Problem p = new Problem(0,
                        parts[0],
                        org.codebuddy.core.models.Platform.valueOf(parts[1]),
                        Difficulty.valueOf(parts[2]),
                        Integer.parseInt(parts[3]),
                        LocalDateTime.parse(parts[4]),
                        parts[5],
                        parts[6],
                        loggedInUser.getId()
                );
                problemService.addProblem(p);
                imported++;
            }
            loadProblemsForUser();
            statusLabel.setText("Imported " + imported + " problems from CSV");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Import failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return '"' + s + '"';
        }
        return s;
    }
    private String[] parseCsvLine(String line) {
        // Simple CSV parser for demo (does not handle all edge cases)
        return line.split(",", -1);
    }

    private void handleEditProblem(Problem problem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProblemDialog.fxml"));
            Parent root = loader.load();
            EditProblemDialogController controller = loader.getController();
            controller.setDialogStage(new Stage());
            controller.setProblem(problem);
            controller.setLoggedInUser(loggedInUser);
            controller.setProblemService(problemService);
            Stage dialogStage = controller.getDialogStage();
            dialogStage.setTitle("Edit Problem");
            Scene scene = new Scene(root, 400, 420);
            scene.getStylesheets().add(getClass().getResource(LIGHT_STYLESHEET).toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            if (controller.isSaved()) {
                Problem before = problem;
                Problem after = controller.getSavedProblem();
                if (after != null) {
                    undoStack.push(new Action(ActionType.EDIT, before, after));
                    redoStack.clear();
                }
                loadProblemsForUser();
                statusLabel.setText("Problem updated successfully");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to edit problem");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleDeleteProblem(Problem problem) {
        if (!confirmDelete()) return;
        try {
            problemService.deleteProblem(problem.getId());
            lastDeletedProblem = problem;
            undoStack.push(new Action(ActionType.DELETE, problem, null));
            redoStack.clear();
            showSnackbar("Problem deleted.", "Undo", this::undoDelete);
        } catch (Exception e) {
            showSnackbar("Delete failed: " + e.getMessage());
        }
    }

    private void undoDelete() {
        if (lastDeletedProblem != null) {
            try {
                problemService.addProblem(lastDeletedProblem);
                undoStack.push(new Action(ActionType.ADD, null, lastDeletedProblem));
                redoStack.clear();
                showSnackbar("Delete undone.");
                lastDeletedProblem = null;
                if (undoTimer != null) undoTimer.stop();
            } catch (Exception e) {
                showSnackbar("Undo failed: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUndo() {
        if (undoStack.isEmpty()) return;
        Action action = undoStack.pop();
        try {
            switch (action.type) {
                case ADD -> {
                    if (action.after != null) {
                        problemService.deleteProblem(action.after.getId());
                        redoStack.push(new Action(ActionType.ADD, null, action.after));
                    }
                }
                case DELETE -> {
                    if (action.before != null) {
                        problemService.addProblem(action.before);
                        redoStack.push(new Action(ActionType.DELETE, action.before, null));
                    }
                }
                case EDIT -> {
                    if (action.before != null) {
                        problemService.updateProblem(action.before);
                        redoStack.push(new Action(ActionType.EDIT, action.after, action.before));
                    }
                }
            }
            showSnackbar("Undo performed.");
        } catch (Exception e) {
            showSnackbar("Undo failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRedo() {
        if (redoStack.isEmpty()) return;
        Action action = redoStack.pop();
        try {
            switch (action.type) {
                case ADD -> {
                    if (action.after != null) {
                        problemService.addProblem(action.after);
                        undoStack.push(new Action(ActionType.ADD, null, action.after));
                    }
                }
                case DELETE -> {
                    if (action.before != null) {
                        problemService.deleteProblem(action.before.getId());
                        undoStack.push(new Action(ActionType.DELETE, action.before, null));
                    }
                }
                case EDIT -> {
                    if (action.after != null) {
                        problemService.updateProblem(action.after);
                        undoStack.push(new Action(ActionType.EDIT, action.before, action.after));
                    }
                }
            }
            showSnackbar("Redo performed.");
        } catch (Exception e) {
            showSnackbar("Redo failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleToggleDarkMode() {
        darkMode = !darkMode;
        String darkStylesheet = getClass().getResource("/fxml/dark-style.css").toExternalForm();
        if (darkMode) {
            applyStylesheetToAllWindows(darkStylesheet, true);
            toggleDarkModeButton.setText("Enable Light Mode");
            showSnackbar("Dark mode enabled");
        } else {
            applyStylesheetToAllWindows(darkStylesheet, false);
            toggleDarkModeButton.setText("Enable Dark Mode");
            showSnackbar("Light mode enabled");
        }
    }
    @FXML
    private void handleClearFilters() {
        platformListView.getSelectionModel().clearSelection();
        difficultyListView.getSelectionModel().clearSelection();
        if (searchField != null) searchField.clear();
        filteredList.setPredicate(p -> true);
        statusLabel.setText("Filters cleared");
    }
    @FXML
    private void handleSearch() {
        lastSearchText = searchField.getText().trim();
        applyCombinedFilters();
    }

    private TextFlow highlightMatch(String text, String search) {
        if (search == null || search.isEmpty()) return new TextFlow(new Text(text));
        String lower = text.toLowerCase();
        String searchLower = search.toLowerCase();
        int idx = lower.indexOf(searchLower);
        if (idx < 0) return new TextFlow(new Text(text));
        Text before = new Text(text.substring(0, idx));
        Text match = new Text(text.substring(idx, idx + search.length()));
        match.setFill(Color.ORANGE);
        match.setStyle("-fx-font-weight: bold");
        Text after = new Text(text.substring(idx + search.length()));
        return new TextFlow(before, match, after);
    }

    private void addButtonEffects(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #444; -fx-text-fill: #fff;"));
        btn.setOnMouseExited(e -> btn.setStyle(""));
        btn.setOnMousePressed(e -> btn.setStyle("-fx-background-color: #222; -fx-text-fill: #fff;"));
        btn.setOnMouseReleased(e -> btn.setStyle("-fx-background-color: #444; -fx-text-fill: #fff;"));
    }

    // Add confirmation dialog for delete (if you have delete logic)
    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this problem?");
        alert.setContentText("This action cannot be undone.");
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    @FXML
    private void handleLogout() {
        // Close current window
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
        try {
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginRoot = loginLoader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot));
            loginStage.setTitle("Login / Signup");
            loginStage.initModality(Modality.APPLICATION_MODAL);
            LoginController loginController = loginLoader.getController();
            loginController.setStage(loginStage);
            loginStage.showAndWait();
            User loggedInUser = loginController.getLoggedInUser();
            if (loggedInUser != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
                Parent root = loader.load();
                MainViewController mainController = loader.getController();
                mainController.setLoggedInUser(loggedInUser);
                Stage mainStage = new Stage();
                Scene scene = new Scene(root, 1000, 700);
                scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());
                mainStage.setScene(scene);
                mainStage.setTitle("CodeBuddy – Competitive Practice Tracker");
                mainStage.setMinWidth(800);
                mainStage.setMinHeight(600);
                mainStage.show();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to log out");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleExportJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Problems to JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(problemTable.getScene().getWindow());
        if (file == null) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            // Export only filtered problems
            mapper.writeValue(file, filteredList);
            showSnackbar("Exported to " + file.getAbsolutePath());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Export failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleImportJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Problems from JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(problemTable.getScene().getWindow());
        if (file == null) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            int imported = 0;
            // Read as List<Problem>
            java.util.List<Problem> problems = mapper.readValue(file, new TypeReference<java.util.List<Problem>>(){});
            for (Problem p : problems) {
                problemService.addProblem(p);
                imported++;
            }
            loadProblemsForUser();
            showSnackbar("Imported " + imported + " problems from JSON");
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Import failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleHighContrastToggle() {
        String hcStylesheet = getClass().getResource(HIGH_CONTRAST_STYLESHEET).toExternalForm();
        if (!highContrast) {
            applyStylesheetToAllWindows(hcStylesheet, true);
            highContrastButton.setText("Disable High Contrast");
            showSnackbar("High contrast mode enabled");
        } else {
            applyStylesheetToAllWindows(hcStylesheet, false);
            highContrastButton.setText("Enable High Contrast");
            showSnackbar("High contrast mode disabled");
        }
        highContrast = !highContrast;
    }

    private void showKeyboardShortcutsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Keyboard Shortcuts");
        alert.setHeaderText("Keyboard Navigation & Shortcuts");
        alert.setContentText(
            "Tab/Shift+Tab: Move focus between controls\n" +
            "Enter/Space: Activate focused button\n" +
            "Ctrl+Z: Undo\n" +
            "Ctrl+Y: Redo\n" +
            "Ctrl+F: Focus search field\n" +
            "Ctrl+N: Add new problem\n" +
            "Ctrl+E: Edit selected problem\n" +
            "Ctrl+D: Delete selected problem\n" +
            "Ctrl+H: Toggle high contrast mode\n" +
            "Ctrl+M: Toggle dark mode\n" +
            "Esc: Close dialogs\n"
        );
        alert.showAndWait();
    }

    private void showSendFeedbackDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Send Feedback");
        dialog.setHeaderText("We value your feedback! Please share bugs, suggestions, or comments.");
        ButtonType submitType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ChoiceBox<String> typeBox = new ChoiceBox<>();
        typeBox.getItems().addAll("Bug", "Suggestion", "Other");
        typeBox.setValue("Bug");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe your feedback...");
        descArea.setPrefRowCount(5);
        TextField contactField = new TextField();
        contactField.setPromptText("(Optional) Your email or contact");

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Contact:"), 0, 2);
        grid.add(contactField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitType) {
                String type = typeBox.getValue();
                String desc = descArea.getText();
                String contact = contactField.getText();
                if (desc == null || desc.isBlank()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Description cannot be empty.");
                    alert.showAndWait();
                    return null;
                }
                saveFeedback(type, desc, contact);
                showSnackbar("Thank you for your feedback!");
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void saveFeedback(String type, String desc, String contact) {
        try (FileWriter writer = new FileWriter("user_feedback.txt", true)) {
            writer.write("--- Feedback ---\n");
            writer.write("Time: " + LocalDateTime.now() + "\n");
            writer.write("Type: " + type + "\n");
            writer.write("Description: " + desc + "\n");
            writer.write("Contact: " + (contact == null ? "" : contact) + "\n");
            writer.write("----------------\n\n");
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save feedback: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void applyStylesheetToAllWindows(String stylesheet, boolean add) {
        for (Window window : Window.getWindows()) {
            if (window.getScene() != null) {
                if (add && !window.getScene().getStylesheets().contains(stylesheet)) {
                    window.getScene().getStylesheets().add(stylesheet);
                } else if (!add) {
                    window.getScene().getStylesheets().remove(stylesheet);
                }
            }
        }
    }
} 
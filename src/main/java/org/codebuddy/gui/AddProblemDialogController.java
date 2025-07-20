package org.codebuddy.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.codebuddy.core.models.Platform;
import org.codebuddy.core.models.Difficulty;
import org.codebuddy.core.models.Problem;
import org.codebuddy.core.services.ProblemService;
import javafx.util.StringConverter;
import javafx.scene.control.SpinnerValueFactory;
import java.time.LocalDate;
import org.codebuddy.core.models.User;
import java.time.LocalDateTime;
import java.net.MalformedURLException;
import java.net.URL;

public class AddProblemDialogController {
    @FXML private TextField nameField;
    @FXML private ComboBox<Platform> platformCombo;
    @FXML private ComboBox<Difficulty> difficultyCombo;
    @FXML private Spinner<Integer> timeSpinner;
    @FXML private TextArea notesField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private TextField linkField;

    private ProblemService problemService;
    private Stage dialogStage;
    private boolean saved = false;
    private Problem savedProblem;
    private User loggedInUser;

    public void setLoggedInUser(User user) { this.loggedInUser = user; }
    public void setProblemService(ProblemService service) { this.problemService = service; }

    @FXML
    public void initialize() {
        platformCombo.getItems().addAll(Platform.values());
        platformCombo.setConverter(new StringConverter<Platform>() {
            public String toString(Platform p) { return p == null ? "" : p.getDisplayName(); }
            public Platform fromString(String s) { return null; }
        });
        difficultyCombo.getItems().addAll(Difficulty.values());
        difficultyCombo.setConverter(new StringConverter<Difficulty>() {
            public String toString(Difficulty d) { return d == null ? "" : d.getDisplayName(); }
            public Difficulty fromString(String s) { return null; }
        });
        timeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, 30));
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }

    public void setDialogStage(Stage stage) { this.dialogStage = stage; }
    public boolean isSaved() { return saved; }
    public Problem getSavedProblem() { return savedProblem; }

    private void handleSave() {
        String name = nameField.getText();
        Platform platform = platformCombo.getValue();
        Difficulty difficulty = difficultyCombo.getValue();
        Integer timeTaken = timeSpinner.getValue();
        String notes = notesField.getText();
        String link = linkField.getText();
        if (name == null || name.isBlank() || platform == null || difficulty == null || timeTaken == null) {
            showAlert("Please fill all required fields.");
            return;
        }
        if (timeTaken <= 0) {
            showAlert("Time taken must be a positive number.");
            return;
        }
        if (link != null && !link.isBlank()) {
            try {
                new URL(link);
            } catch (MalformedURLException e) {
                showAlert("Link must be a valid URL.");
                return;
            }
        }
        Problem problem = new Problem(0, name, platform, difficulty, timeTaken, LocalDateTime.now(), notes == null ? "" : notes, link == null ? "" : link, loggedInUser.getId());
        try {
            problemService.addProblem(problem);
            savedProblem = problem;
            saved = true;
            dialogStage.close();
        } catch (Exception ex) {
            showAlert("Failed to save: " + ex.getMessage());
        }
    }

    private void handleCancel() {
        saved = false;
        dialogStage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
} 
// src/main/java/org/codebuddy/gui/GUIApp.java
package org.codebuddy.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import org.codebuddy.core.models.*;
import org.codebuddy.core.services.ProblemService;
import org.codebuddy.core.models.User;
import org.codebuddy.core.services.UserService;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDateTime;

public class GUIApp extends Application {
    private final ProblemService service = new ProblemService();

    @Override
    public void start(Stage stage) throws Exception {
        // Ensure at least one user exists
        UserService userService = new UserService();
        try {
            if (!userService.usernameExists("admin")) {
                userService.registerUser("admin", "admin123", "admin@example.com");
                System.out.println("Default admin user created: admin / admin123");
            }
        } catch (Exception e) {
            System.out.println("Error creating default admin user: " + e.getMessage());
        }
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
        System.out.println("Login dialog closed. User: " + loggedInUser);
        if (loggedInUser == null) {
            System.out.println("No user logged in. Exiting app.");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText("No user logged in");
            alert.setContentText("Login failed or was cancelled. The app will now exit.\nTry logging in as admin/admin123 if you have no users.");
            alert.showAndWait();
            System.exit(0);
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        MainViewController mainController = loader.getController();
        mainController.setLoggedInUser(loggedInUser);
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("CodeBuddy – Competitive Practice Tracker");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    private VBox createInputForm(User loggedInUser) {
        TextField nameField = new TextField();
        ComboBox<Platform> platformCombo = new ComboBox<>();
        ComboBox<Difficulty> difficultyCombo = new ComboBox<>();
        Spinner<Integer> timeSpinner = new Spinner<>(1, 300, 30);
        Button submitBtn = new Button("Save Problem");

        platformCombo.getItems().addAll(Platform.values());
        difficultyCombo.getItems().addAll(Difficulty.values());

        submitBtn.setOnAction(e -> {
            try {
                Problem problem = new Problem(
                        0,
                        nameField.getText(),
                        platformCombo.getValue(),
                        difficultyCombo.getValue(),
                        timeSpinner.getValue(),
                        LocalDateTime.now(),
                        "",
                        "",
                        loggedInUser.getId()
                );
                service.addProblem(problem);
                showAlert("Success", "Problem saved successfully!");
            } catch (SQLException ex) {
                showAlert("Error", "Failed to save: " + ex.getMessage());
            }
        });

        VBox form = new VBox(10,
                new Label("Problem Name:"), nameField,
                new Label("Platform:"), platformCombo,
                new Label("Difficulty:"), difficultyCombo,
                new Label("Time Taken (mins):"), timeSpinner,
                submitBtn
        );
        form.setPadding(new javafx.geometry.Insets(15));
        return form;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
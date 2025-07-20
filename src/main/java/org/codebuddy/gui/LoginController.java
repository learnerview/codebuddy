package org.codebuddy.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.codebuddy.core.models.User;
import org.codebuddy.core.services.UserService;
import java.util.function.Consumer;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();
    private Stage stage;
    private Consumer<User> onLoginSuccess;
    private User loggedInUser;

    public void setStage(Stage stage) { this.stage = stage; }
    public User getLoggedInUser() { return loggedInUser; }

    @FXML
    public void initialize() {
        System.out.println("[LoginController] initialize called. Controller instance: " + this);
        System.out.println("[LoginController] FXML root: " + usernameField.getParent().getParent());
    }

    @FXML
    private void handleLogin() {
        System.out.println("[LoginController] handleLogin called");
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            System.out.println("Attempting login for user: " + username);
            User user = userService.authenticateUser(username, password);
            System.out.println("User returned from authenticateUser: " + user);
            if (user != null) {
                System.out.println("Login successful for user: " + username);
                System.out.println("User details: id=" + user.getId() + ", username=" + user.getUsername() + ", email=" + user.getEmail());
                loggedInUser = user;
                System.out.println("loggedInUser set in controller: " + loggedInUser);
                stage.close();
            } else {
                System.out.println("Login failed for user: " + username);
                errorLabel.setText("Invalid username or password");
                showAlert("Login Failed", "Invalid username or password. Please try again or sign up.");
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            errorLabel.setText("Login error: " + e.getMessage());
            showAlert("Login Error", e.getMessage());
        }
    }

    @FXML
    private void handleSignup() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();
            System.out.println("Attempting signup for user: " + username);
            if (userService.usernameExists(username)) {
                errorLabel.setText("Username already exists");
                showAlert("Signup Failed", "Username already exists.");
                return;
            }
            if (userService.emailExists(email)) {
                errorLabel.setText("Email already registered");
                showAlert("Signup Failed", "Email already registered.");
                return;
            }
            userService.registerUser(username, password, email);
            System.out.println("Signup successful for user: " + username);
            errorLabel.setText("Registration successful! Please log in.");
            showAlert("Signup Success", "Registration successful! Please log in.");
        } catch (Exception e) {
            System.out.println("Signup error: " + e.getMessage());
            errorLabel.setText("Signup error: " + e.getMessage());
            showAlert("Signup Error", e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
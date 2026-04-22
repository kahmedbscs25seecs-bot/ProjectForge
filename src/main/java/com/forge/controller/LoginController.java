package com.forge.controller;

import com.forge.model.User;
import com.forge.service.UserService;
import com.forge.util.DragUtil;
import com.forge.util.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final UserService userService = new UserService();
    private static User currentUser;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        try {
            Optional<User> userOpt = userService.login(username, password);
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                loadMainScreen();
            } else {
                showError("Invalid username or password");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }
        
        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            return;
        }
        
        try {
            int userId = userService.register(username, username + "@forge.com", password);
            if (userId > 0) {
                Optional<User> userOpt = userService.getUserById(userId);
                if (userOpt.isPresent()) {
                    currentUser = userOpt.get();
                    loadMainScreen();
                }
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                showError("Username already exists");
            } else {
                showError("Registration failed: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void loadMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
            stage.setResizable(true);
            stage.setWidth(1100);
            stage.setHeight(750);
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            
            DragUtil.makeDraggable(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }
}
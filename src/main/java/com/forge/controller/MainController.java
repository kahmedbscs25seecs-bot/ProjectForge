package com.forge.controller;

import com.forge.model.Mode;
import com.forge.model.User;
import com.forge.service.ModeService;
import com.forge.service.UserService;
import com.forge.util.DragUtil;
import com.forge.util.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label levelLabel;
    @FXML
    private Label xpLabel;
    @FXML
    private ProgressBar xpBar;
    @FXML
    private Label coinsLabel;
    @FXML
    private Label hpLabel;
    @FXML
    private ProgressBar hpBar;
    @FXML
    private Label rankLabel;
    @FXML
    private VBox modeContainer;
    @FXML
    private HBox activeModesContainer;

    private final UserService userService = new UserService();
    private final ModeService modeService = new ModeService();
    private User currentUser;
    private Mode selectedMode;
    private List<Mode> selectedModes = new ArrayList<>();

    public void initData(User user) {
        this.currentUser = user;
        updateStats();
        loadActiveModesFromDb();
        loadModes();
        
        if (modeContainer.getScene() != null) {
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            Parent root = modeContainer.getScene().getRoot();
            DragUtil.makeDraggable(stage, root);
        }
    }

    private void loadActiveModesFromDb() {
        try {
            selectedModes = modeService.getActiveModes(currentUser.getId());
        } catch (SQLException e) {
            selectedModes = new ArrayList<>();
            e.printStackTrace();
        }
    }

    private void updateStats() {
        usernameLabel.setText(currentUser.getUsername());
        levelLabel.setText("Level " + currentUser.getLevel());
        hpLabel.setText(currentUser.getCurrentHp() + " / " + currentUser.getMaxHp());
        hpBar.setProgress((double) currentUser.getCurrentHp() / currentUser.getMaxHp());
        xpLabel.setText(currentUser.getXp() + " / " + currentUser.getXpForNextLevel());
        xpBar.setProgress((double) currentUser.getXp() / currentUser.getXpForNextLevel());
        coinsLabel.setText(String.valueOf(currentUser.getCoins()));
        rankLabel.setText(String.valueOf(currentUser.getRankPoints()));
    }

    private void loadModes() {
        modeContainer.getChildren().clear();
        activeModesContainer.getChildren().clear();
        
        try {
            List<Mode> availableModes = modeService.getAvailableModes(currentUser.getLevel());
            for (Mode mode : availableModes) {
                VBox card = createModeCard(mode);
                modeContainer.getChildren().add(card);
            }
            updateActiveModesDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createModeCard(Mode mode) {
        VBox card = new VBox(10);
        
        // Apply mode-specific background based on mode name
        String modeName = mode.getName().toLowerCase();
        if (modeName.contains("study") || modeName.contains("learn")) {
            card.getStyleClass().add("mode-card-study");
        } else if (modeName.contains("body") || modeName.contains("gym") || modeName.contains("fitness")) {
            card.getStyleClass().add("mode-card-gym");
        } else if (modeName.contains("social") || modeName.contains("introvert") || modeName.contains("talk")) {
            card.getStyleClass().add("mode-card-social");
        } else if (modeName.contains("creative") || modeName.contains("art")) {
            card.getStyleClass().add("mode-card-creative");
        } else if (modeName.contains("mindful") || modeName.contains("meditation") || modeName.contains("calm")) {
            card.getStyleClass().add("mode-card-mindful");
        } else if (mode.isCustom()) {
            card.getStyleClass().add("mode-card-custom");
        } else {
            card.getStyleClass().add("mode-card");
        }
        
        card.setPadding(new Insets(20));
        card.setMinWidth(250);
        card.setMaxWidth(300);
        
        Label nameLabel = new Label(mode.getName());
        
        // Apply mode-specific title color
        if (modeName.contains("study") || modeName.contains("learn")) {
            nameLabel.getStyleClass().add("mode-title-study");
        } else if (modeName.contains("body") || modeName.contains("gym") || modeName.contains("fitness")) {
            nameLabel.getStyleClass().add("mode-title-gym");
        } else if (modeName.contains("social") || modeName.contains("introvert") || modeName.contains("talk")) {
            nameLabel.getStyleClass().add("mode-title-social");
        } else if (modeName.contains("creative") || modeName.contains("art")) {
            nameLabel.getStyleClass().add("mode-title-creative");
        } else if (modeName.contains("mindful") || modeName.contains("meditation") || modeName.contains("calm")) {
            nameLabel.getStyleClass().add("mode-title-mindful");
        } else {
            nameLabel.getStyleClass().add("subtitle-label");
        }
        
        Label descLabel = new Label(mode.getDescription());
        descLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px;");
        descLabel.setWrapText(true);
        
        Label levelLabel = new Label("Unlocks at Level " + mode.getUnlockLevel());
        levelLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        
        boolean isSelected = selectedModes.contains(mode);
        
        if (mode.isCustom()) {
            Button deleteBtn = new Button("DELETE");
            deleteBtn.getStyleClass().add("magenta-text");
            deleteBtn.setOnAction(e -> deleteMode(mode));
            
            if (isSelected) {
                Label activeLabel = new Label("ACTIVE");
                activeLabel.getStyleClass().add("neon-button");
                activeLabel.setMouseTransparent(true);
                card.getChildren().addAll(nameLabel, descLabel, levelLabel, activeLabel, deleteBtn);
            } else {
                ToggleButton selectBtn = new ToggleButton("SELECT");
                selectBtn.getStyleClass().add("secondary-button");
                selectBtn.setOnAction(e -> toggleMode(mode, selectBtn));
                card.getChildren().addAll(nameLabel, descLabel, levelLabel, selectBtn, deleteBtn);
            }
        } else {
            if (isSelected) {
                Label activeLabel = new Label("ACTIVE");
                activeLabel.getStyleClass().add("neon-button");
                activeLabel.setMouseTransparent(true);
                card.getChildren().addAll(nameLabel, descLabel, levelLabel, activeLabel);
            } else {
                ToggleButton selectBtn = new ToggleButton("SELECT");
                selectBtn.getStyleClass().add("secondary-button");
                selectBtn.setOnAction(e -> toggleMode(mode, selectBtn));
                card.getChildren().addAll(nameLabel, descLabel, levelLabel, selectBtn);
            }
        }
        
        return card;
    }

    private void toggleMode(Mode mode, ToggleButton button) {
        if (selectedModes.contains(mode)) {
            selectedModes.remove(mode);
            try {
                modeService.removeActiveMode(currentUser.getId(), mode.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            selectedModes.add(mode);
            try {
                modeService.addActiveMode(currentUser.getId(), mode.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        loadModes();
    }

    private void updateActiveModesDisplay() {
        activeModesContainer.getChildren().clear();
        for (Mode mode : selectedModes) {
            Button modeBtn = new Button(mode.getName());
            modeBtn.getStyleClass().add("secondary-button");
            modeBtn.setOnAction(e -> {
                selectedMode = mode;
                viewQuests();
            });
            modeBtn.setOnMouseClicked(e -> {
                if (e.isSecondaryButtonDown()) {
                    removeActiveMode(mode);
                }
            });
            activeModesContainer.getChildren().add(modeBtn);
        }
        
        if (selectedModes.isEmpty()) {
            Label noModes = new Label("No modes selected");
            noModes.getStyleClass().add("text-label");
            activeModesContainer.getChildren().add(noModes);
        }
    }
    
    private void removeActiveMode(Mode mode) {
        selectedModes.remove(mode);
        try {
            modeService.removeActiveMode(currentUser.getId(), mode.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadModes();
    }
    
    private void deleteMode(Mode mode) {
        try {
            modeService.deleteMode(mode.getId());
            selectedModes.remove(mode);
            loadModes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void viewQuests() {
        if (selectedMode == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/quests.fxml"));
            Parent root = loader.load();
            
            QuestsController controller = loader.getController();
            controller.initData(currentUser, selectedMode);
            
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAllActiveQuests() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/allActiveQuests.fxml"));
            Parent root = loader.load();
            
            AllActiveQuestsController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openInventory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory.fxml"));
            Parent root = loader.load();
            
            InventoryController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAchievements() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/achievements.fxml"));
            Parent root = loader.load();
            
            AchievementsController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void createCustomMode() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/createMode.fxml"));
            Parent root = loader.load();
            
            CreateModeController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logout() {
        Stage stage = (Stage) modeContainer.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openBattle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/battle.fxml"));
            Parent root = loader.load();
            
            BattleController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/leaderboard.fxml"));
            Parent root = loader.load();
            
            LeaderboardController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) modeContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshUser() {
        try {
            Optional<User> userOpt = userService.getUserById(currentUser.getId());
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                updateStats();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
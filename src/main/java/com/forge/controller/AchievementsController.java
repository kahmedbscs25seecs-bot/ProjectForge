package com.forge.controller;

import com.forge.model.Achievement;
import com.forge.model.User;
import com.forge.service.AchievementService;
import com.forge.util.DragUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class AchievementsController {
    @FXML
    private VBox unlockedContainer;
    @FXML
    private VBox allContainer;

    private final AchievementService achievementService = new AchievementService();
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        loadAchievements();
    }

    private void loadAchievements() {
        try {
            // Load unlocked achievements
            List<Achievement> unlocked = achievementService.getUnlockedAchievements(currentUser.getId());
            unlockedContainer.getChildren().clear();
            
            if (unlocked.isEmpty()) {
                Label emptyLabel = new Label("No achievements unlocked yet. Complete quests to earn achievements!");
                emptyLabel.getStyleClass().add("text-label");
                unlockedContainer.getChildren().add(emptyLabel);
            } else {
                for (Achievement achievement : unlocked) {
                    VBox card = createAchievementCard(achievement, true);
                    unlockedContainer.getChildren().add(card);
                }
            }

            // Load all achievements
            List<Achievement> all = achievementService.getAllAchievements();
            allContainer.getChildren().clear();
            
            for (Achievement achievement : all) {
                boolean isUnlocked = unlocked.stream().anyMatch(a -> a.getId() == achievement.getId());
                VBox card = createAchievementCard(achievement, isUnlocked);
                allContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createAchievementCard(Achievement achievement, boolean unlocked) {
        VBox card = new VBox(10);
        card.getStyleClass().add("achievement-card");
        if (!unlocked) {
            card.setOpacity(0.5);
        }
        card.setPadding(new Insets(15));
        card.setMinWidth(300);
        
        String statusIcon = unlocked ? "★ " : "☆ ";
        Label nameLabel = new Label(statusIcon + achievement.getName());
        nameLabel.getStyleClass().add(unlocked ? "gold-text" : "text-label");
        
        Label descLabel = new Label(achievement.getDescription());
        descLabel.getStyleClass().add("text-label");
        descLabel.setWrapText(true);
        
        card.getChildren().addAll(nameLabel, descLabel);
        return card;
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) unlockedContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            DragUtil.makeDraggable(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.forge.controller;

import com.forge.model.Mode;
import com.forge.model.QuestProgress;
import com.forge.model.QuestProgress.QuestStatus;
import com.forge.model.User;
import com.forge.service.ModeService;
import com.forge.service.QuestService;
import com.forge.service.UserService;
import com.forge.util.DragUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AllActiveQuestsController {
    
    @FXML
    private Label coinsLabel;
    @FXML
    private VBox activeQuestContainer;
    @FXML
    private VBox completedQuestContainer;
    
    private final QuestService questService = new QuestService();
    private final UserService userService = new UserService();
    private final ModeService modeService = new ModeService();
    private User currentUser;
    
    public void initData(User user) {
        this.currentUser = user;
        updateCoins();
        loadAllQuests();
    }
    
    private void updateCoins() {
        try {
            Optional<User> userOpt = userService.getUserById(currentUser.getId());
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                coinsLabel.setText(String.valueOf(currentUser.getCoins()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadAllQuests() {
        activeQuestContainer.getChildren().clear();
        completedQuestContainer.getChildren().clear();
        
        try {
            List<Mode> allModes = modeService.getAllModes();
            
            List<QuestProgress> allProgress = questService.getUserProgress(currentUser.getId());
            
            List<QuestProgress> activeQuests = new ArrayList<>();
            List<QuestProgress> completedQuests = new ArrayList<>();
            
            for (QuestProgress progress : allProgress) {
                if (progress.getStatus() == QuestStatus.IN_PROGRESS || progress.getStatus() == QuestStatus.PENDING) {
                    activeQuests.add(progress);
                } else if (progress.getStatus() == QuestStatus.COMPLETED) {
                    completedQuests.add(progress);
                }
            }
            
            // Load active quests
            if (activeQuests.isEmpty()) {
                Label emptyLabel = new Label("No active quests. Select a mode and start a quest!");
                emptyLabel.getStyleClass().add("text-label");
                activeQuestContainer.getChildren().add(emptyLabel);
            } else {
                for (QuestProgress progress : activeQuests) {
                    Mode mode = findModeById(allModes, progress.getQuestId());
                    String modeName = mode != null ? mode.getName() : "Unknown";
                    VBox card = createQuestCard(progress, modeName, true);
                    activeQuestContainer.getChildren().add(card);
                }
            }
            
            // Load completed quests
            if (completedQuests.isEmpty()) {
                Label emptyLabel = new Label("No completed quests yet");
                emptyLabel.getStyleClass().add("text-label");
                completedQuestContainer.getChildren().add(emptyLabel);
            } else {
                for (QuestProgress progress : completedQuests) {
                    Mode mode = findModeById(allModes, progress.getQuestId());
                    String modeName = mode != null ? mode.getName() : "Unknown";
                    VBox card = createQuestCard(progress, modeName, false);
                    completedQuestContainer.getChildren().add(card);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Mode findModeById(List<Mode> modes, int questId) {
        for (Mode mode : modes) {
            try {
                List<com.forge.model.Quest> quests = questService.getQuestsByMode(mode.getId());
                for (com.forge.model.Quest quest : quests) {
                    if (quest.getId() == questId) {
                        return mode;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private VBox createQuestCard(QuestProgress progress, String modeName, boolean isActive) {
        VBox card = new VBox(10);
        card.getStyleClass().add("quest-card");
        card.setPadding(new Insets(15));
        card.setMinWidth(400);
        card.setMaxWidth(500);
        
        if (!isActive) {
            card.setOpacity(0.7);
        }
        
        Label modeLabel = new Label("[" + modeName + "]");
        modeLabel.getStyleClass().add("text-label");
        modeLabel.setStyle("-fx-font-size: 12px;");
        
        Label titleLabel = new Label(progress.getQuest().getTitle());
        titleLabel.getStyleClass().add("subtitle-label");
        
        Label descLabel = new Label(progress.getQuest().getDescription());
        descLabel.getStyleClass().add("text-label");
        descLabel.setWrapText(true);
        
        HBox rewardBox = new HBox(20);
        rewardBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label xpReward = new Label("XP: " + progress.getQuest().getXpReward());
        xpReward.getStyleClass().add("cyan-text");
        
        Label coinReward = new Label("Coins: " + progress.getQuest().getCoinReward());
        coinReward.getStyleClass().add("gold-text");
        
        rewardBox.getChildren().addAll(xpReward, coinReward);
        
        if (isActive) {
            Button completeBtn = new Button("COMPLETE");
            completeBtn.getStyleClass().add("neon-button");
            completeBtn.setOnAction(e -> completeQuest(progress));
            
            Button abandonBtn = new Button("ABANDON");
            abandonBtn.getStyleClass().add("secondary-button");
            abandonBtn.setOnAction(e -> abandonQuest(progress));
            
            HBox buttonBox = new HBox(10);
            buttonBox.getChildren().addAll(completeBtn, abandonBtn);
            
            card.getChildren().addAll(modeLabel, titleLabel, descLabel, rewardBox, buttonBox);
        } else {
            Label completedLabel = new Label("COMPLETED");
            completedLabel.getStyleClass().add("gold-text");
            card.getChildren().addAll(modeLabel, titleLabel, descLabel, rewardBox, completedLabel);
        }
        
        return card;
    }
    
    private void completeQuest(QuestProgress progress) {
        try {
            questService.completeQuest(progress.getId(), currentUser.getId());
            refreshUser();
            loadAllQuests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void abandonQuest(QuestProgress progress) {
        try {
            questService.failQuest(progress.getId(), currentUser.getId());
            refreshUser();
            loadAllQuests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void refreshUser() {
        try {
            Optional<User> userOpt = userService.getUserById(currentUser.getId());
            if (userOpt.isPresent()) {
                currentUser = userOpt.get();
                coinsLabel.setText(String.valueOf(currentUser.getCoins()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) activeQuestContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            DragUtil.makeDraggable(stage, root);
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
            
            Stage stage = (Stage) activeQuestContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
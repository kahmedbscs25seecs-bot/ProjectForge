package com.forge.controller;

import com.forge.model.Mode;
import com.forge.model.Quest;
import com.forge.model.QuestProgress;
import com.forge.model.QuestProgress.QuestStatus;
import com.forge.model.User;
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
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class QuestsController {
    @FXML
    private Label coinsLabel;
    @FXML
    private Label modeNameLabel;
    @FXML
    private Label modeDescLabel;
    @FXML
    private VBox activeQuestContainer;
    @FXML
    private VBox availableQuestContainer;
    @FXML
    private VBox completedQuestContainer;

    private final QuestService questService = new QuestService();
    private final UserService userService = new UserService();
    private User currentUser;
    private Mode currentMode;

    public void initData(User user, Mode mode) {
        this.currentUser = user;
        this.currentMode = mode;
        modeNameLabel.setText(mode.getName());
        modeDescLabel.setText(mode.getDescription());
        updateCoins();
        loadQuests();
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

    private void loadQuests() {
        activeQuestContainer.getChildren().clear();
        availableQuestContainer.getChildren().clear();
        completedQuestContainer.getChildren().clear();
        
        try {
            List<Quest> quests = questService.getQuestsByMode(currentMode.getId());
            List<QuestProgress> allProgress = questService.getUserProgress(currentUser.getId());
            
            for (Quest quest : quests) {
                // Find if there's any progress for this quest
                QuestProgress progress = allProgress.stream()
                    .filter(p -> p.getQuestId() == quest.getId())
                    .findFirst()
                    .orElse(null);
                
                if (progress == null) {
                    // No progress - available quest
                    VBox card = createQuestCard(quest, null, false, false);
                    availableQuestContainer.getChildren().add(card);
                } else if (progress.getStatus() == QuestStatus.IN_PROGRESS || progress.getStatus() == QuestStatus.PENDING) {
                    // Active quest
                    VBox card = createQuestCard(quest, progress, true, false);
                    activeQuestContainer.getChildren().add(card);
                } else if (progress.getStatus() == QuestStatus.COMPLETED) {
                    // Completed quest
                    VBox card = createQuestCard(quest, progress, false, true);
                    completedQuestContainer.getChildren().add(card);
                }
            }
            
            // Show empty message if no quests in any category
            if (activeQuestContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("No active quests");
                emptyLabel.getStyleClass().add("text-label");
                activeQuestContainer.getChildren().add(emptyLabel);
            }
            
            if (availableQuestContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("All quests completed or in progress!");
                emptyLabel.getStyleClass().add("text-label");
                availableQuestContainer.getChildren().add(emptyLabel);
            }
            
            if (completedQuestContainer.getChildren().isEmpty()) {
                Label emptyLabel = new Label("No completed quests yet");
                emptyLabel.getStyleClass().add("text-label");
                completedQuestContainer.getChildren().add(emptyLabel);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createQuestCard(Quest quest, QuestProgress progress, boolean isActive, boolean isCompleted) {
        VBox card = new VBox(10);
        card.getStyleClass().add("quest-card");
        card.setPadding(new Insets(15));
        card.setMinWidth(400);
        card.setMaxWidth(500);
        
        if (isCompleted) {
            card.setOpacity(0.7);
        }
        
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(quest.getTitle());
        titleLabel.getStyleClass().add("subtitle-label");
        
        if (quest.getDifficulty() != null) {
            Label diffLabel = new Label(quest.getDifficulty().name());
            switch (quest.getDifficulty()) {
                case EASY: diffLabel.getStyleClass().add("cyan-text"); break;
                case MEDIUM: diffLabel.getStyleClass().add("gold-text"); break;
                case HARD: diffLabel.getStyleClass().add("magenta-text"); break;
                case BOSS: diffLabel.getStyleClass().add("red-text"); break;
            }
            headerBox.getChildren().addAll(titleLabel, diffLabel);
        } else {
            headerBox.getChildren().add(titleLabel);
        }
        
        Label descLabel = new Label(quest.getDescription());
        descLabel.getStyleClass().add("text-label");
        descLabel.setWrapText(true);
        
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label xpReward = new Label("XP: " + quest.getXpReward());
        xpReward.getStyleClass().add("cyan-text");
        
        Label coinReward = new Label("Coins: " + quest.getCoinReward());
        coinReward.getStyleClass().add("gold-text");
        
        Label timeLabel = new Label("Time: " + quest.getTimeLimitHours() + "h");
        timeLabel.getStyleClass().add("text-label");
        
        infoBox.getChildren().addAll(xpReward, coinReward, timeLabel);
        
        if (quest.getRequiredSlot() != null) {
            Label gearLabel = new Label("Requires: " + quest.getRequiredSlot().name());
            gearLabel.getStyleClass().add("magenta-text");
            infoBox.getChildren().add(gearLabel);
        }
        
        if (quest.isCustom()) {
            Label customLabel = new Label("Custom Quest");
            customLabel.getStyleClass().add("text-label");
            infoBox.getChildren().add(customLabel);
        }
        
        if (quest.isDaily()) {
            Label dailyLabel = new Label("DAILY");
            dailyLabel.getStyleClass().add("neon-button");
            infoBox.getChildren().add(dailyLabel);
        }
        
        if (isActive && progress != null && progress.getExpiresAt() != null) {
            long hoursLeft = java.time.Duration.between(
                java.time.LocalDateTime.now(), 
                progress.getExpiresAt()
            ).toHours();
            Label timerLabel = new Label("Expires in: " + hoursLeft + "h");
            timerLabel.getStyleClass().add(hoursLeft < 6 ? "magenta-text" : "cyan-text");
            infoBox.getChildren().add(timerLabel);
        }
        
        if (isCompleted) {
            Label completedLabel = new Label("COMPLETED");
            completedLabel.getStyleClass().add("gold-text");
            card.getChildren().addAll(headerBox, descLabel, infoBox, completedLabel);
        } else if (isActive) {
            Button completeBtn = new Button("MARK COMPLETE");
            completeBtn.getStyleClass().add("neon-button");
            completeBtn.setOnAction(e -> completeQuest(progress));
            
            if (quest.getRequiredSlot() != null) {
                String gearCheck = questService.checkGearRequirements(currentUser.getId(), quest);
                if (gearCheck != null) {
                    Label gearWarning = new Label(gearCheck);
                    gearWarning.getStyleClass().add("magenta-text");
                    card.getChildren().addAll(headerBox, descLabel, infoBox, gearWarning, completeBtn);
                    return card;
                }
            }
            
            Button abandonBtn = new Button("ABANDON");
            abandonBtn.getStyleClass().add("secondary-button");
            abandonBtn.setOnAction(e -> abandonQuest(progress));
            
            HBox buttonBox = new HBox(10);
            buttonBox.getChildren().addAll(completeBtn, abandonBtn);
            
            card.getChildren().addAll(headerBox, descLabel, infoBox, buttonBox);
        } else {
            Button startBtn = new Button("START QUEST");
            startBtn.getStyleClass().add("neon-button");
            startBtn.setOnAction(e -> startQuest(quest));
            
            card.getChildren().addAll(headerBox, descLabel, infoBox, startBtn);
        }
        
        return card;
    }

    private void startQuest(Quest quest) {
        try {
            questService.startQuest(currentUser.getId(), quest.getId());
            loadQuests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void completeQuest(QuestProgress progress) {
        try {
            questService.completeQuest(progress.getId(), currentUser.getId());
            refreshUser();
            loadQuests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void abandonQuest(QuestProgress progress) {
        try {
            Optional<Quest> questOpt = questService.getQuestById(progress.getQuestId());
            if (questOpt.isPresent()) {
                Quest quest = questOpt.get();
                if ("lose_coins".equals(quest.getPenaltyType())) {
                    userService.subtractCoins(currentUser.getId(), quest.getPenaltyValue());
                }
            }
            questService.delete(progress.getId());
            refreshUser();
            loadQuests();
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
    
    @FXML
    private void openAIGenerate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/questSuggestion.fxml"));
            Parent root = loader.load();
            
            QuestSuggestionController controller = loader.getController();
            controller.initData(currentUser, currentMode, false);
            
            Stage stage = (Stage) activeQuestContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            DragUtil.makeDraggable(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
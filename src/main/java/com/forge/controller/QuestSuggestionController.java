package com.forge.controller;

import com.forge.model.Mode;
import com.forge.model.Quest;
import com.forge.model.User;
import com.forge.service.AIQuestService;
import com.forge.service.QuestService;
import com.forge.util.DragUtil;
import com.forge.util.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestSuggestionController {
    @FXML
    private Label statusLabel;
    @FXML
    private VBox suggestionsContainer;
    
    private final AIQuestService aiQuestService = new AIQuestService();
    private final QuestService questService = new QuestService();
    
    private User currentUser;
    private Mode currentMode;
    private List<Quest> generatedQuests = new ArrayList<>();
    private boolean isCustomMode;
    private Stage currentStage;
    
    public void initData(User user, Mode mode, boolean isCustomMode) {
        this.currentUser = user;
        this.currentMode = mode;
        this.isCustomMode = isCustomMode;
        
        if (suggestionsContainer.getScene() != null) {
            this.currentStage = (Stage) suggestionsContainer.getScene().getWindow();
        }
        
        if (aiQuestService.isOllamaAvailable()) {
            statusLabel.setText("AI is generating quests for " + mode.getName() + "...");
        } else {
            statusLabel.setText("Generating quests (Rule-based mode - Ollama not available)");
        }
        
        generateQuests();
    }
    
    private void generateQuests() {
        suggestionsContainer.getChildren().clear();
        generatedQuests.clear();
        
        String modeName = currentMode != null ? currentMode.getName() : "Custom";
        String modeDesc = currentMode != null ? currentMode.getDescription() : "User-created mode";
        
        int[] stats = {0, 0, 0, 0, 0, 0};
        if (currentMode != null) {
            try {
                stats = questService.getModeProgressStats(currentUser.getId(), currentMode.getId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        generatedQuests = aiQuestService.generateQuests(
            modeName, modeDesc, currentUser.getLevel(), 4,
            stats[0], stats[1], stats[2], stats[3], stats[4], stats[5]
        );
        
        for (int i = 0; i < generatedQuests.size(); i++) {
            Quest quest = generatedQuests.get(i);
            VBox card = createQuestCard(quest, i);
            suggestionsContainer.getChildren().add(card);
        }
        
        if (generatedQuests.isEmpty()) {
            Label noQuests = new Label("No quests generated. Click Regenerate.");
            noQuests.getStyleClass().add("text-label");
            suggestionsContainer.getChildren().add(noQuests);
        }
    }
    
    private VBox createQuestCard(Quest quest, int index) {
        VBox card = new VBox(10);
        card.getStyleClass().add("quest-card");
        card.setPadding(new Insets(15));
        card.setMinWidth(400);
        card.setMaxWidth(500);
        
        Label titleLabel = new Label(quest.getTitle());
        titleLabel.getStyleClass().add("subtitle-label");
        
        Label descLabel = new Label(quest.getDescription());
        descLabel.getStyleClass().add("text-label");
        descLabel.setWrapText(true);
        
        HBox infoBox = new HBox(20);
        infoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label xpLabel = new Label("XP: " + quest.getXpReward());
        xpLabel.getStyleClass().add("cyan-text");
        
        Label coinsLabel = new Label("Coins: " + quest.getCoinReward());
        coinsLabel.getStyleClass().add("gold-text");
        
        Label diffLabel = new Label("Difficulty: " + (quest.getDifficulty() != null ? quest.getDifficulty().name() : "MEDIUM"));
        diffLabel.getStyleClass().add("text-label");
        
        infoBox.getChildren().addAll(xpLabel, coinsLabel, diffLabel);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        Button approveBtn = new Button("Approve");
        approveBtn.getStyleClass().add("neon-button");
        approveBtn.setOnAction(e -> approveQuest(index));
        
        Button rejectBtn = new Button("Reject");
        rejectBtn.getStyleClass().add("secondary-button");
        rejectBtn.setOnAction(e -> rejectQuest(index, card));
        
        buttonBox.getChildren().addAll(approveBtn, rejectBtn);
        
        card.getChildren().addAll(titleLabel, descLabel, infoBox, buttonBox);
        return card;
    }
    
    private void approveQuest(int index) {
        if (index >= 0 && index < generatedQuests.size()) {
            Quest quest = generatedQuests.get(index);
            
            if (currentMode != null) {
                quest.setModeId(currentMode.getId());
            }
            quest.setCustom(isCustomMode);
            
            try {
                questService.createQuest(quest);
                generatedQuests.remove(index);
                loadSuggestions();
                
                if (generatedQuests.isEmpty()) {
                    statusLabel.setText("All quests approved! Going back...");
                    if (currentStage != null) {
                        new java.util.Timer().schedule(new java.util.TimerTask() {
                            @Override
                            public void run() {
                                goBack();
                            }
                        }, 1500);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void rejectQuest(int index, VBox card) {
        generatedQuests.remove(index);
        suggestionsContainer.getChildren().remove(card);
        
        if (generatedQuests.isEmpty()) {
            statusLabel.setText("All quests rejected. Generate more?");
        }
    }
    
    private void loadSuggestions() {
        suggestionsContainer.getChildren().clear();
        for (int i = 0; i < generatedQuests.size(); i++) {
            Quest quest = generatedQuests.get(i);
            VBox card = createQuestCard(quest, i);
            suggestionsContainer.getChildren().add(card);
        }
    }
    
    @FXML
    private void regenerate() {
        statusLabel.setText("Generating new quests...");
        generateQuests();
    }
    
    @FXML
    private void approveAll() {
        List<Quest> toSave = new ArrayList<>(generatedQuests);
        
        for (Quest quest : toSave) {
            if (currentMode != null) {
                quest.setModeId(currentMode.getId());
            }
            quest.setCustom(isCustomMode);
            
            try {
                questService.createQuest(quest);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        generatedQuests.clear();
        suggestionsContainer.getChildren().clear();
        statusLabel.setText("All quests saved! Going back...");
        
        if (currentStage != null) {
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    goBack();
                }
            }, 1500);
        }
    }
    
    @FXML
    private void goBack() {
        Stage stage = currentStage;
        
        if (stage == null && suggestionsContainer.getScene() != null) {
            stage = (Stage) suggestionsContainer.getScene().getWindow();
        }
        
        if (stage == null) {
            return;
        }
        
        try {
            String fxmlFile = isCustomMode ? "/fxml/createMode.fxml" : "/fxml/quests.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            if (isCustomMode) {
                CreateModeController controller = loader.getController();
                controller.initData(currentUser);
            } else {
                QuestsController controller = loader.getController();
                controller.initData(currentUser, currentMode);
            }
            
            stage.setScene(SceneHelper.createStyledScene(root));
            DragUtil.makeDraggable(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
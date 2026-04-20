package com.forge.controller;

import com.forge.model.Mode;
import com.forge.model.Quest;
import com.forge.model.User;
import com.forge.service.ModeService;
import com.forge.service.QuestService;
import com.forge.util.DragUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateModeController {
    @FXML
    private TextField modeNameField;
    @FXML
    private TextArea modeDescArea;
    @FXML
    private VBox questInputsContainer;

    private final ModeService modeService = new ModeService();
    private final QuestService questService = new QuestService();
    private User currentUser;
    private final List<VBox> questInputBoxes = new ArrayList<>();

    public void initData(User user) {
        this.currentUser = user;
        addQuestInput();
    }

    @FXML
    private void addQuestInput() {
        VBox questBox = new VBox(10);
        questBox.setPadding(new Insets(15));
        questBox.getStyleClass().add("quest-card");
        
        TextField titleField = new TextField();
        titleField.setPromptText("Quest title");
        titleField.getStyleClass().add("text-input");
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Quest description");
        descArea.getStyleClass().add("text-input");
        descArea.setWrapText(true);
        descArea.setPrefRowCount(2);
        
        TextField xpField = new TextField();
        xpField.setPromptText("XP reward");
        xpField.getStyleClass().add("text-input");
        
        TextField coinField = new TextField();
        coinField.setPromptText("Coin reward");
        coinField.getStyleClass().add("text-input");
        
        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("secondary-button");
        removeBtn.setOnAction(e -> {
            questInputsContainer.getChildren().remove(questBox);
            questInputBoxes.remove(questBox);
        });
        
        questBox.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descArea,
            new Label("XP:"), xpField,
            new Label("Coins:"), coinField,
            removeBtn
        );
        
        questInputBoxes.add(questBox);
        questInputsContainer.getChildren().add(questBox);
    }

    @FXML
    private void createMode() {
        String name = modeNameField.getText().trim();
        String description = modeDescArea.getText().trim();
        
        if (name.isEmpty() || description.isEmpty()) {
            System.out.println("Please fill in mode details");
            return;
        }
        
        if (questInputBoxes.isEmpty()) {
            System.out.println("Please add at least one quest");
            return;
        }
        
        try {
            Mode mode = new Mode(name, description, "/images/modes/custom.png", 1);
            mode.setCustom(true);
            int modeId = modeService.createCustomMode(mode);
            
            for (VBox questBox : questInputBoxes) {
                List<javafx.scene.Node> children = questBox.getChildren();
                String title = ((TextField) children.get(1)).getText().trim();
                String desc = ((TextArea) children.get(3)).getText().trim();
                int xp = Integer.parseInt(((TextField) children.get(5)).getText().trim());
                int coins = Integer.parseInt(((TextField) children.get(7)).getText().trim());
                
                if (!title.isEmpty()) {
                    Quest quest = new Quest(modeId, title, desc, xp, coins, "lose_coins", 5);
                    questService.getQuestsByMode(modeId);
                }
            }
            
            goBack();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numbers for XP and coins");
        }
    }
    
    @FXML
    private void openAIAssist() {
        String modeName = modeNameField.getText().trim();
        String modeDesc = modeDescArea.getText().trim();
        
        if (modeName.isEmpty()) {
            modeName = "Custom Mode";
        }
        if (modeDesc.isEmpty()) {
            modeDesc = "A custom mode created by the user";
        }
        
        Mode tempMode = new Mode(modeName, modeDesc, "/images/modes/custom.png", 1);
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/questSuggestion.fxml"));
            Parent root = loader.load();
            
            QuestSuggestionController controller = loader.getController();
            controller.initData(currentUser, tempMode, true);
            
            Stage stage = (Stage) modeNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            DragUtil.makeDraggable(stage, root);
        } catch (Exception e) {
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
            
            Stage stage = (Stage) modeNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            DragUtil.makeDraggable(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
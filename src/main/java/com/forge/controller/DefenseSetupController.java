package com.forge.controller;

import com.forge.model.User;
import com.forge.repository.UserRepository;
import com.forge.util.DragUtil;
import com.forge.util.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class DefenseSetupController {
    @FXML
    private Label currentDefenseLabel;
    @FXML
    private Button shieldBtn;
    @FXML
    private Button wandBtn;

    private final UserRepository userRepo = new UserRepository();
    private User currentUser;
    private BattleController battleController;

    public void initData(User user, BattleController battleController) {
        this.currentUser = user;
        this.battleController = battleController;
        updateDisplay();
        
        if (currentDefenseLabel.getScene() != null) {
            Stage stage = (Stage) currentDefenseLabel.getScene().getWindow();
            Parent root = currentDefenseLabel.getScene().getRoot();
            DragUtil.makeDraggable(stage, root);
        }
    }

    private void updateDisplay() {
        String defense = currentUser.getDefenseType();
        if (defense == null) defense = "SHIELD";
        currentDefenseLabel.setText("Current: " + defense);
        
        if (defense.equals("SHIELD")) {
            shieldBtn.setText("Selected ✓");
            wandBtn.setText("Select Wand");
        } else {
            wandBtn.setText("Selected ✓");
            shieldBtn.setText("Select Shield");
        }
    }

    @FXML
    private void selectShield() {
        updateDefense("SHIELD");
    }

    @FXML
    private void selectWand() {
        updateDefense("WAND");
    }

    private void updateDefense(String defenseType) {
        try {
            userRepo.updateDefenseType(currentUser.getId(), defenseType);
            currentUser.setDefenseType(defenseType);
            updateDisplay();
            
            if (battleController != null) {
                battleController.refreshUser();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/battle.fxml"));
            Parent root = loader.load();
            
            BattleController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) currentDefenseLabel.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
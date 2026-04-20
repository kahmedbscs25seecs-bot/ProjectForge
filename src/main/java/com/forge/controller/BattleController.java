package com.forge.controller;

import com.forge.model.Battle;
import com.forge.model.BattleAction;
import com.forge.model.User;
import com.forge.service.BattleService;
import com.forge.service.UserService;
import com.forge.util.DragUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BattleController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label statsLabel;
    @FXML
    private Label hpLabel;
    @FXML
    private ProgressBar hpBar;
    @FXML
    private Label playerHpLabel;
    @FXML
    private ProgressBar playerHpBar;
    @FXML
    private Label opponentHpLabel;
    @FXML
    private ProgressBar opponentHpBar;
    @FXML
    private Label opponentLevelLabel;
    @FXML
    private Label opponentNameLabel;
    @FXML
    private VBox battleLogContainer;
    @FXML
    private Button findOpponentBtn;
    @FXML
    private HBox attackButtonsBox;
    @FXML
    private Button nextTurnBtn;
    @FXML
    private Button newBattleBtn;

    private final BattleService battleService = new BattleService();
    private final UserService userService = new UserService();
    private User currentUser;
    private User opponent;
    private Battle currentBattle;
    private List<BattleAction> battleActions = new ArrayList<>();
    private boolean isPlayerTurn = true;
    private ScheduledExecutorService scheduler;

    public void initData(User user) {
        this.currentUser = user;
        updateStats();
        
        if (battleLogContainer.getScene() != null) {
            Stage stage = (Stage) battleLogContainer.getScene().getWindow();
            Parent root = battleLogContainer.getScene().getRoot();
            DragUtil.makeDraggable(stage, root);
        }
    }

    private void updateStats() {
        usernameLabel.setText(currentUser.getUsername());
        statsLabel.setText("ATK: " + currentUser.getAttack() + " | DEF: " + currentUser.getDefense() + " | LCK: " + currentUser.getLuck());
        hpLabel.setText(currentUser.getCurrentHp() + " / " + currentUser.getMaxHp());
        hpBar.setProgress((double) currentUser.getCurrentHp() / currentUser.getMaxHp());
        playerHpLabel.setText("HP: " + currentUser.getCurrentHp() + "/" + currentUser.getMaxHp());
        playerHpBar.setProgress((double) currentUser.getCurrentHp() / currentUser.getMaxHp());
    }

    @FXML
    private void findOpponent() {
        try {
            Optional<User> opponentOpt = battleService.findOpponent(currentUser.getId());
            if (opponentOpt.isEmpty()) {
                addBattleLog("No opponents available. Try again later.");
                return;
            }
            
            opponent = opponentOpt.get();
            opponentNameLabel.setText(opponent.getUsername() + " (Lv." + opponent.getLevel() + ")");
            opponentLevelLabel.setText("Lv." + opponent.getLevel());
            opponentHpLabel.setText("HP: " + opponent.getCurrentHp() + "/" + opponent.getMaxHp());
            opponentHpBar.setProgress((double) opponent.getCurrentHp() / opponent.getMaxHp());
            
            currentBattle = battleService.startBattle(currentUser.getId(), opponent.getId());
            battleActions.clear();
            battleLogContainer.getChildren().clear();
            
            addBattleLog("Battle started vs " + opponent.getUsername() + "!");
            addBattleLog("Your HP: " + currentUser.getCurrentHp() + " | Enemy HP: " + opponent.getCurrentHp());
            
            findOpponentBtn.setVisible(false);
            attackButtonsBox.setVisible(true);
            
            isPlayerTurn = true;
        } catch (SQLException e) {
            addBattleLog("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void swordAttack() {
        executeAttack(BattleService.ATTACK_SWORD);
    }

    @FXML
    private void wandAttack() {
        executeAttack(BattleService.ATTACK_WAND);
    }

    private void executeAttack(String attackType) {
        try {
            BattleAction action = battleService.executeTurn(currentBattle, attackType, currentUser.getId());
            battleActions.add(action);
            
            int damage = action.getDamageDealt();
            int reduced = action.getDamageReduced();
            
            addBattleLog("You used " + attackType + "!");
            addBattleLog("Dealt " + damage + " damage (Reduced by " + reduced + ")");
            
            int newOpponentHp = currentBattle.getDefenderHpBefore();
            opponentHpLabel.setText("HP: " + Math.max(0, newOpponentHp) + "/" + opponent.getMaxHp());
            opponentHpBar.setProgress(Math.max(0, (double) newOpponentHp / opponent.getMaxHp()));
            
            checkBattleEnd();
            
            if (currentBattle.getStatus().equals("ACTIVE")) {
                attackButtonsBox.setVisible(false);
                nextTurnBtn.setVisible(true);
                isPlayerTurn = false;
                
                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.schedule(this::executeEnemyTurn, 2, TimeUnit.SECONDS);
            }
        } catch (SQLException e) {
            addBattleLog("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeEnemyTurn() {
        Platform.runLater(() -> {
            try {
                String[] attacks = {BattleService.ATTACK_SWORD, BattleService.ATTACK_WAND};
                String enemyAttack = attacks[(int)(Math.random() * attacks.length)];
                
                BattleAction action = battleService.executeTurn(currentBattle, enemyAttack, opponent.getId());
                battleActions.add(action);
                
                int damage = action.getDamageDealt();
                int reduced = action.getDamageReduced();
                
                addBattleLog(opponent.getUsername() + " used " + enemyAttack + "!");
                addBattleLog("Dealt " + damage + " damage (Reduced by " + reduced + ")");
                
                int newPlayerHp = currentBattle.getAttackerHpBefore();
                playerHpLabel.setText("HP: " + Math.max(0, newPlayerHp) + "/" + currentUser.getMaxHp());
                playerHpBar.setProgress(Math.max(0, (double) newPlayerHp / currentUser.getMaxHp()));
                
                try {
                    Optional<User> updatedUser = userService.getUserById(currentUser.getId());
                    if (updatedUser.isPresent()) {
                        currentUser = updatedUser.get();
                        hpLabel.setText(currentUser.getCurrentHp() + " / " + currentUser.getMaxHp());
                        hpBar.setProgress((double) currentUser.getCurrentHp() / currentUser.getMaxHp());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                checkBattleEnd();
                
                if (currentBattle.getStatus().equals("ACTIVE")) {
                    nextTurnBtn.setVisible(false);
                    attackButtonsBox.setVisible(true);
                    isPlayerTurn = true;
                    addBattleLog("Your turn!");
                }
            } catch (SQLException e) {
                addBattleLog("Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void nextTurn() {
        nextTurnBtn.setVisible(false);
        attackButtonsBox.setVisible(true);
    }

    private void checkBattleEnd() {
        try {
            if (currentBattle.getAttackerHpBefore() <= 0 || currentBattle.getDefenderHpBefore() <= 0) {
                currentBattle = battleService.completeBattle(currentBattle);
                
                Integer winnerId = currentBattle.getWinnerId();
                
                if (winnerId != null) {
                    if (winnerId == currentUser.getId()) {
                        addBattleLog("VICTORY! You won!");
                        addBattleLog("+" + currentBattle.getAttackerPointsChange() + " Rank Points");
                    } else {
                        addBattleLog("DEFEAT! You lost.");
                        addBattleLog("" + currentBattle.getAttackerPointsChange() + " Rank Points");
                    }
                } else {
                    addBattleLog("DRAW! Both fighters fell!");
                }
                
                attackButtonsBox.setVisible(false);
                nextTurnBtn.setVisible(false);
                newBattleBtn.setVisible(true);
                
                Optional<User> updatedUser = userService.getUserById(currentUser.getId());
                if (updatedUser.isPresent()) {
                    currentUser = updatedUser.get();
                    updateStats();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void newBattle() {
        try {
            Optional<User> updatedUser = userService.getUserById(currentUser.getId());
            if (updatedUser.isPresent()) {
                currentUser = updatedUser.get();
            }
            
            if (currentUser.getCurrentHp() <= 0) {
                addBattleLog("You need to heal before battling!");
                return;
            }
            
            opponent = null;
            currentBattle = null;
            battleActions.clear();
            battleLogContainer.getChildren().clear();
            
            opponentNameLabel.setText("Waiting for opponent...");
            opponentHpLabel.setText("HP: - / -");
            opponentHpBar.setProgress(0);
            opponentLevelLabel.setText("Lv.1");
            
            findOpponentBtn.setVisible(true);
            attackButtonsBox.setVisible(false);
            nextTurnBtn.setVisible(false);
            newBattleBtn.setVisible(false);
            
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addBattleLog(String message) {
        Label logLabel = new Label(message);
        logLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12;");
        battleLogContainer.getChildren().add(logLabel);
    }

    @FXML
    private void openHealShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/healShop.fxml"));
            Parent root = loader.load();
            
            HealShopController controller = loader.getController();
            controller.initData(currentUser, this);
            
            Stage stage = (Stage) battleLogContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openDefenseSetup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/defenseSetup.fxml"));
            Parent root = loader.load();
            
            DefenseSetupController controller = loader.getController();
            controller.initData(currentUser, this);
            
            Stage stage = (Stage) battleLogContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
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
            
            Stage stage = (Stage) battleLogContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
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
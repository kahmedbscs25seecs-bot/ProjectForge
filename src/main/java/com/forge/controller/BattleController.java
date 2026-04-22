package com.forge.controller;

import com.forge.model.Battle;
import com.forge.model.BattleAction;
import com.forge.model.User;
import com.forge.model.Spell;
import com.forge.model.Wand;
import com.forge.model.UserInventory;
import com.forge.service.BattleService;
import com.forge.service.HarryPotterBattleService;
import com.forge.service.UserService;
import com.forge.service.StatCalculationService;
import com.forge.util.DragUtil;
import com.forge.util.SceneHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private TextFlow analysisText;
    @FXML
    private Button findOpponentBtn;
    @FXML
    private HBox attackButtonsBox;
    @FXML
    private Button nextTurnBtn;
    @FXML
    private Button newBattleBtn;

    private final HarryPotterBattleService battleService = new HarryPotterBattleService();
    private final UserService userService = new UserService();
    private User currentUser;
    private User opponent;
    private Battle currentBattle;
    private List<BattleAction> battleActions = new ArrayList<>();
    private boolean isPlayerTurn = true;
    private ScheduledExecutorService scheduler;
    private Wand selectedWand;
    private List<Spell> selectedAttackSpells;
    private List<Spell> selectedDefenseSpells;
    private List<UserInventory> equippedItems;
    private final StatCalculationService statCalculationService = new StatCalculationService();

    public void initData(User user) {
        this.currentUser = user;
        this.selectedWand = null;
        this.selectedAttackSpells = null;
        this.selectedDefenseSpells = null;
        this.equippedItems = null;
        updateStats();
        
        if (battleLogContainer.getScene() != null) {
            Stage stage = (Stage) battleLogContainer.getScene().getWindow();
            Parent root = battleLogContainer.getScene().getRoot();
            DragUtil.makeDraggable(stage, root);
        }
    }

    public void initDataWithLoadout(User user, User opponent, Wand wand, List<Spell> attackSpells, List<Spell> defenseSpells, List<UserInventory> equipped) {
        this.currentUser = user;
        this.opponent = opponent;
        this.selectedWand = wand;
        this.selectedAttackSpells = attackSpells;
        this.selectedDefenseSpells = defenseSpells;
        this.equippedItems = equipped;
        
        try {
            List<UserInventory> equippedList = (equipped != null) ? equipped.stream().filter(UserInventory::isEquipped).collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
            StatCalculationService.PlayerStats stats = statCalculationService.calculateFinalStatsMinimal(user, wand, equippedList);
            
            currentUser.setAttack(stats.getFinalAttack());
            currentUser.setDefense(stats.getFinalDefense());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (battleLogContainer.getScene() != null) {
            Stage stage = (Stage) battleLogContainer.getScene().getWindow();
            Parent root = battleLogContainer.getScene().getRoot();
            DragUtil.makeDraggable(stage, root);
        }
        
        updateStats();
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
            opponentNameLabel.setText("⚔️ " + opponent.getUsername());
            opponentHpLabel.setText("HP: " + opponent.getCurrentHp() + "/" + opponent.getMaxHp());
            opponentHpBar.setProgress((double) opponent.getCurrentHp() / opponent.getMaxHp());
            
            if (analysisText != null) {
                analysisText.getChildren().clear();
                String analysis = battleService.getOpponentAnalysis(opponent);
                Text analysisTextNode = new Text(analysis);
                analysisTextNode.setStyle("-fx-fill: #b0b0b0; -fx-font-size: 12;");
                analysisText.getChildren().add(analysisTextNode);
            }
            
            currentBattle = battleService.startBattle(currentUser.getId(), opponent.getId());
            battleActions.clear();
            battleLogContainer.getChildren().clear();
            
            addBattleLog("⚔️ Duel started vs " + opponent.getUsername() + "!");
            addBattleLog("Your HP: " + currentUser.getCurrentHp() + " | Enemy HP: " + opponent.getCurrentHp());
            addBattleLog("Choose your spell wisely based on the analysis!");
            
            findOpponentBtn.setVisible(false);
            attackButtonsBox.setVisible(true);
            
            isPlayerTurn = true;
        } catch (SQLException e) {
            addBattleLog("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void castSpell1() {
        castSpell("Incendio", 1);
    }

    @FXML
    private void castSpell2() {
        castSpell("Aguamenti", 2);
    }

    @FXML
    private void castSpell3() {
        castSpell("Stupefy", 3);
    }

    @FXML
    private void castSpell4() {
        castSpell("Protego", 6);
    }

    @FXML
    private void castSpell5() {
        castSpell("Expelliarmus", 4);
    }

    private void castSpell(String spellName, int spellId) {
        try {
            Spell spell = battleService.getAllSpells().stream()
                .filter(s -> s.getId() == spellId)
                .findFirst()
                .orElse(null);
            
            if (spell == null || opponent == null) {
                addBattleLog("No opponent found!");
                return;
            }
            
            BattleAction action = battleService.castSpell(currentBattle, spell, currentUser.getId(), opponent.getId());
            battleActions.add(action);
            
            if (action.isHit()) {
                int damage = action.getDamageDealt();
                int reduced = action.getDamageReduced();
                
                String logMessage = "You cast " + spell.getName() + "! ";
                if (damage > 0) {
                    logMessage += "Dealt " + damage + " damage!";
                    if (reduced > 0) {
                        logMessage += " (Blocked: " + reduced + ")";
                    }
                    if (action.getStatusEffect() != null) {
                        logMessage += " " + action.getStatusEffect();
                    }
                } else {
                    logMessage += "Shielded!";
                }
                addBattleLog(logMessage);
                
                int newOpponentHp = currentBattle.getDefenderHpBefore();
                opponentHpLabel.setText("HP: " + Math.max(0, newOpponentHp) + "/" + opponent.getMaxHp());
                opponentHpBar.setProgress(Math.max(0, (double) newOpponentHp / opponent.getMaxHp()));
            } else {
                addBattleLog("You cast " + spell.getName() + "... but missed!");
            }
            
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
                List<Spell> attackSpells = battleService.getAttackSpells();
                Spell randomSpell = attackSpells.get((int)(Math.random() * attackSpells.size()));
                
                BattleAction action = battleService.castSpell(currentBattle, randomSpell, opponent.getId(), currentUser.getId());
                battleActions.add(action);
                
                if (action.isHit()) {
                    int damage = action.getDamageDealt();
                    int reduced = action.getDamageReduced();
                    
                    String logMessage = opponent.getUsername() + " cast " + randomSpell.getName() + "! ";
                    if (damage > 0) {
                        logMessage += "Dealt " + damage + " damage!";
                        if (reduced > 0) {
                            logMessage += " (Blocked: " + reduced + ")";
                        }
                        if (action.getStatusEffect() != null) {
                            logMessage += " " + action.getStatusEffect();
                        }
                    } else {
                        logMessage += "You blocked with shield!";
                    }
                    addBattleLog(logMessage);
                    
                    int newPlayerHp = currentBattle.getAttackerHpBefore();
                    playerHpLabel.setText("HP: " + Math.max(0, newPlayerHp) + "/" + currentUser.getMaxHp());
                    playerHpBar.setProgress(Math.max(0, (double) newPlayerHp / currentUser.getMaxHp()));
                } else {
                    addBattleLog(opponent.getUsername() + " cast " + randomSpell.getName() + "... but missed!");
                }
                
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
                    addBattleLog("Your turn! Choose your spell.");
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
        logLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 14px; -fx-font-weight: bold;");
        logLabel.setOpacity(0);
        
        if (battleLogContainer.getChildren().size() > 3) {
            battleLogContainer.getChildren().remove(0);
        }
        
        battleLogContainer.getChildren().add(logLabel);
        
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(300),
            new javafx.animation.KeyValue(logLabel.opacityProperty(), 1.0)
        ));
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(2000),
            new javafx.animation.KeyValue(logLabel.opacityProperty(), 1.0)
        ));
        timeline.getKeyFrames().add(new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(2500),
            new javafx.animation.KeyValue(logLabel.opacityProperty(), 0.0)
        ));
        
        timeline.setOnFinished(e -> {
            if (battleLogContainer.getChildren().contains(logLabel)) {
                battleLogContainer.getChildren().remove(logLabel);
            }
        });
        
        timeline.play();
    }

    @FXML
    private void openHealShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/healShop.fxml"));
            Parent root = loader.load();
            
            HealShopController controller = loader.getController();
            controller.initData(currentUser, this);
            
            Stage stage = (Stage) battleLogContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
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
            stage.setScene(SceneHelper.createStyledScene(root));
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
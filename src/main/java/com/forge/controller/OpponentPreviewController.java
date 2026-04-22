package com.forge.controller;

import com.forge.model.*;
import com.forge.repository.*;
import com.forge.service.*;
import com.forge.util.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.*;

public class OpponentPreviewController {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label statsLabel;
    @FXML
    private Label hpLabel;
    @FXML
    private ProgressBar hpBar;
    @FXML
    private Label levelLabel;
    @FXML
    private Label wandLabel;
    @FXML
    private Label equipmentLabel;
    @FXML
    private Label attackSpellsLabel;
    @FXML
    private Label defenseSpellsLabel;
    @FXML
    private Label opponentNameLabel;
    @FXML
    private Label opponentRankLabel;
    @FXML
    private Label opponentLevelLabel;
    @FXML
    private Label opponentHpLabel;
    @FXML
    private ProgressBar opponentHpBar;
    @FXML
    private Label playerAtkLabel;
    @FXML
    private Label playerDefLabel;
    @FXML
    private Label playerLckLabel;
    @FXML
    private Label opponentAtkLabel;
    @FXML
    private Label opponentDefLabel;
    @FXML
    private Label opponentLckLabel;
    @FXML
    private Label opponentSpellsLabel;
    @FXML
    private Button startBattleBtn;

    private final UserRepository userRepository = new UserRepository();
    private final BattleService battleService = new BattleService();
    private final StatCalculationService statCalculationService = new StatCalculationService();
    private final SpellRepository spellRepository = new SpellRepository();

    private User currentUser;
    private Wand selectedWand;
    private List<Spell> selectedAttackSpells;
    private List<Spell> selectedDefenseSpells;
    private List<UserInventory> equippedItems;
    private User opponent;

    public void initData(User user, Wand wand, List<Spell> attackSpells, List<Spell> defenseSpells, List<UserInventory> equipped) {
        this.currentUser = user;
        this.selectedWand = wand;
        this.equippedItems = equipped;
        
        // Set default spells if none selected
        if (attackSpells == null || attackSpells.isEmpty()) {
            this.selectedAttackSpells = getDefaultAttackSpells();
        } else {
            this.selectedAttackSpells = attackSpells;
        }
        
        if (defenseSpells == null || defenseSpells.isEmpty()) {
            this.selectedDefenseSpells = getDefaultDefenseSpells();
        } else {
            this.selectedDefenseSpells = defenseSpells;
        }
        
        displayPlayerInfo();
        findAndDisplayOpponent();
    }
    
    private List<Spell> getDefaultAttackSpells() {
        List<Spell> defaultSpells = new ArrayList<>();
        try {
            List<Spell> allSpells = spellRepository.getAllSpells();
            for (Spell s : allSpells) {
                if (s.getType() == Spell.SpellType.ATTACK && defaultSpells.size() < 5) {
                    defaultSpells.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultSpells;
    }
    
    private List<Spell> getDefaultDefenseSpells() {
        List<Spell> defaultSpells = new ArrayList<>();
        try {
            List<Spell> allSpells = spellRepository.getAllSpells();
            for (Spell s : allSpells) {
                if (s.getType() == Spell.SpellType.DEFENSE && defaultSpells.size() < 3) {
                    defaultSpells.add(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultSpells;
    }

    private void displayPlayerInfo() {
        if (usernameLabel != null) usernameLabel.setText(currentUser.getUsername());
        if (hpLabel != null) hpLabel.setText(currentUser.getCurrentHp() + "/" + currentUser.getMaxHp());
        if (hpBar != null) hpBar.setProgress((double) currentUser.getCurrentHp() / currentUser.getMaxHp());
        if (levelLabel != null) levelLabel.setText(String.valueOf(currentUser.getLevel()));
        
        StatCalculationService.PlayerStats stats = statCalculationService.calculateFinalStatsMinimal(currentUser, selectedWand, equippedItems);
        
        if (statsLabel != null) statsLabel.setText("ATK: " + stats.getFinalAttack() + " | DEF: " + stats.getFinalDefense() + " | LCK: " + currentUser.getLuck());
        
        if (playerAtkLabel != null) playerAtkLabel.setText("ATK: " + stats.getFinalAttack());
        if (playerDefLabel != null) playerDefLabel.setText("DEF: " + stats.getFinalDefense());
        if (playerLckLabel != null) playerLckLabel.setText("LCK: " + currentUser.getLuck());
        
        String wandName = selectedWand != null ? selectedWand.getName() : "None";
        if (wandLabel != null) wandLabel.setText("🪄 Wand: " + wandName);
        
        String attackNames = selectedAttackSpells.stream()
            .map(Spell::getName)
            .collect(Collectors.joining(", "));
        if (attackSpellsLabel != null) attackSpellsLabel.setText("⚔️ " + attackNames);
        
        String defenseNames = selectedDefenseSpells.stream()
            .map(Spell::getName)
            .collect(Collectors.joining(", "));
        if (defenseSpellsLabel != null) defenseSpellsLabel.setText("🛡️ " + defenseNames);
    }

    private void findAndDisplayOpponent() {
        try {
            Optional<User> opponentOpt = battleService.findOpponent(currentUser.getId());
            
            if (opponentOpt.isEmpty()) {
                if (opponentNameLabel != null) opponentNameLabel.setText("🧙 No opponents");
                if (startBattleBtn != null) startBattleBtn.setDisable(true);
                return;
            }
            
            opponent = opponentOpt.get();
            
            if (opponentNameLabel != null) opponentNameLabel.setText("🧙 " + opponent.getUsername());
            if (opponentRankLabel != null) opponentRankLabel.setText(opponent.getRank() != null ? opponent.getRank() : "Bronze");
            if (opponentLevelLabel != null) opponentLevelLabel.setText("Lv." + opponent.getLevel());
            if (opponentHpLabel != null) opponentHpLabel.setText(opponent.getCurrentHp() + "/" + opponent.getMaxHp());
            if (opponentHpBar != null) opponentHpBar.setProgress((double) opponent.getCurrentHp() / opponent.getMaxHp());
            
            if (opponentAtkLabel != null) opponentAtkLabel.setText("ATK: " + opponent.getAttack());
            if (opponentDefLabel != null) opponentDefLabel.setText("DEF: " + opponent.getDefense());
            if (opponentLckLabel != null) opponentLckLabel.setText("LCK: " + opponent.getLuck());
            
            if (startBattleBtn != null) startBattleBtn.setDisable(false);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void startBattle() {
        if (opponent == null) {
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/battle.fxml"));
            Parent root = loader.load();
            
            BattleController controller = loader.getController();
            controller.initDataWithLoadout(currentUser, opponent, selectedWand, selectedAttackSpells, selectedDefenseSpells, equippedItems);
            
            Stage stage = (Stage) ((javafx.scene.Node)startBattleBtn).getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/prepareForBattle.fxml"));
            Parent root = loader.load();
            
            PrepareForBattleController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) ((javafx.scene.Node)startBattleBtn).getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
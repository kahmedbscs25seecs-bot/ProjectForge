package com.forge.controller;

import com.forge.model.HealingItem;
import com.forge.model.User;
import com.forge.repository.HealingItemRepository;
import com.forge.repository.UserRepository;
import com.forge.util.DragUtil;
import com.forge.util.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HealShopController {
    @FXML
    private Label hpLabel;
    @FXML
    private Label coinsLabel;
    @FXML
    private Label dailyHealLabel;
    @FXML
    private VBox potionsContainer;
    @FXML
    private VBox userPotionsContainer;

    private final HealingItemRepository healingRepo = new HealingItemRepository();
    private final UserRepository userRepo = new UserRepository();
    private User currentUser;
    private BattleController battleController;
    private Map<Integer, Integer> userPotions = new HashMap<>();

    public void initData(User user, BattleController battleController) {
        this.currentUser = user;
        this.battleController = battleController;
        updateStats();
        loadPotions();
        
        if (potionsContainer.getScene() != null) {
            Stage stage = (Stage) potionsContainer.getScene().getWindow();
            Parent root = potionsContainer.getScene().getRoot();
            DragUtil.makeDraggable(stage, root);
        }
    }

    private void updateStats() {
        hpLabel.setText(currentUser.getCurrentHp() + " / " + currentUser.getMaxHp());
        coinsLabel.setText(String.valueOf(currentUser.getCoins()));
        
        if (canUseDailyHeal()) {
            dailyHealLabel.setText("Available!");
            dailyHealLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14;");
        } else {
            dailyHealLabel.setText("Next heal: " + getTimeUntilNextHeal());
            dailyHealLabel.setStyle("-fx-text-fill: #FF5722; -fx-font-size: 14;");
        }
    }

    private boolean canUseDailyHeal() {
        if (currentUser.getLastHealTime() == null) return true;
        LocalDateTime lastHeal = currentUser.getLastHealTime();
        LocalDateTime now = LocalDateTime.now();
        return lastHeal.plusDays(1).isBefore(now);
    }

    private String getTimeUntilNextHeal() {
        if (currentUser.getLastHealTime() == null) return "now";
        LocalDateTime nextHeal = currentUser.getLastHealTime().plusDays(1);
        long hours = java.time.Duration.between(LocalDateTime.now(), nextHeal).toHours();
        return hours + " hours";
    }

    private void loadPotions() {
        potionsContainer.getChildren().clear();
        userPotionsContainer.getChildren().clear();
        userPotions.clear();
        
        try {
            List<HealingItem> potions = healingRepo.getAll();
            List<HealingItem> userItems = healingRepo.getUserItems(currentUser.getId());
            
            for (HealingItem item : userItems) {
                int qty = healingRepo.getUserQuantity(currentUser.getId(), item.getId());
                if (qty > 0) {
                    userPotions.put(item.getId(), qty);
                }
            }
            
            for (HealingItem potion : potions) {
                HBox potionBox = new HBox(15);
                potionBox.setAlignment(Pos.CENTER_LEFT);
                potionBox.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 10; -fx-background-radius: 5;");
                
                Label nameLabel = new Label(potion.getName());
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
                
                Label descLabel = new Label("+" + potion.getHealAmount() + " HP");
                descLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
                
                Label costLabel = new Label(potion.getCost() + " coins");
                costLabel.setStyle("-fx-text-fill: gold; -fx-font-size: 12;");
                
                Button buyBtn = new Button("Buy");
                buyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                buyBtn.setOnAction(e -> buyPotion(potion));
                
                potionBox.getChildren().addAll(nameLabel, descLabel, costLabel, buyBtn);
                potionsContainer.getChildren().add(potionBox);
            }
            
            for (Map.Entry<Integer, Integer> entry : userPotions.entrySet()) {
                HealingItem potion = healingRepo.findById(entry.getKey()).orElse(null);
                if (potion != null) {
                    HBox userPotionBox = new HBox(15);
                    userPotionBox.setAlignment(Pos.CENTER_LEFT);
                    userPotionBox.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 10; -fx-background-radius: 5;");
                    
                    Label nameLabel = new Label(potion.getName() + " x" + entry.getValue());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
                    
                    Label healLabel = new Label("+" + potion.getHealAmount() + " HP");
                    healLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12;");
                    
                    Button useBtn = new Button("Use");
                    useBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                    useBtn.setOnAction(e -> usePotion(potion));
                    
                    userPotionBox.getChildren().addAll(nameLabel, healLabel, useBtn);
                    userPotionsContainer.getChildren().add(userPotionBox);
                }
            }
            
            if (userPotions.isEmpty()) {
                Label noPotions = new Label("No potions owned");
                noPotions.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                userPotionsContainer.getChildren().add(noPotions);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buyPotion(HealingItem potion) {
        if (currentUser.getCoins() < potion.getCost()) {
            return;
        }
        
        try {
            currentUser.setCoins(currentUser.getCoins() - potion.getCost());
            healingRepo.addItem(currentUser.getId(), potion.getId(), 1);
            
            userRepo.update(currentUser);
            updateStats();
            loadPotions();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void usePotion(HealingItem potion) {
        int qty = userPotions.getOrDefault(potion.getId(), 0);
        if (qty <= 0) return;
        
        int newHp = Math.min(currentUser.getCurrentHp() + potion.getHealAmount(), currentUser.getMaxHp());
        currentUser.setCurrentHp(newHp);
        
        try {
            healingRepo.removeItem(currentUser.getId(), potion.getId(), 1);
            userRepo.update(currentUser);
            updateStats();
            loadPotions();
            
            if (battleController != null) {
                battleController.refreshUser();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void useDailyHeal() {
        if (!canUseDailyHeal()) return;
        
        currentUser.setCurrentHp(currentUser.getMaxHp());
        currentUser.setLastHealTime(LocalDateTime.now());
        
        try {
            userRepo.update(currentUser);
            updateStats();
            
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
            
            Stage stage = (Stage) potionsContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
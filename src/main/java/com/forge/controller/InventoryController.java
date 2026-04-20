package com.forge.controller;

import com.forge.model.Item;
import com.forge.model.User;
import com.forge.model.UserInventory;
import com.forge.service.InventoryService;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InventoryController {
    @FXML
    private Label coinsLabel;
    @FXML
    private VBox inventoryContainer;
    @FXML
    private VBox availableContainer;
    @FXML
    private VBox characterPanel;
    @FXML
    private HBox equipmentSlotsContainer;

    private final InventoryService inventoryService = new InventoryService();
    private final UserService userService = new UserService();
    private User currentUser;
    private Map<Item.EquipmentSlot, Item> equippedItems = new HashMap<>();

    public void initData(User user) {
        this.currentUser = user;
        updateCoins();
        loadEquippedItems();
        loadCharacterDisplay();
        loadInventory();
        loadAvailableItems();
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

    private void loadEquippedItems() {
        equippedItems.clear();
        try {
            List<UserInventory> inventory = inventoryService.getUserInventory(currentUser.getId());
            for (UserInventory ui : inventory) {
                if (ui.isEquipped() && ui.getItem().getSlot() != null) {
                    equippedItems.put(ui.getItem().getSlot(), ui.getItem());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCharacterDisplay() {
        characterPanel.getChildren().clear();
        
        javafx.scene.layout.Pane characterRoot = new javafx.scene.layout.Pane();
        characterRoot.setPrefSize(250, 320);
        
        double centerX = 100;
        double headY = 15;
        double chestY = 60;
        double glovesY = 62;
        double legsY = 155;
        double bootsY = 230;
        
        // HEAD - centered at top
        Rectangle head = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.HEAD), 
            70, 50, "HEAD"
        );
        head.setX(centerX - 35);
        head.setY(headY);
        
        // CHEST - center
        Rectangle chest = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.CHEST), 
            90, 95, "CHEST"
        );
        chest.setX(centerX - 45);
        chest.setY(chestY);
        
        // LEFT GLOVE - left of chest
        Rectangle leftGlove = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.GLOVES), 
            35, 90, "GLOVES"
        );
        leftGlove.setX(centerX - 80);
        leftGlove.setY(glovesY);
        
        // RIGHT GLOVE - right of chest  
        Rectangle rightGlove = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.GLOVES), 
            35, 90, "GLOVES"
        );
        rightGlove.setX(centerX + 45);
        rightGlove.setY(glovesY);
        
        // LEGS - below chest, connected
        Rectangle legs = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.LEGS), 
            90, 75, "LEGS"
        );
        legs.setX(centerX - 45);
        legs.setY(legsY);
        
        // LEFT BOOT - connected to left leg
        Rectangle leftBoot = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.BOOTS), 
            43, 40, "BOOTS"
        );
        leftBoot.setX(centerX - 45);
        leftBoot.setY(bootsY);
        
        // RIGHT BOOT - connected to right leg
        Rectangle rightBoot = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.BOOTS), 
            43, 40, "BOOTS"
        );
        rightBoot.setX(centerX + 2);
        rightBoot.setY(bootsY);
        
        // WEAPON - far right side
        Rectangle weapon = createSlotRectangle(
            equippedItems.get(Item.EquipmentSlot.WEAPON), 
            35, 110, "WEAPON"
        );
        weapon.setX(200);
        weapon.setY(chestY + 10);
        
        characterRoot.getChildren().addAll(
            head, chest, leftGlove, rightGlove, legs, leftBoot, rightBoot, weapon
        );
        
        characterPanel.getChildren().add(characterRoot);
        
        loadEquipmentSlots();
    }

    private Rectangle createSlotRectangle(Item item, double width, double height, String slot) {
        Rectangle rect = new Rectangle(width, height);
        if (item != null && item.getSpriteColor() != null) {
            try {
                rect.setFill(Color.web(item.getSpriteColor()));
            } catch (Exception e) {
                rect.setFill(Color.GRAY);
            }
        } else {
            rect.setFill(Color.web("#3C3C3C"));
            rect.setStroke(javafx.scene.paint.Color.valueOf("#00FFFF"));
            rect.setStrokeWidth(2);
        }
        rect.setArcWidth(5);
        rect.setArcHeight(5);
        return rect;
    }

    private void loadEquipmentSlots() {
        equipmentSlotsContainer.getChildren().clear();
        for (Item.EquipmentSlot slot : Item.EquipmentSlot.values()) {
            VBox slotBox = new VBox(5);
            slotBox.setAlignment(javafx.geometry.Pos.CENTER);
            
            Label slotLabel = new Label(slot.name());
            slotLabel.getStyleClass().add("text-label");
            
            Item equipped = equippedItems.get(slot);
            Label itemLabel = new Label(equipped != null ? equipped.getName() : "Empty");
            itemLabel.getStyleClass().add("subtitle-label");
            if (equipped != null) {
                itemLabel.setTextFill(javafx.scene.paint.Color.GOLD);
            }
            
            slotBox.getChildren().addAll(slotLabel, itemLabel);
            equipmentSlotsContainer.getChildren().add(slotBox);
        }
    }

    private void loadInventory() {
        inventoryContainer.getChildren().clear();
        try {
            List<UserInventory> inventory = inventoryService.getUserInventory(currentUser.getId());
            if (inventory.isEmpty()) {
                Label emptyLabel = new Label("No items yet. Unlock items below!");
                emptyLabel.getStyleClass().add("text-label");
                inventoryContainer.getChildren().add(emptyLabel);
                return;
            }
            
            for (UserInventory ui : inventory) {
                VBox card = createInventoryCard(ui);
                inventoryContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAvailableItems() {
        availableContainer.getChildren().clear();
        try {
            List<Item> availableItems = inventoryService.getAvailableItems(currentUser.getLevel());
            List<UserInventory> owned = inventoryService.getUserInventory(currentUser.getId());
            
            for (Item item : availableItems) {
                boolean ownedItem = owned.stream().anyMatch(ui -> ui.getItemId() == item.getId());
                if (!ownedItem) {
                    VBox card = createAvailableCard(item);
                    availableContainer.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createInventoryCard(UserInventory ui) {
        VBox card = new VBox(10);
        card.getStyleClass().add("inventory-item");
        card.setPadding(new Insets(15));
        card.setMinWidth(200);
        
        Item item = ui.getItem();
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("subtitle-label");
        
        Label descLabel = new Label(item.getDescription());
        descLabel.getStyleClass().add("text-label");
        descLabel.setWrapText(true);
        
        String typeStr = item.getType() == Item.ItemType.COSMETIC ? "Cosmetic" : "Functional";
        Label typeLabel = new Label("Type: " + typeStr);
        typeLabel.getStyleClass().add("text-label");
        
        Label slotLabel = new Label("Slot: " + (item.getSlot() != null ? item.getSlot().name() : "None"));
        slotLabel.getStyleClass().add("text-label");
        
        Button equipBtn = new Button(ui.isEquipped() ? "UNEQUIP" : "EQUIP");
        equipBtn.getStyleClass().add("neon-button");
        equipBtn.setOnAction(e -> toggleEquip(ui));
        
        card.getChildren().addAll(nameLabel, descLabel, typeLabel, slotLabel, equipBtn);
        return card;
    }

    private VBox createAvailableCard(Item item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("inventory-item");
        card.setPadding(new Insets(15));
        card.setMinWidth(200);
        
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("subtitle-label");
        
        Label descLabel = new Label(item.getDescription());
        descLabel.getStyleClass().add("text-label");
        descLabel.setWrapText(true);
        
        String typeStr = item.getType() == Item.ItemType.COSMETIC ? "Cosmetic" : "Functional";
        Label typeLabel = new Label("Type: " + typeStr);
        typeLabel.getStyleClass().add("text-label");
        
        Label slotLabel = new Label("Slot: " + (item.getSlot() != null ? item.getSlot().name() : "None"));
        slotLabel.getStyleClass().add("text-label");
        
        Label costLabel = new Label("Cost: " + item.getCost() + " coins");
        costLabel.getStyleClass().add("gold-text");
        
        Label levelLabel = new Label("Unlocks at Level " + item.getUnlockLevel());
        levelLabel.getStyleClass().add("text-label");
        
        Button buyBtn = new Button("BUY");
        buyBtn.getStyleClass().add("neon-button");
        buyBtn.setOnAction(e -> buyItem(item));
        
        card.getChildren().addAll(nameLabel, descLabel, typeLabel, slotLabel, costLabel, levelLabel, buyBtn);
        return card;
    }

    private void toggleEquip(UserInventory ui) {
        try {
            boolean newState = !ui.isEquipped();
            inventoryService.equipItem(ui.getId(), newState);
            
            if (newState && ui.getItem().getSlot() != null) {
                List<UserInventory> all = inventoryService.getUserInventory(currentUser.getId());
                for (UserInventory other : all) {
                    if (other.isEquipped() && other.getItem().getSlot() == ui.getItem().getSlot() && other.getId() != ui.getId()) {
                        inventoryService.equipItem(other.getId(), false);
                    }
                }
            }
            
            loadEquippedItems();
            loadCharacterDisplay();
            loadInventory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void buyItem(Item item) {
        try {
            boolean success = inventoryService.purchaseItem(currentUser.getId(), item.getId());
            if (success) {
                refreshUser();
                loadInventory();
                loadAvailableItems();
            }
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
            
            Stage stage = (Stage) inventoryContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            DragUtil.makeDraggable(stage, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.forge.controller;

import com.forge.model.*;
import com.forge.repository.*;
import com.forge.service.*;
import com.forge.util.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.*;

public class PrepareForBattleController {
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
    private Label atkLabel;
    @FXML
    private Label defLabel;
    @FXML
    private Label lckLabel;
    @FXML
    private Label statBreakdownLabel;
    @FXML
    private HBox wandButtonsBox;
    @FXML
    private GridPane equipmentGrid;
    @FXML
    private FlowPane attackSpellsBox;
    @FXML
    private FlowPane defenseSpellsBox;
    @FXML
    private Label attackCountLabel;
    @FXML
    private Label defenseCountLabel;
    @FXML
    private Button proceedBtn;

    private final UserRepository userRepository = new UserRepository();
    private final WandRepository wandRepository = new WandRepository();
    private final SpellRepository spellRepository = new SpellRepository();
    private final ItemRepository itemRepository = new ItemRepository();
    private final UserInventoryRepository inventoryRepository = new UserInventoryRepository();
    private final StatCalculationService statCalculationService = new StatCalculationService();

    private User currentUser;
    private Wand selectedWand;
    private List<Spell> availableAttackSpells = new ArrayList<>();
    private List<Spell> availableDefenseSpells = new ArrayList<>();
    private List<Spell> selectedAttackSpells = new ArrayList<>();
    private List<Spell> selectedDefenseSpells = new ArrayList<>();
    private Map<String, Item> equippedItemsMap = new HashMap<>();

    public void initData(User user) {
        this.currentUser = user;
        loadData();
        updateStatsDisplay();
    }

    private void loadData() {
        usernameLabel.setText(currentUser.getUsername());
        hpLabel.setText(currentUser.getCurrentHp() + "/" + currentUser.getMaxHp());
        hpBar.setProgress((double) currentUser.getCurrentHp() / currentUser.getMaxHp());
        levelLabel.setText(String.valueOf(currentUser.getLevel()));
        lckLabel.setText(String.valueOf(currentUser.getLuck()));

        loadWands();
        loadSpells();
        loadEquipment();
        updateStatsDisplay();
    }

    private void loadWands() {
        try {
            Wand currentWand = wandRepository.getUserWand(currentUser.getId());
            List<Wand> allWands = wandRepository.getAllWands();
            List<Wand> ownedWands = wandRepository.getUserWands(currentUser.getId());
            Set<Integer> ownedIds = ownedWands.stream().map(Wand::getId).collect(Collectors.toSet());

            wandButtonsBox.getChildren().clear();
            
            for (Wand wand : allWands) {
                boolean owned = ownedIds.contains(wand.getId());
                boolean isCurrent = currentWand != null && currentWand.getId() == wand.getId();
                
                Button wandBtn = new Button();
                wandBtn.setPrefWidth(120);
                wandBtn.setPrefHeight(60);
                wandBtn.setWrapText(true);
                
                String wandInfo = wand.getName() + "\n+" + wand.getAttackBonus() + " ATK / +" + wand.getDefenseBonus() + " DEF";
                
                if (!owned) {
                    wandBtn.setText(wand.getName() + "\n(Locked Lv." + wand.getUnlockLevel() + ")");
                    wandBtn.setStyle("-fx-background-color: #2a2a4a; -fx-text-fill: #666; -fx-font-size: 10px;");
                    wandBtn.setDisable(true);
                } else {
                    wandBtn.setText(wandInfo);
                    wandBtn.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #b0b0b0; -fx-font-size: 11px;");
                    
                    if (isCurrent) {
                        wandBtn.setStyle("-fx-background-color: #00fff530; -fx-border-color: #00fff5; -fx-border-width: 2px; -fx-text-fill: #00fff5; -fx-font-size: 11px;");
                        selectedWand = wand;
                    }
                    
                    wandBtn.setOnAction(e -> selectWand(wand));
                }
                
                wandButtonsBox.getChildren().add(wandBtn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void selectWand(Wand wand) {
        this.selectedWand = wand;
        loadWands();
        updateStatsDisplay();
    }

    private void loadSpells() {
        try {
            availableAttackSpells = spellRepository.getSpellsByType("ATTACK");
            availableDefenseSpells = spellRepository.getSpellsByType("DEFENSE");
            
            displaySpells(attackSpellsBox, availableAttackSpells, selectedAttackSpells, "ATTACK");
            displaySpells(defenseSpellsBox, availableDefenseSpells, selectedDefenseSpells, "DEFENSE");
            
            updateSpellCounts();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displaySpells(FlowPane pane, List<Spell> spells, List<Spell> selected, String type) {
        pane.getChildren().clear();
        
        for (Spell spell : spells) {
            boolean isSelected = selected.stream().anyMatch(s -> s.getId() == spell.getId());
            
            Button spellBtn = new Button();
            spellBtn.setPrefWidth(100);
            spellBtn.setPrefHeight(50);
            spellBtn.setWrapText(true);
            
            String icon = type.equals("ATTACK") ? "⚔️" : "🛡️";
            String spellInfo = icon + " " + spell.getName() + "\n" + spell.getBaseDamage() + " DMG";
            
            if (isSelected) {
                spellBtn.setText(spellInfo);
                spellBtn.setStyle("-fx-background-color: #00fff530; -fx-border-color: #00fff5; -fx-border-width: 2px; -fx-text-fill: #00fff5; -fx-font-size: 10px;");
            } else {
                spellBtn.setText(spellInfo);
                spellBtn.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #b0b0b0; -fx-font-size: 10px;");
            }
            
            spellBtn.setOnAction(e -> toggleSpell(spell, type));
            pane.getChildren().add(spellBtn);
        }
    }

    private void toggleSpell(Spell spell, String type) {
        List<Spell> selectedList = type.equals("ATTACK") ? selectedAttackSpells : selectedDefenseSpells;
        FlowPane pane = type.equals("ATTACK") ? attackSpellsBox : defenseSpellsBox;
        
        boolean wasSelected = selectedList.stream().anyMatch(s -> s.getId() == spell.getId());
        
        if (wasSelected) {
            selectedList.removeIf(s -> s.getId() == spell.getId());
        } else {
            if (type.equals("ATTACK") && selectedList.size() < 5) {
                selectedList.add(spell);
            } else if (type.equals("DEFENSE") && selectedList.size() < 3) {
                selectedList.add(spell);
            }
        }
        
        displaySpells(pane, type.equals("ATTACK") ? availableAttackSpells : availableDefenseSpells, selectedList, type);
        updateSpellCounts();
    }

    private void updateSpellCounts() {
        attackCountLabel.setText(selectedAttackSpells.size() + "/5");
        defenseCountLabel.setText(selectedDefenseSpells.size() + "/3");
        
        boolean ready = selectedAttackSpells.size() >= 5 && selectedDefenseSpells.size() >= 3 && selectedWand != null;
        proceedBtn.setDisable(!ready);
        
        if (ready) {
            proceedBtn.setStyle("-fx-background-color: #00ff8830; -fx-border-color: #00ff88; -fx-border-width: 2px; -fx-text-fill: #00ff88; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            proceedBtn.setStyle("-fx-background-color: #2a2a4a; -fx-text-fill: #666; -fx-font-size: 14px;");
        }
    }

    private void loadEquipment() {
        try {
            List<UserInventory> inventory = inventoryRepository.getUserInventory(currentUser.getId());
            List<Item> allItems = itemRepository.getAllItems();
            
            equippedItemsMap.clear();
            for (UserInventory ui : inventory) {
                if (ui.isEquipped() && ui.getItem() != null) {
                    equippedItemsMap.put(ui.getItem().getSlot().name(), ui.getItem());
                }
            }
            
            displayEquipmentSlots(allItems, inventory);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayEquipmentSlots(List<Item> allItems, List<UserInventory> inventory) {
        equipmentGrid.getChildren().clear();
        
        String[] slotNames = {"HELMET", "ARMOR", "GLOVES", "BOOTS", "ACCESSORY"};
        String[] slotLabels = {"Helmet", "Armor", "Gloves", "Boots", "Accessory"};
        
        for (int i = 0; i < slotNames.length; i++) {
            String slot = slotNames[i];
            Item equipped = equippedItemsMap.get(slot);
            
            Label slotLabel = new Label(slotLabels[i]);
            slotLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
            
            Button itemBtn = new Button();
            itemBtn.setPrefWidth(100);
            itemBtn.setPrefHeight(40);
            
            if (equipped != null) {
                int atkBonus = equipped.getAttackBonus();
                int defBonus = equipped.getDefenseBonus();
                itemBtn.setText(equipped.getName() + "\n+" + atkBonus + " ATK / +" + defBonus + " DEF");
                itemBtn.setStyle("-fx-background-color: #00fff530; -fx-border-color: #00fff5; -fx-border-width: 1px; -fx-text-fill: #00fff5; -fx-font-size: 9px;");
            } else {
                itemBtn.setText("Empty");
                itemBtn.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #666; -fx-font-size: 10px;");
            }
            
            itemBtn.setUserData(slot);
            itemBtn.setOnAction(e -> showEquipmentPicker(slot, allItems, inventory));
            
            equipmentGrid.add(slotLabel, 0, i);
            equipmentGrid.add(itemBtn, 1, i);
        }
    }

    private void showEquipmentPicker(String slot, List<Item> allItems, List<UserInventory> userInvList) {
        List<Item> slotItems = allItems.stream()
            .filter(i -> i.getSlot() != null && i.getSlot().name().equals(slot))
            .collect(Collectors.toList());
        
        if (slotItems.isEmpty()) {
            return;
        }
        
        List<Button> buttons = new ArrayList<>();
        
        for (Item item : slotItems) {
            Button btn = new Button();
            btn.setPrefWidth(120);
            btn.setPrefHeight(35);
            btn.setText(item.getName() + " (+" + item.getAttackBonus() + " ATK / +" + item.getDefenseBonus() + " DEF)");
            btn.setStyle("-fx-background-color: #1a1a2e; -fx-text-fill: #b0b0b0; -fx-font-size: 10px;");
            btn.setOnAction(e -> {
                equipItem(item, slot, allItems, userInvList);
                closePickerDialog();
            });
            buttons.add(btn);
        }
        
        Button clearBtn = new Button("Unequip");
        clearBtn.setPrefWidth(120);
        clearBtn.setStyle("-fx-background-color: #3a1a1a; -fx-text-fill: #ff4444; -fx-font-size: 10px;");
        clearBtn.setOnAction(e -> {
            unequipItem(slot, allItems, userInvList);
            closePickerDialog();
        });
        buttons.add(clearBtn);
        
        Scene scene = equipmentGrid.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            
            VBox dialog = new VBox(10);
            dialog.setStyle("-fx-background-color: #16213e; -fx-padding: 20;");
            dialog.getChildren().add(new Label("Select " + slot + ":"));
            dialog.getChildren().addAll(buttons);
            
            Scene dialogScene = new Scene(dialog);
            Stage dialogStage = new Stage();
            dialogStage.setScene(dialogScene);
            dialogStage.setTitle("Select " + slot);
            dialogStage.show();
        }
    }

    private void equipItem(Item item, String slot, List<Item> allItems, List<UserInventory> userInvList) {
        try {
            inventoryRepository.equipItem(currentUser.getId(), item.getId(), true);
            equippedItemsMap.put(slot, item);
            List<Item> allItemsList = new ArrayList<>(allItems);
            displayEquipmentSlots(allItemsList, userInvList);
            updateStatsDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void unequipItem(String slot, List<Item> allItems, List<UserInventory> userInvList) {
        try {
            Item current = equippedItemsMap.get(slot);
            if (current != null) {
                inventoryRepository.equipItem(currentUser.getId(), current.getId(), false);
                equippedItemsMap.remove(slot);
            }
            List<Item> allItemsList = new ArrayList<>(allItems);
            displayEquipmentSlots(allItemsList, userInvList);
            updateStatsDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void closePickerDialog() {
    }

    private void updateStatsDisplay() {
        try {
            List<UserInventory> equippedList = inventoryRepository.getUserInventory(currentUser.getId())
                .stream()
                .filter(UserInventory::isEquipped)
                .collect(Collectors.toList());
            
            StatCalculationService.PlayerStats stats = statCalculationService.calculateFinalStatsMinimal(
                currentUser, selectedWand, equippedList
            );
            
            atkLabel.setText(stats.getFinalAttack() + " (+" + (stats.getLevelBonusAttack() + stats.getWandAttackBonus() + stats.getEquipmentAttackBonus()) + ")");
            defLabel.setText(stats.getFinalDefense() + " (+" + (stats.getLevelBonusDefense() + stats.getWandDefenseBonus() + stats.getEquipmentDefenseBonus()) + ")");
            
            if (statBreakdownLabel != null) {
                statBreakdownLabel.setText(String.format(
                    "Base: %d/%d | Level: +%d | Wand: +%d | Equipment: +%d",
                    stats.getBaseAttack(), stats.getBaseDefense(),
                    currentUser.getLevel() * 2,
                    stats.getWandAttackBonus(),
                    stats.getEquipmentAttackBonus()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void proceedToBattle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/opponentPreview.fxml"));
            Parent root = loader.load();
            
            OpponentPreviewController controller = loader.getController();
            
            List<UserInventory> equippedList = inventoryRepository.getUserInventory(currentUser.getId())
                .stream()
                .filter(UserInventory::isEquipped)
                .collect(Collectors.toList());
            
            controller.initData(
                currentUser,
                selectedWand,
                new ArrayList<>(selectedAttackSpells),
                new ArrayList<>(selectedDefenseSpells),
                equippedList
            );
            
            Stage stage = (Stage) proceedBtn.getScene().getWindow();
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
            
            Stage stage = (Stage) proceedBtn.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
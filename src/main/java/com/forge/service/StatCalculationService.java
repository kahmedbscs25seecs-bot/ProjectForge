package com.forge.service;

import com.forge.model.User;
import com.forge.model.UserInventory;
import com.forge.model.Item;
import com.forge.model.Wand;
import java.util.List;

public class StatCalculationService {
    
    private static final int LEVEL_SCALE_FACTOR = 2;
    
    public static class PlayerStats {
        private int baseAttack;
        private int baseDefense;
        private int finalAttack;
        private int finalDefense;
        private int levelBonusAttack;
        private int levelBonusDefense;
        private int wandAttackBonus;
        private int wandDefenseBonus;
        private int equipmentAttackBonus;
        private int equipmentDefenseBonus;
        
        public int getBaseAttack() { return baseAttack; }
        public int getBaseDefense() { return baseDefense; }
        public int getFinalAttack() { return finalAttack; }
        public int getFinalDefense() { return finalDefense; }
        public int getLevelBonusAttack() { return levelBonusAttack; }
        public int getLevelBonusDefense() { return levelBonusDefense; }
        public int getWandAttackBonus() { return wandAttackBonus; }
        public int getWandDefenseBonus() { return wandDefenseBonus; }
        public int getEquipmentAttackBonus() { return equipmentAttackBonus; }
        public int getEquipmentDefenseBonus() { return equipmentDefenseBonus; }
        
        public String getBreakdown() {
            return String.format("ATK: %d (base) + %d (level) + %d (wand) + %d (equipment) = %d\n" +
                         "DEF: %d (base) + %d (level) + %d (wand) + %d (equipment) = %d",
                baseAttack, levelBonusAttack, wandAttackBonus, equipmentAttackBonus, finalAttack,
                baseDefense, levelBonusDefense, wandDefenseBonus, equipmentDefenseBonus, finalDefense);
        }
    }
    
    public PlayerStats calculateFinalStats(User user, Wand wand, List<UserInventory> equippedItems) {
        PlayerStats stats = new PlayerStats();
        
        stats.baseAttack = user.getAttack();
        stats.baseDefense = user.getDefense();
        
        int userLevel = user.getLevel();
        stats.levelBonusAttack = userLevel * LEVEL_SCALE_FACTOR;
        stats.levelBonusDefense = userLevel * LEVEL_SCALE_FACTOR;
        
        if (wand != null) {
            stats.wandAttackBonus = wand.getAttackBonus();
            stats.wandDefenseBonus = wand.getDefenseBonus();
        } else {
            stats.wandAttackBonus = 0;
            stats.wandDefenseBonus = 0;
        }
        
        stats.equipmentAttackBonus = 0;
        stats.equipmentDefenseBonus = 0;
        
        if (equippedItems != null) {
            for (UserInventory inv : equippedItems) {
                if (inv.isEquipped() && inv.getItem() != null) {
                    Item item = inv.getItem();
                    stats.equipmentAttackBonus += item.getAttackBonus();
                    stats.equipmentDefenseBonus += item.getDefenseBonus();
                }
            }
        }
        
        stats.finalAttack = stats.baseAttack + stats.levelBonusAttack + stats.wandAttackBonus + stats.equipmentAttackBonus;
        stats.finalDefense = stats.baseDefense + stats.levelBonusDefense + stats.wandDefenseBonus + stats.equipmentDefenseBonus;
        
        return stats;
    }
    
    public PlayerStats calculateFinalStatsMinimal(User user, Wand wand, List<UserInventory> equippedItems) {
        int baseATK = user.getAttack();
        int baseDEF = user.getDefense();
        int userLevel = user.getLevel();
        
        int levelATK = userLevel * LEVEL_SCALE_FACTOR;
        int levelDEF = userLevel * LEVEL_SCALE_FACTOR;
        
        int wandATK = (wand != null) ? wand.getAttackBonus() : 0;
        int wandDEF = (wand != null) ? wand.getDefenseBonus() : 0;
        
        int equipATK = 0;
        int equipDEF = 0;
        if (equippedItems != null) {
            for (UserInventory inv : equippedItems) {
                if (inv.isEquipped() && inv.getItem() != null) {
                    equipATK += inv.getItem().getAttackBonus();
                    equipDEF += inv.getItem().getDefenseBonus();
                }
            }
        }
        
        PlayerStats result = new PlayerStats();
        result.baseAttack = baseATK;
        result.baseDefense = baseDEF;
        result.levelBonusAttack = levelATK;
        result.levelBonusDefense = levelDEF;
        result.wandAttackBonus = wandATK;
        result.wandDefenseBonus = wandDEF;
        result.equipmentAttackBonus = equipATK;
        result.equipmentDefenseBonus = equipDEF;
        result.finalAttack = baseATK + levelATK + wandATK + equipATK;
        result.finalDefense = baseDEF + levelDEF + wandDEF + equipDEF;
        
        return result;
    }
    
    public int getFinalAttack(User user, Wand wand, List<UserInventory> equippedItems) {
        return calculateFinalStatsMinimal(user, wand, equippedItems).finalAttack;
    }
    
    public int getFinalDefense(User user, Wand wand, List<UserInventory> equippedItems) {
        return calculateFinalStatsMinimal(user, wand, equippedItems).finalDefense;
    }
}
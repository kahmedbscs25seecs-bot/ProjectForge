package com.forge.model;

public class Item {
    private int id;
    private String name;
    private String description;
    private ItemType type;
    private EquipmentSlot slot;
    private String spriteColor;
    private int unlockLevel;
    private int cost;
    private int attackBonus;
    private int defenseBonus;
    private boolean isConsumable;

    public enum ItemType {
        COSMETIC, FUNCTIONAL
    }
    
    public enum EquipmentSlot {
        HELMET, BOOTS, GLOVES, ARMOR, ACCESSORY, CONSUMABLE, NONE,
        HEAD, CHEST, LEGS, WEAPON
    }

    public Item() {}

    public Item(String name, String description, ItemType type, int unlockLevel, int cost) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.unlockLevel = unlockLevel;
        this.cost = cost;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemType getType() { return type; }
    public void setType(ItemType type) { this.type = type; }

    public EquipmentSlot getSlot() { return slot; }
    public void setSlot(EquipmentSlot slot) { this.slot = slot; }

    public String getSpriteColor() { return spriteColor; }
    public void setSpriteColor(String spriteColor) { this.spriteColor = spriteColor; }

    public int getUnlockLevel() { return unlockLevel; }
    public void setUnlockLevel(int unlockLevel) { this.unlockLevel = unlockLevel; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public int getAttackBonus() { return attackBonus; }
    public void setAttackBonus(int attackBonus) { this.attackBonus = attackBonus; }

    public int getDefenseBonus() { return defenseBonus; }
    public void setDefenseBonus(int defenseBonus) { this.defenseBonus = defenseBonus; }

    public boolean isConsumable() { return isConsumable; }
    public void setConsumable(boolean consumable) { isConsumable = consumable; }
}

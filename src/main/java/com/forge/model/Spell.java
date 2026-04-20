package com.forge.model;

public class Spell {
    private int id;
    private String name;
    private String description;
    private SpellType type;
    private int baseDamage;
    private int manaCost;
    private int accuracy;
    private String effect;
    private Rarity rarity;

    public enum SpellType {
        ATTACK,      // Direct damage spells
        DEFENSE,    // Shield/protection spells
        UTILITY,    // Info gathering, buffs
        CURSED      // High risk, high reward
    }

    public enum Rarity {
        COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
    }

    public Spell() {}

    public Spell(int id, String name, String description, SpellType type, int baseDamage, 
               int manaCost, int accuracy, String effect, Rarity rarity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.baseDamage = baseDamage;
        this.manaCost = manaCost;
        this.accuracy = accuracy;
        this.effect = effect;
        this.rarity = rarity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SpellType getType() { return type; }
    public void setType(SpellType type) { this.type = type; }

    public int getBaseDamage() { return baseDamage; }
    public void setBaseDamage(int baseDamage) { this.baseDamage = baseDamage; }

    public int getManaCost() { return manaCost; }
    public void setManaCost(int manaCost) { this.manaCost = manaCost; }

    public int getAccuracy() { return accuracy; }
    public void setAccuracy(int accuracy) { this.accuracy = accuracy; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
}
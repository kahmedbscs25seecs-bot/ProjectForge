package com.forge.model;

public class HealingItem {
    private int id;
    private String name;
    private String description;
    private int healAmount;
    private int cost;
    private String rarity;

    public HealingItem() {}

    public HealingItem(String name, String description, int healAmount, int cost, String rarity) {
        this.name = name;
        this.description = description;
        this.healAmount = healAmount;
        this.cost = cost;
        this.rarity = rarity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getHealAmount() { return healAmount; }
    public void setHealAmount(int healAmount) { this.healAmount = healAmount; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
}
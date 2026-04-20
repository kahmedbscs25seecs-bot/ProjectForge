package com.forge.model;

public class Wand {
    private int id;
    private String name;
    private String wood;
    private String core;
    private int attackBonus;
    private int defenseBonus;
    private int luckBonus;
    private int cost;
    private int unlockLevel;

    public Wand() {}

    public Wand(int id, String name, String wood, String core, int attackBonus, 
                int defenseBonus, int luckBonus, int cost, int unlockLevel) {
        this.id = id;
        this.name = name;
        this.wood = wood;
        this.core = core;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.luckBonus = luckBonus;
        this.cost = cost;
        this.unlockLevel = unlockLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWood() { return wood; }
    public void setWood(String wood) { this.wood = wood; }

    public String getCore() { return core; }
    public void setCore(String core) { this.core = core; }

    public int getAttackBonus() { return attackBonus; }
    public void setAttackBonus(int attackBonus) { this.attackBonus = attackBonus; }

    public int getDefenseBonus() { return defenseBonus; }
    public void setDefenseBonus(int defenseBonus) { this.defenseBonus = defenseBonus; }

    public int getLuckBonus() { return luckBonus; }
    public void setLuckBonus(int luckBonus) { this.luckBonus = luckBonus; }

    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }

    public int getUnlockLevel() { return unlockLevel; }
    public void setUnlockLevel(int unlockLevel) { this.unlockLevel = unlockLevel; }
}
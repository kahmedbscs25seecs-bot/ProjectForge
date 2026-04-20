package com.forge.model;

import java.time.LocalDateTime;

public class BattleAction {
    private int id;
    private int battleId;
    private int attackerId;
    private int defenderId;
    private int roundNumber;
    private int spellId;
    private String spellName;
    private String spellType;
    private String spellEffect;
    private int damageDealt;
    private int damageReduced;
    private int turnOrder;
    private boolean hit;
    private String statusEffect;
    private LocalDateTime createdAt;

    public BattleAction() {}

    public BattleAction(int battleId, int attackerId, int defenderId, int roundNumber, 
                        int spellId, String spellName, String spellType, String spellEffect,
                        int damageDealt, int damageReduced, int turnOrder, boolean hit) {
        this.battleId = battleId;
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.roundNumber = roundNumber;
        this.spellId = spellId;
        this.spellName = spellName;
        this.spellType = spellType;
        this.spellEffect = spellEffect;
        this.damageDealt = damageDealt;
        this.damageReduced = damageReduced;
        this.turnOrder = turnOrder;
        this.hit = hit;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBattleId() { return battleId; }
    public void setBattleId(int battleId) { this.battleId = battleId; }

    public int getAttackerId() { return attackerId; }
    public void setAttackerId(int attackerId) { this.attackerId = attackerId; }

    public int getDefenderId() { return defenderId; }
    public void setDefenderId(int defenderId) { this.defenderId = defenderId; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public int getSpellId() { return spellId; }
    public void setSpellId(int spellId) { this.spellId = spellId; }

    public String getSpellName() { return spellName; }
    public void setSpellName(String spellName) { this.spellName = spellName; }

    public String getSpellType() { return spellType; }
    public void setSpellType(String spellType) { this.spellType = spellType; }

    public String getSpellEffect() { return spellEffect; }
    public void setSpellEffect(String spellEffect) { this.spellEffect = spellEffect; }

    public int getDamageDealt() { return damageDealt; }
    public void setDamageDealt(int damageDealt) { this.damageDealt = damageDealt; }

    public int getDamageReduced() { return damageReduced; }
    public void setDamageReduced(int damageReduced) { this.damageReduced = damageReduced; }

    public int getTurnOrder() { return turnOrder; }
    public void setTurnOrder(int turnOrder) { this.turnOrder = turnOrder; }

    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }

    public String getStatusEffect() { return statusEffect; }
    public void setStatusEffect(String statusEffect) { this.statusEffect = statusEffect; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAttackType() { return spellName != null ? spellName : ""; }
    public void setAttackType(String attackType) { this.spellName = attackType; }

    public String getDefenseType() { return spellType != null ? spellType : ""; }
    public void setDefenseType(String defenseType) { this.spellType = defenseType; }
}
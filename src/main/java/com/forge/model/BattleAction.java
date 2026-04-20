package com.forge.model;

import java.time.LocalDateTime;

public class BattleAction {
    private int id;
    private int battleId;
    private int attackerId;
    private int defenderId;
    private int roundNumber;
    private String attackType;
    private String defenseType;
    private int damageDealt;
    private int damageReduced;
    private int turnOrder;
    private LocalDateTime createdAt;

    public BattleAction() {}

    public BattleAction(int battleId, int attackerId, int defenderId, int roundNumber, 
                        String attackType, String defenseType, int damageDealt, int damageReduced, int turnOrder) {
        this.battleId = battleId;
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.roundNumber = roundNumber;
        this.attackType = attackType;
        this.defenseType = defenseType;
        this.damageDealt = damageDealt;
        this.damageReduced = damageReduced;
        this.turnOrder = turnOrder;
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

    public String getAttackType() { return attackType; }
    public void setAttackType(String attackType) { this.attackType = attackType; }

    public String getDefenseType() { return defenseType; }
    public void setDefenseType(String defenseType) { this.defenseType = defenseType; }

    public int getDamageDealt() { return damageDealt; }
    public void setDamageDealt(int damageDealt) { this.damageDealt = damageDealt; }

    public int getDamageReduced() { return damageReduced; }
    public void setDamageReduced(int damageReduced) { this.damageReduced = damageReduced; }

    public int getTurnOrder() { return turnOrder; }
    public void setTurnOrder(int turnOrder) { this.turnOrder = turnOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
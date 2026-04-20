package com.forge.model;

import java.time.LocalDateTime;

public class Battle {
    private int id;
    private int attackerId;
    private int defenderId;
    private int attackerHpBefore;
    private int defenderHpBefore;
    private Integer winnerId;
    private int attackerPointsChange;
    private int defenderPointsChange;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private User attacker;
    private User defender;

    public Battle() {}

    public Battle(int attackerId, int defenderId) {
        this.attackerId = attackerId;
        this.defenderId = defenderId;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAttackerId() { return attackerId; }
    public void setAttackerId(int attackerId) { this.attackerId = attackerId; }

    public int getDefenderId() { return defenderId; }
    public void setDefenderId(int defenderId) { this.defenderId = defenderId; }

    public int getAttackerHpBefore() { return attackerHpBefore; }
    public void setAttackerHpBefore(int attackerHpBefore) { this.attackerHpBefore = attackerHpBefore; }

    public int getDefenderHpBefore() { return defenderHpBefore; }
    public void setDefenderHpBefore(int defenderHpBefore) { this.defenderHpBefore = defenderHpBefore; }

    public Integer getWinnerId() { return winnerId; }
    public void setWinnerId(Integer winnerId) { this.winnerId = winnerId; }

    public int getAttackerPointsChange() { return attackerPointsChange; }
    public void setAttackerPointsChange(int attackerPointsChange) { this.attackerPointsChange = attackerPointsChange; }

    public int getDefenderPointsChange() { return defenderPointsChange; }
    public void setDefenderPointsChange(int defenderPointsChange) { this.defenderPointsChange = defenderPointsChange; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public User getAttacker() { return attacker; }
    public void setAttacker(User attacker) { this.attacker = attacker; }

    public User getDefender() { return defender; }
    public void setDefender(User defender) { this.defender = defender; }
}
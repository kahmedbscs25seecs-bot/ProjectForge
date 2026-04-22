package com.forge.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private int coins;
    private int xp;
    private int level;
    private int totalQuestsCompleted;
    private int totalCoinsEarned;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActiveDate;
    private LocalDateTime createdAt;
    private int attack;
    private int defense;
    private int luck;
    private int rankPoints;
    private int maxHp;
    private int currentHp;
    private LocalDateTime lastHealTime;
    private String defenseType;
    private String rank;

    public User() {}

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.coins = 0;
        this.xp = 0;
        this.level = 1;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getTotalQuestsCompleted() { return totalQuestsCompleted; }
    public void setTotalQuestsCompleted(int totalQuestsCompleted) { this.totalQuestsCompleted = totalQuestsCompleted; }

    public int getTotalCoinsEarned() { return totalCoinsEarned; }
    public void setTotalCoinsEarned(int totalCoinsEarned) { this.totalCoinsEarned = totalCoinsEarned; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public LocalDate getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(LocalDate lastActiveDate) { this.lastActiveDate = lastActiveDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getLuck() { return luck; }
    public void setLuck(int luck) { this.luck = luck; }

    public int getRankPoints() { return rankPoints; }
    public void setRankPoints(int rankPoints) { this.rankPoints = rankPoints; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }

    public LocalDateTime getLastHealTime() { return lastHealTime; }
    public void setLastHealTime(LocalDateTime lastHealTime) { this.lastHealTime = lastHealTime; }

    public String getDefenseType() { return defenseType; }
    public void setDefenseType(String defenseType) { this.defenseType = defenseType; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getPlayerRank() {
        if (rankPoints >= 1000) return "Platinum";
        if (rankPoints >= 500) return "Gold";
        if (rankPoints >= 200) return "Silver";
        return "Bronze";
    }

    public int getXpForNextLevel() {
        return level * 100;
    }

    public void addXp(int amount) {
        this.xp += amount;
        while (this.xp >= getXpForNextLevel()) {
            this.xp -= getXpForNextLevel();
            this.level++;
        }
    }
}

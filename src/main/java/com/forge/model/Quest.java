package com.forge.model;

public class Quest {
    private int id;
    private int modeId;
    private String title;
    private String description;
    private int xpReward;
    private int coinReward;
    private String penaltyType;
    private int penaltyValue;
    private Difficulty difficulty;
    private int timeLimitHours;
    private Item.EquipmentSlot requiredSlot;
    private int requiredItemId;
    private boolean isCustom;
    private boolean isDaily;

    public enum Difficulty {
        EASY(12), MEDIUM(24), HARD(72), BOSS(72);
        
        private final int defaultHours;
        Difficulty(int defaultHours) {
            this.defaultHours = defaultHours;
        }
        public int getDefaultHours() {
            return defaultHours;
        }
    }

    public Quest() {}

    public Quest(int modeId, String title, String description, int xpReward, int coinReward, String penaltyType, int penaltyValue) {
        this.modeId = modeId;
        this.title = title;
        this.description = description;
        this.xpReward = xpReward;
        this.coinReward = coinReward;
        this.penaltyType = penaltyType;
        this.penaltyValue = penaltyValue;
        this.difficulty = Difficulty.MEDIUM;
        this.timeLimitHours = Difficulty.MEDIUM.getDefaultHours();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getModeId() { return modeId; }
    public void setModeId(int modeId) { this.modeId = modeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public int getCoinReward() { return coinReward; }
    public void setCoinReward(int coinReward) { this.coinReward = coinReward; }

    public String getPenaltyType() { return penaltyType; }
    public void setPenaltyType(String penaltyType) { this.penaltyType = penaltyType; }

    public int getPenaltyValue() { return penaltyValue; }
    public void setPenaltyValue(int penaltyValue) { this.penaltyValue = penaltyValue; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { 
        this.difficulty = difficulty;
        if (this.timeLimitHours == 0) {
            this.timeLimitHours = difficulty.getDefaultHours();
        }
    }

    public int getTimeLimitHours() { return timeLimitHours; }
    public void setTimeLimitHours(int timeLimitHours) { this.timeLimitHours = timeLimitHours; }

    public Item.EquipmentSlot getRequiredSlot() { return requiredSlot; }
    public void setRequiredSlot(Item.EquipmentSlot requiredSlot) { this.requiredSlot = requiredSlot; }

    public int getRequiredItemId() { return requiredItemId; }
    public void setRequiredItemId(int requiredItemId) { this.requiredItemId = requiredItemId; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public boolean isDaily() { return isDaily; }
    public void setDaily(boolean daily) { isDaily = daily; }
}

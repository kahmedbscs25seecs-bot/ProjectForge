package com.forge.model;

public class Achievement {
    private int id;
    private String name;
    private String description;
    private String iconPath;
    private boolean unlocked;

    public Achievement() {}

    public Achievement(int id, String name, String description, String iconPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconPath = iconPath;
        this.unlocked = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}
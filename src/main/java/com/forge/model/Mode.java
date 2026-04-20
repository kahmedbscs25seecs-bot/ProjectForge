package com.forge.model;

public class Mode {
    private int id;
    private String name;
    private String description;
    private String iconPath;
    private boolean isCustom;
    private int unlockLevel;

    public Mode() {}

    public Mode(String name, String description, String iconPath, int unlockLevel) {
        this.name = name;
        this.description = description;
        this.iconPath = iconPath;
        this.isCustom = false;
        this.unlockLevel = unlockLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconPath() { return iconPath; }
    public void setIconPath(String iconPath) { this.iconPath = iconPath; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public int getUnlockLevel() { return unlockLevel; }
    public void setUnlockLevel(int unlockLevel) { this.unlockLevel = unlockLevel; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mode mode = (Mode) o;
        return id == mode.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

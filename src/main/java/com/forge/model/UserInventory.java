package com.forge.model;

import java.time.LocalDateTime;

public class UserInventory {
    private int id;
    private int userId;
    private int itemId;
    private LocalDateTime acquiredAt;
    private boolean isEquipped;
    private Item item;

    public UserInventory() {}

    public UserInventory(int userId, int itemId) {
        this.userId = userId;
        this.itemId = itemId;
        this.acquiredAt = LocalDateTime.now();
        this.isEquipped = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }

    public boolean isEquipped() { return isEquipped; }
    public void setEquipped(boolean equipped) { isEquipped = equipped; }

    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
}

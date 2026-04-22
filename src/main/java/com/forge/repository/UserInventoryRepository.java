package com.forge.repository;

import com.forge.model.Item;
import com.forge.model.UserInventory;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserInventoryRepository {

    public List<UserInventory> findByUserId(int userId) throws SQLException {
        List<UserInventory> inventory = new ArrayList<>();
        String sql = "SELECT ui.*, i.name, i.description, i.type, i.unlock_level, i.cost " +
            "FROM user_inventory ui JOIN items i ON ui.item_id = i.id WHERE ui.user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserInventory ui = mapResultSetToUserInventory(rs);
                ui.setItem(mapResultSetToItem(rs));
                inventory.add(ui);
            }
        }
        return inventory;
    }

    public int create(UserInventory inventory) throws SQLException {
        String sql = "INSERT INTO user_inventory (user_id, item_id, is_equipped) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, inventory.getUserId());
            stmt.setInt(2, inventory.getItemId());
            stmt.setBoolean(3, inventory.isEquipped());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public void updateEquipped(int id, boolean equipped) throws SQLException {
        String sql = "UPDATE user_inventory SET is_equipped = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, equipped);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void equipItem(int userId, int itemId, boolean equipped) throws SQLException {
        String sql = "UPDATE user_inventory SET is_equipped = ? WHERE user_id = ? AND item_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, equipped);
            stmt.setInt(2, userId);
            stmt.setInt(3, itemId);
            stmt.executeUpdate();
        }
    }

    public List<UserInventory> getUserInventory(int userId) throws SQLException {
        String sql = "SELECT ui.*, i.name, i.description, i.type, i.slot, i.unlock_level, i.cost, i.attack_bonus, i.defense_bonus, i.is_consumable " +
            "FROM user_inventory ui JOIN items i ON ui.item_id = i.id WHERE ui.user_id = ?";
        List<UserInventory> inventory = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                UserInventory ui = new UserInventory();
                ui.setId(rs.getInt("id"));
                ui.setUserId(rs.getInt("user_id"));
                ui.setItemId(rs.getInt("item_id"));
                try {
                    ui.setAcquiredAt(rs.getTimestamp("acquired_at").toLocalDateTime());
                } catch (Exception e) {}
                try {
                    ui.setEquipped(rs.getBoolean("is_equipped"));
                } catch (Exception e) {
                    ui.setEquipped(false);
                }
                
                Item item = new Item();
                item.setId(rs.getInt("item_id"));
                item.setName(rs.getString("name"));
                item.setDescription(rs.getString("description"));
                try {
                    item.setType(Item.ItemType.valueOf(rs.getString("type")));
                } catch (Exception e) {
                    item.setType(Item.ItemType.COSMETIC);
                }
                try {
                    String slotStr = rs.getString("slot");
                    if (slotStr != null && !slotStr.isEmpty()) {
                        item.setSlot(Item.EquipmentSlot.valueOf(slotStr));
                    }
                } catch (Exception e) {}
                item.setUnlockLevel(rs.getInt("unlock_level"));
                item.setCost(rs.getInt("cost"));
                try {
                    item.setAttackBonus(rs.getInt("attack_bonus"));
                } catch (Exception e) {
                    item.setAttackBonus(0);
                }
                try {
                    item.setDefenseBonus(rs.getInt("defense_bonus"));
                } catch (Exception e) {
                    item.setDefenseBonus(0);
                }
                try {
                    item.setConsumable(rs.getBoolean("is_consumable"));
                } catch (Exception e) {
                    item.setConsumable(false);
                }
                
                ui.setItem(item);
                inventory.add(ui);
            }
        }
        return inventory;
    }

    public boolean hasItem(int userId, int itemId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_inventory WHERE user_id = ? AND item_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    private UserInventory mapResultSetToUserInventory(ResultSet rs) throws SQLException {
        UserInventory ui = new UserInventory();
        ui.setId(rs.getInt("id"));
        ui.setUserId(rs.getInt("user_id"));
        ui.setItemId(rs.getInt("item_id"));
        ui.setAcquiredAt(rs.getTimestamp("acquired_at").toLocalDateTime());
        ui.setEquipped(rs.getBoolean("is_equipped"));
        return ui;
    }

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("item_id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setType(Item.ItemType.valueOf(rs.getString("type")));
        item.setUnlockLevel(rs.getInt("unlock_level"));
        item.setCost(rs.getInt("cost"));
        return item;
    }
}
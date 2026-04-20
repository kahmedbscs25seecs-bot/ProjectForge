package com.forge.repository;

import com.forge.model.HealingItem;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HealingItemRepository {

    public List<HealingItem> getAll() throws SQLException {
        String sql = "SELECT * FROM healing_items ORDER BY cost";
        List<HealingItem> items = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public Optional<HealingItem> findById(int id) throws SQLException {
        String sql = "SELECT * FROM healing_items WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToItem(rs));
            }
        }
        return Optional.empty();
    }

    private HealingItem mapResultSetToItem(ResultSet rs) throws SQLException {
        HealingItem item = new HealingItem();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setHealAmount(rs.getInt("heal_amount"));
        item.setCost(rs.getInt("cost"));
        item.setRarity(rs.getString("rarity"));
        return item;
    }

    public int getUserQuantity(int userId, int itemId) throws SQLException {
        String sql = "SELECT quantity FROM user_healing_items WHERE user_id = ? AND item_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
        }
        return 0;
    }

    public void addItem(int userId, int itemId, int quantity) throws SQLException {
        String sql = "INSERT INTO user_healing_items (user_id, item_id, quantity) VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
        }
    }

    public void removeItem(int userId, int itemId, int quantity) throws SQLException {
        String sql = "UPDATE user_healing_items SET quantity = quantity - ? WHERE user_id = ? AND item_id = ? AND quantity >= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, userId);
            stmt.setInt(3, itemId);
            stmt.setInt(4, quantity);
            stmt.executeUpdate();
        }

        sql = "DELETE FROM user_healing_items WHERE user_id = ? AND item_id = ? AND quantity <= 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        }
    }

    public List<HealingItem> getUserItems(int userId) throws SQLException {
        String sql = "SELECT hi.*, COALESCE(uhi.quantity, 0) as user_quantity FROM healing_items hi " +
            "LEFT JOIN user_healing_items uhi ON hi.id = uhi.item_id AND uhi.user_id = ?";
        List<HealingItem> items = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HealingItem item = mapResultSetToItem(rs);
                items.add(item);
            }
        }
        return items;
    }
}
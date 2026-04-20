package com.forge.repository;

import com.forge.model.Item;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemRepository {

    public List<Item> findAll() throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public List<Item> findAvailableForLevel(int userLevel) throws SQLException {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE unlock_level <= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userLevel);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(mapResultSetToItem(rs));
            }
        }
        return items;
    }

    public Optional<Item> findById(int id) throws SQLException {
        String sql = "SELECT * FROM items WHERE id = ?";
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

    private Item mapResultSetToItem(ResultSet rs) throws SQLException {
        Item item = new Item();
        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setType(Item.ItemType.valueOf(rs.getString("type")));
        
        String slotStr = rs.getString("slot");
        if (slotStr != null && !slotStr.isEmpty()) {
            item.setSlot(Item.EquipmentSlot.valueOf(slotStr));
        }
        
        item.setSpriteColor(rs.getString("sprite_color"));
        item.setUnlockLevel(rs.getInt("unlock_level"));
        item.setCost(rs.getInt("cost"));
        return item;
    }
}

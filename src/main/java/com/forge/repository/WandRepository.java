package com.forge.repository;

import com.forge.model.Wand;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WandRepository {

    public List<Wand> getAllWands() throws SQLException {
        String sql = "SELECT * FROM wands ORDER BY unlock_level, cost";
        List<Wand> wands = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                wands.add(mapResultSetToWand(rs));
            }
        }
        return wands;
    }

    public Optional<Wand> findById(int id) throws SQLException {
        String sql = "SELECT * FROM wands WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToWand(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Wand> findUserWand(int userId) throws SQLException {
        String sql = "SELECT w.* FROM wands w " +
                    "JOIN user_wand uw ON w.id = uw.wand_id " +
                    "WHERE uw.user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToWand(rs));
            }
        }
        return Optional.empty();
    }

    public void equipWand(int userId, int wandId) throws SQLException {
        String sql = "INSERT INTO user_wand (user_id, wand_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE wand_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, wandId);
            stmt.setInt(3, wandId);
            stmt.executeUpdate();
        }
    }

    private Wand mapResultSetToWand(ResultSet rs) throws SQLException {
        Wand wand = new Wand();
        wand.setId(rs.getInt("id"));
        wand.setName(rs.getString("name"));
        wand.setWood(rs.getString("wood"));
        wand.setCore(rs.getString("core"));
        wand.setAttackBonus(rs.getInt("attack_bonus"));
        wand.setDefenseBonus(rs.getInt("defense_bonus"));
        wand.setLuckBonus(rs.getInt("luck_bonus"));
        wand.setCost(rs.getInt("cost"));
        wand.setUnlockLevel(rs.getInt("unlock_level"));
        return wand;
    }
}
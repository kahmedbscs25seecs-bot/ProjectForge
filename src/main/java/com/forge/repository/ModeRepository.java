package com.forge.repository;

import com.forge.model.Mode;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModeRepository {

    public List<Mode> findAll() throws SQLException {
        List<Mode> modes = new ArrayList<>();
        String sql = "SELECT * FROM modes";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modes.add(mapResultSetToMode(rs));
            }
        }
        return modes;
    }

    public List<Mode> findAvailableForLevel(int userLevel) throws SQLException {
        List<Mode> modes = new ArrayList<>();
        String sql = "SELECT * FROM modes WHERE unlock_level <= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userLevel);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modes.add(mapResultSetToMode(rs));
            }
        }
        return modes;
    }

    public Optional<Mode> findById(int id) throws SQLException {
        String sql = "SELECT * FROM modes WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToMode(rs));
            }
        }
        return Optional.empty();
    }

    public int create(Mode mode) throws SQLException {
        String sql = "INSERT INTO modes (name, description, icon_path, is_custom, unlock_level) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, mode.getName());
            stmt.setString(2, mode.getDescription());
            stmt.setString(3, mode.getIconPath());
            stmt.setBoolean(4, mode.isCustom());
            stmt.setInt(5, mode.getUnlockLevel());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
    
    public void deleteById(int id) throws SQLException {
        String sql1 = "DELETE FROM user_active_modes WHERE mode_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql1)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        
        String sql2 = "DELETE FROM quests WHERE mode_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        
        String sql3 = "DELETE FROM modes WHERE id = ? AND is_custom = TRUE";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql3)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Mode mapResultSetToMode(ResultSet rs) throws SQLException {
        Mode mode = new Mode();
        mode.setId(rs.getInt("id"));
        mode.setName(rs.getString("name"));
        mode.setDescription(rs.getString("description"));
        mode.setIconPath(rs.getString("icon_path"));
        mode.setCustom(rs.getBoolean("is_custom"));
        mode.setUnlockLevel(rs.getInt("unlock_level"));
        return mode;
    }
}
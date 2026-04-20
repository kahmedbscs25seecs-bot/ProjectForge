package com.forge.repository;

import com.forge.model.Mode;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserActiveModesRepository {

    public List<Mode> findActiveModesByUserId(int userId) throws SQLException {
        List<Mode> modes = new ArrayList<>();
        String sql = "SELECT m.* FROM modes m " +
            "JOIN user_active_modes uam ON m.id = uam.mode_id " +
            "WHERE uam.user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Mode mode = new Mode();
                mode.setId(rs.getInt("id"));
                mode.setName(rs.getString("name"));
                mode.setDescription(rs.getString("description"));
                mode.setIconPath(rs.getString("icon_path"));
                mode.setCustom(rs.getBoolean("is_custom"));
                mode.setUnlockLevel(rs.getInt("unlock_level"));
                modes.add(mode);
            }
        }
        return modes;
    }

    public void addActiveMode(int userId, int modeId) throws SQLException {
        String sql = "INSERT IGNORE INTO user_active_modes (user_id, mode_id) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, modeId);
            stmt.executeUpdate();
        }
    }

    public void removeActiveMode(int userId, int modeId) throws SQLException {
        String sql = "DELETE FROM user_active_modes WHERE user_id = ? AND mode_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, modeId);
            stmt.executeUpdate();
        }
    }

    public void clearAllActiveModes(int userId) throws SQLException {
        String sql = "DELETE FROM user_active_modes WHERE user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
}
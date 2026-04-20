package com.forge.repository;

import com.forge.model.Achievement;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AchievementRepository {

    public List<Achievement> findAll() throws SQLException {
        List<Achievement> achievements = new ArrayList<>();
        String sql = "SELECT * FROM achievements";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                achievements.add(mapResultSetToAchievement(rs));
            }
        }
        return achievements;
    }

    public Optional<Achievement> findById(int id) throws SQLException {
        String sql = "SELECT * FROM achievements WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToAchievement(rs));
            }
        }
        return Optional.empty();
    }

    public List<Achievement> findUnlockedByUserId(int userId) throws SQLException {
        List<Achievement> achievements = new ArrayList<>();
        String sql = "SELECT a.* FROM achievements a " +
            "JOIN user_achievements ua ON a.id = ua.achievement_id " +
            "WHERE ua.user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Achievement achievement = mapResultSetToAchievement(rs);
                achievement.setUnlocked(true);
                achievements.add(achievement);
            }
        }
        return achievements;
    }

    public boolean hasAchievement(int userId, int achievementId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_achievements WHERE user_id = ? AND achievement_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, achievementId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public void unlockAchievement(int userId, int achievementId) throws SQLException {
        String sql = "INSERT INTO user_achievements (user_id, achievement_id) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, achievementId);
            stmt.executeUpdate();
        }
    }

    private Achievement mapResultSetToAchievement(ResultSet rs) throws SQLException {
        Achievement achievement = new Achievement();
        achievement.setId(rs.getInt("id"));
        achievement.setName(rs.getString("name"));
        achievement.setDescription(rs.getString("description"));
        achievement.setIconPath(rs.getString("icon_path"));
        return achievement;
    }
}
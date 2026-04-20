package com.forge.repository;

import com.forge.model.User;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public int create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, coins, xp, level, total_quests_completed, total_coins_earned, attack, defense, luck, rank_points, max_hp, current_hp, defense_type) VALUES (?, ?, ?, ?, ?, ?, 0, 0, 10, 10, 5, 100, 60, 60, 'SHIELD')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, user.getCoins());
            stmt.setInt(5, user.getXp());
            stmt.setInt(6, user.getLevel());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

public void update(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, coins = ?, xp = ?, level = ?, total_quests_completed = ?, total_coins_earned = ?, current_streak = ?, longest_streak = ?, last_active_date = ?, attack = ?, defense = ?, luck = ?, rank_points = ?, max_hp = ?, current_hp = ?, last_heal_time = ?, defense_type = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getCoins());
            stmt.setInt(4, user.getXp());
            stmt.setInt(5, user.getLevel());
            stmt.setInt(6, user.getTotalQuestsCompleted());
            stmt.setInt(7, user.getTotalCoinsEarned());
            stmt.setInt(8, user.getCurrentStreak());
            stmt.setInt(9, user.getLongestStreak());
            stmt.setDate(10, user.getLastActiveDate() != null ? Date.valueOf(user.getLastActiveDate()) : null);
            stmt.setInt(11, user.getAttack());
            stmt.setInt(12, user.getDefense());
            stmt.setInt(13, user.getLuck());
            stmt.setInt(14, user.getRankPoints());
            stmt.setInt(15, user.getMaxHp());
            stmt.setInt(16, user.getCurrentHp());
            stmt.setTimestamp(17, user.getLastHealTime() != null ? Timestamp.valueOf(user.getLastHealTime()) : null);
            stmt.setString(18, user.getDefenseType());
            stmt.setInt(19, user.getId());
            stmt.executeUpdate();
        }
    }

    public void updateHp(int userId, int currentHp, int maxHp) throws SQLException {
        String sql = "UPDATE users SET current_hp = ?, max_hp = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentHp);
            stmt.setInt(2, maxHp);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    public void updateRankPoints(int userId, int rankPoints) throws SQLException {
        String sql = "UPDATE users SET rank_points = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rankPoints);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void updateDefenseType(int userId, String defenseType) throws SQLException {
        String sql = "UPDATE users SET defense_type = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, defenseType);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void updateLastHealTime(int userId) throws SQLException {
        String sql = "UPDATE users SET last_heal_time = NOW() WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public List<User> getTopPlayers(int limit) throws SQLException {
        String sql = "SELECT * FROM users ORDER BY rank_points DESC LIMIT ?";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public List<User> findSimilarRank(int userId, int range) throws SQLException {
        String sql = "SELECT * FROM users WHERE id != ? AND rank_points BETWEEN " +
            "(SELECT rank_points FROM users WHERE id = ?) - ? AND " +
            "(SELECT rank_points FROM users WHERE id = ?) + ? " +
            "ORDER BY ABS(rank_points - (SELECT rank_points FROM users WHERE id = ?)) LIMIT 5";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, range);
            stmt.setInt(4, userId);
            stmt.setInt(5, range);
            stmt.setInt(6, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }



    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCoins(rs.getInt("coins"));
        user.setXp(rs.getInt("xp"));
        user.setLevel(rs.getInt("level"));
        user.setTotalQuestsCompleted(rs.getInt("total_quests_completed"));
        user.setTotalCoinsEarned(rs.getInt("total_coins_earned"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setCurrentStreak(rs.getInt("current_streak"));
        user.setLongestStreak(rs.getInt("longest_streak"));
        user.setLastActiveDate(rs.getDate("last_active_date") != null ? rs.getDate("last_active_date").toLocalDate() : null);
        user.setAttack(rs.getInt("attack"));
        user.setDefense(rs.getInt("defense"));
        user.setLuck(rs.getInt("luck"));
        user.setRankPoints(rs.getInt("rank_points"));
        user.setMaxHp(rs.getInt("max_hp"));
        user.setCurrentHp(rs.getInt("current_hp"));
        user.setLastHealTime(rs.getTimestamp("last_heal_time") != null ? rs.getTimestamp("last_heal_time").toLocalDateTime() : null);
        user.setDefenseType(rs.getString("defense_type"));
        return user;
    }
}

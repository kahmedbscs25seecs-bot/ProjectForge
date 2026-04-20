package com.forge.repository;

import com.forge.model.Quest;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.sql.Types;

public class QuestRepository {

    public List<Quest> findByModeId(int modeId) throws SQLException {
        List<Quest> quests = new ArrayList<>();
        String sql = "SELECT * FROM quests WHERE mode_id = ? AND is_custom = FALSE";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, modeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                quests.add(mapResultSetToQuest(rs));
            }
        }
        return quests;
    }

    public Optional<Quest> findById(int id) throws SQLException {
        String sql = "SELECT * FROM quests WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToQuest(rs));
            }
        }
        return Optional.empty();
    }

    public int create(Quest quest) throws SQLException {
        String sql = "INSERT INTO quests (mode_id, title, description, xp_reward, coin_reward, penalty_type, penalty_value, difficulty, time_limit_hours, required_slot, required_item_id, is_custom, is_daily) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (quest.getModeId() > 0) {
                stmt.setInt(1, quest.getModeId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, quest.getTitle());
            stmt.setString(3, quest.getDescription());
            stmt.setInt(4, quest.getXpReward());
            stmt.setInt(5, quest.getCoinReward());
            stmt.setString(6, quest.getPenaltyType());
            stmt.setInt(7, quest.getPenaltyValue());
            stmt.setString(8, quest.getDifficulty() != null ? quest.getDifficulty().name() : "MEDIUM");
            stmt.setInt(9, quest.getTimeLimitHours());
            stmt.setString(10, quest.getRequiredSlot() != null ? quest.getRequiredSlot().name() : null);
            stmt.setInt(11, quest.getRequiredItemId());
            stmt.setBoolean(12, quest.isCustom());
            stmt.setBoolean(13, quest.isDaily());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
    
    public List<Quest> findCustomQuests() throws SQLException {
        List<Quest> quests = new ArrayList<>();
        String sql = "SELECT * FROM quests WHERE is_custom = TRUE";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                quests.add(mapResultSetToQuest(rs));
            }
        }
        return quests;
    }
    
    public List<Quest> findDailyQuests() throws SQLException {
        List<Quest> quests = new ArrayList<>();
        String sql = "SELECT * FROM quests WHERE is_daily = TRUE";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                quests.add(mapResultSetToQuest(rs));
            }
        }
        return quests;
    }
    
    public List<Quest> findByUserId(int userId) throws SQLException {
        List<Quest> quests = new ArrayList<>();
        String sql = "(SELECT q.* FROM quests q JOIN modes m ON q.mode_id = m.id WHERE m.unlock_level <= (SELECT level FROM users WHERE id = ?)) UNION ALL (SELECT * FROM quests WHERE is_custom = TRUE)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                quests.add(mapResultSetToQuest(rs));
            }
        }
        return quests;
    }

    private Quest mapResultSetToQuest(ResultSet rs) throws SQLException {
        Quest quest = new Quest();
        quest.setId(rs.getInt("id"));
        
        int modeId = rs.getInt("mode_id");
        if (!rs.wasNull()) {
            quest.setModeId(modeId);
        }
        
        quest.setTitle(rs.getString("title"));
        quest.setDescription(rs.getString("description"));
        quest.setXpReward(rs.getInt("xp_reward"));
        quest.setCoinReward(rs.getInt("coin_reward"));
        quest.setPenaltyType(rs.getString("penalty_type"));
        quest.setPenaltyValue(rs.getInt("penalty_value"));
        
        String difficultyStr = rs.getString("difficulty");
        if (difficultyStr != null && !difficultyStr.isEmpty()) {
            quest.setDifficulty(Quest.Difficulty.valueOf(difficultyStr));
        }
        
        quest.setTimeLimitHours(rs.getInt("time_limit_hours"));
        
        String slotStr = rs.getString("required_slot");
        if (slotStr != null && !slotStr.isEmpty()) {
            quest.setRequiredSlot(com.forge.model.Item.EquipmentSlot.valueOf(slotStr));
        }
        
        int reqItemId = rs.getInt("required_item_id");
        if (!rs.wasNull()) {
            quest.setRequiredItemId(reqItemId);
        }
        
        quest.setCustom(rs.getBoolean("is_custom"));
        quest.setDaily(rs.getBoolean("is_daily"));
        
        return quest;
    }
}

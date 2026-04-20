package com.forge.repository;

import com.forge.model.QuestProgress;
import com.forge.model.Quest;
import com.forge.model.QuestProgress.QuestStatus;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestProgressRepository {

    public int create(QuestProgress progress) throws SQLException {
        String sql = "INSERT INTO user_progress (user_id, quest_id, status, expires_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, progress.getUserId());
            stmt.setInt(2, progress.getQuestId());
            stmt.setString(3, progress.getStatus().name());
            stmt.setTimestamp(4, progress.getExpiresAt() != null ? 
                Timestamp.valueOf(progress.getExpiresAt()) : null);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM user_progress WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    public List<QuestProgress> findByUserId(int userId) throws SQLException {
        List<QuestProgress> progressList = new ArrayList<>();
        String sql = "SELECT up.*, q.title, q.description, q.xp_reward, q.coin_reward, q.penalty_type, q.penalty_value " +
            "FROM user_progress up JOIN quests q ON up.quest_id = q.id WHERE up.user_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                QuestProgress progress = mapResultSetToQuestProgress(rs);
                Quest quest = new Quest();
                quest.setId(rs.getInt("quest_id"));
                quest.setTitle(rs.getString("title"));
                quest.setDescription(rs.getString("description"));
                quest.setXpReward(rs.getInt("xp_reward"));
                quest.setCoinReward(rs.getInt("coin_reward"));
                quest.setPenaltyType(rs.getString("penalty_type"));
                quest.setPenaltyValue(rs.getInt("penalty_value"));
                progress.setQuest(quest);
                progressList.add(progress);
            }
        }
        return progressList;
    }

    public List<QuestProgress> findPendingByUserId(int userId) throws SQLException {
        List<QuestProgress> progressList = new ArrayList<>();
        String sql = "SELECT up.*, q.title, q.description, q.xp_reward, q.coin_reward, q.penalty_type, q.penalty_value " +
            "FROM user_progress up JOIN quests q ON up.quest_id = q.id WHERE up.user_id = ? AND up.status = 'PENDING'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                QuestProgress progress = mapResultSetToQuestProgress(rs);
                Quest quest = new Quest();
                quest.setId(rs.getInt("quest_id"));
                quest.setTitle(rs.getString("title"));
                quest.setDescription(rs.getString("description"));
                quest.setXpReward(rs.getInt("xp_reward"));
                quest.setCoinReward(rs.getInt("coin_reward"));
                quest.setPenaltyType(rs.getString("penalty_type"));
                quest.setPenaltyValue(rs.getInt("penalty_value"));
                progress.setQuest(quest);
                progressList.add(progress);
            }
        }
        return progressList;
    }

    public void updateStatus(int id, QuestStatus status) throws SQLException {
        String sql = "UPDATE user_progress SET status = ?, completed_at = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setTimestamp(2, status == QuestStatus.COMPLETED || status == QuestStatus.FAILED ? 
                new Timestamp(System.currentTimeMillis()) : null);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        }
    }
    
    public Optional<QuestProgress> findById(int id) throws SQLException {
        String sql = "SELECT up.*, q.title, q.description, q.xp_reward, q.coin_reward, q.penalty_type, q.penalty_value " +
            "FROM user_progress up JOIN quests q ON up.quest_id = q.id WHERE up.id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                QuestProgress progress = mapResultSetToQuestProgress(rs);
                Quest quest = new Quest();
                quest.setId(rs.getInt("quest_id"));
                quest.setTitle(rs.getString("title"));
                quest.setDescription(rs.getString("description"));
                quest.setXpReward(rs.getInt("xp_reward"));
                quest.setCoinReward(rs.getInt("coin_reward"));
                quest.setPenaltyType(rs.getString("penalty_type"));
                quest.setPenaltyValue(rs.getInt("penalty_value"));
                progress.setQuest(quest);
                return Optional.of(progress);
            }
        }
        return Optional.empty();
    }

    private QuestProgress mapResultSetToQuestProgress(ResultSet rs) throws SQLException {
        QuestProgress progress = new QuestProgress();
        progress.setId(rs.getInt("id"));
        progress.setUserId(rs.getInt("user_id"));
        progress.setQuestId(rs.getInt("quest_id"));
        progress.setStatus(QuestStatus.valueOf(rs.getString("status")));
        progress.setStartedAt(rs.getTimestamp("started_at").toLocalDateTime());
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            progress.setExpiresAt(expiresAt.toLocalDateTime());
        }
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            progress.setCompletedAt(completedAt.toLocalDateTime());
        }
        return progress;
    }
}
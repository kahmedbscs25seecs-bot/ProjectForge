package com.forge.repository;

import com.forge.model.Battle;
import com.forge.model.BattleAction;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BattleRepository {

    public int create(Battle battle) throws SQLException {
        String sql = "INSERT INTO battles (attacker_id, defender_id, attacker_hp_before, defender_hp_before, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, battle.getAttackerId());
            stmt.setInt(2, battle.getDefenderId());
            stmt.setInt(3, battle.getAttackerHpBefore());
            stmt.setInt(4, battle.getDefenderHpBefore());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public void update(Battle battle) throws SQLException {
        String sql = "UPDATE battles SET winner_id = ?, attacker_points_change = ?, defender_points_change = ?, status = ?, completed_at = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, battle.getWinnerId() != null ? battle.getWinnerId() : 0);
            stmt.setInt(2, battle.getAttackerPointsChange());
            stmt.setInt(3, battle.getDefenderPointsChange());
            stmt.setString(4, battle.getStatus());
            stmt.setTimestamp(5, battle.getCompletedAt() != null ? Timestamp.valueOf(battle.getCompletedAt()) : null);
            stmt.setInt(6, battle.getId());
            stmt.executeUpdate();
        }
    }

    public Optional<Battle> findById(int id) throws SQLException {
        String sql = "SELECT * FROM battles WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToBattle(rs));
            }
        }
        return Optional.empty();
    }

    public List<Battle> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM battles WHERE attacker_id = ? OR defender_id = ? ORDER BY created_at DESC";
        List<Battle> battles = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                battles.add(mapResultSetToBattle(rs));
            }
        }
        return battles;
    }

    public List<Battle> getRecentBattles(int limit) throws SQLException {
        String sql = "SELECT * FROM battles WHERE status = 'COMPLETED' ORDER BY completed_at DESC LIMIT ?";
        List<Battle> battles = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                battles.add(mapResultSetToBattle(rs));
            }
        }
        return battles;
    }

    private Battle mapResultSetToBattle(ResultSet rs) throws SQLException {
        Battle battle = new Battle();
        battle.setId(rs.getInt("id"));
        battle.setAttackerId(rs.getInt("attacker_id"));
        battle.setDefenderId(rs.getInt("defender_id"));
        battle.setAttackerHpBefore(rs.getInt("attacker_hp_before"));
        battle.setDefenderHpBefore(rs.getInt("defender_hp_before"));
        battle.setWinnerId(rs.getInt("winner_id"));
        if (rs.wasNull()) battle.setWinnerId(null);
        battle.setAttackerPointsChange(rs.getInt("attacker_points_change"));
        battle.setDefenderPointsChange(rs.getInt("defender_points_change"));
        battle.setStatus(rs.getString("status"));
        battle.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        battle.setCompletedAt(rs.getTimestamp("completed_at") != null ? rs.getTimestamp("completed_at").toLocalDateTime() : null);
        return battle;
    }

    public void addAction(BattleAction action) throws SQLException {
        String sql = "INSERT INTO battle_actions (battle_id, attacker_id, defender_id, round_number, attack_type, defense_type, damage_dealt, damage_reduced, turn_order) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, action.getBattleId());
            stmt.setInt(2, action.getAttackerId());
            stmt.setInt(3, action.getDefenderId());
            stmt.setInt(4, action.getRoundNumber());
            stmt.setString(5, action.getAttackType());
            stmt.setString(6, action.getDefenseType());
            stmt.setInt(7, action.getDamageDealt());
            stmt.setInt(8, action.getDamageReduced());
            stmt.setInt(9, action.getTurnOrder());
            stmt.executeUpdate();
        }
    }

    public List<BattleAction> getBattleActions(int battleId) throws SQLException {
        String sql = "SELECT * FROM battle_actions WHERE battle_id = ? ORDER BY round_number, turn_order";
        List<BattleAction> actions = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, battleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BattleAction action = new BattleAction();
                action.setId(rs.getInt("id"));
                action.setBattleId(rs.getInt("battle_id"));
                action.setAttackerId(rs.getInt("attacker_id"));
                action.setDefenderId(rs.getInt("defender_id"));
                action.setRoundNumber(rs.getInt("round_number"));
                action.setAttackType(rs.getString("attack_type"));
                action.setDefenseType(rs.getString("defense_type"));
                action.setDamageDealt(rs.getInt("damage_dealt"));
                action.setDamageReduced(rs.getInt("damage_reduced"));
                action.setTurnOrder(rs.getInt("turn_order"));
                action.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                actions.add(action);
            }
        }
        return actions;
    }
}
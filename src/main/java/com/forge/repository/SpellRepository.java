package com.forge.repository;

import com.forge.model.Spell;
import com.forge.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpellRepository {

    public List<Spell> getAllSpells() throws SQLException {
        String sql = "SELECT * FROM spells ORDER BY rarity, type";
        List<Spell> spells = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                spells.add(mapResultSetToSpell(rs));
            }
        }
        return spells;
    }

    public List<Spell> getAttackSpells() throws SQLException {
        String sql = "SELECT * FROM spells WHERE type = 'ATTACK' ORDER BY rarity";
        List<Spell> spells = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                spells.add(mapResultSetToSpell(rs));
            }
        }
        return spells;
    }

    public List<Spell> getDefenseSpells() throws SQLException {
        String sql = "SELECT * FROM spells WHERE type = 'DEFENSE' ORDER BY rarity";
        List<Spell> spells = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                spells.add(mapResultSetToSpell(rs));
            }
        }
        return spells;
    }

    public List<Spell> getSpellsByType(String type) throws SQLException {
        String sql = "SELECT * FROM spells WHERE type = ? ORDER BY rarity";
        List<Spell> spells = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                spells.add(mapResultSetToSpell(rs));
            }
        }
        return spells;
    }

    public Optional<Spell> findById(int id) throws SQLException {
        String sql = "SELECT * FROM spells WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToSpell(rs));
            }
        }
        return Optional.empty();
    }

    private Spell mapResultSetToSpell(ResultSet rs) throws SQLException {
        Spell spell = new Spell();
        spell.setId(rs.getInt("id"));
        spell.setName(rs.getString("name"));
        spell.setDescription(rs.getString("description"));
        spell.setType(Spell.SpellType.valueOf(rs.getString("type")));
        spell.setBaseDamage(rs.getInt("base_damage"));
        spell.setManaCost(rs.getInt("mana_cost"));
        spell.setAccuracy(rs.getInt("accuracy"));
        spell.setEffect(rs.getString("effect"));
        spell.setRarity(Spell.Rarity.valueOf(rs.getString("rarity")));
        return spell;
    }
}
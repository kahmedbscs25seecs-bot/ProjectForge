package com.forge.service;

import com.forge.model.Battle;
import com.forge.model.BattleAction;
import com.forge.model.User;
import com.forge.repository.BattleRepository;
import com.forge.repository.UserRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BattleService {
    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final Random random;

    public static final String ATTACK_SWORD = "SWORD";
    public static final String ATTACK_WAND = "WAND";
    public static final String DEFENSE_SHIELD = "SHIELD";
    public static final String DEFENSE_WAND = "WAND";

    private static final int WIN_ON_ATTACK_POINTS = 20;
    private static final int WIN_ON_DEFENSE_POINTS = 25;
    private static final int LOSE_ON_ATTACK_POINTS = -15;
    private static final int LOSE_ON_DEFENSE_POINTS = -10;

    public BattleService() {
        this.battleRepository = new BattleRepository();
        this.userRepository = new UserRepository();
        this.random = new Random();
    }

    public Battle startBattle(int attackerId, int defenderId) throws SQLException {
        Optional<User> attackerOpt = userRepository.findById(attackerId);
        Optional<User> defenderOpt = userRepository.findById(defenderId);

        if (attackerOpt.isEmpty() || defenderOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid user IDs");
        }

        User attacker = attackerOpt.get();
        User defender = defenderOpt.get();

        Battle battle = new Battle(attackerId, defenderId);
        battle.setAttackerHpBefore(attacker.getCurrentHp());
        battle.setDefenderHpBefore(defender.getCurrentHp());

        int battleId = battleRepository.create(battle);
        battle.setId(battleId);

        return battle;
    }

    public BattleAction executeTurn(Battle battle, String attackType, int attackerId) throws SQLException {
        User attacker = userRepository.findById(attackerId).orElseThrow();
        int defenderId = (attackerId == battle.getAttackerId()) ? battle.getDefenderId() : battle.getAttackerId();
        User defender = userRepository.findById(defenderId).orElseThrow();

        int currentAttackerHp = (attackerId == battle.getAttackerId()) ? battle.getAttackerHpBefore() : battle.getDefenderHpBefore();
        int currentDefenderHp = (attackerId == battle.getAttackerId()) ? battle.getDefenderHpBefore() : battle.getAttackerHpBefore();

        int damage = calculateDamage(attacker, defender, attackType, defender.getDefenseType());
        int damageReduced = calculateDamageReduction(defender, attackType, defender.getDefenseType());
        int finalDamage = Math.max(0, damage - damageReduced);

        int roundNumber = battleRepository.getBattleActions(battle.getId()).size() / 2 + 1;
        int turnOrder = (battleRepository.getBattleActions(battle.getId()).size() % 2) + 1;

        BattleAction action = new BattleAction(
            battle.getId(), attackerId, defenderId, roundNumber,
            0, attackType, defender.getDefenseType(), "", finalDamage, damageReduced, turnOrder, true
        );
        battleRepository.addAction(action);

        if (attackerId == battle.getAttackerId()) {
            battle.setDefenderHpBefore(currentDefenderHp - finalDamage);
        } else {
            battle.setAttackerHpBefore(currentAttackerHp - finalDamage);
        }

        return action;
    }

    private int calculateDamage(User attacker, User defender, String attackType, String defenseType) {
        int baseDamage;
        double variance;

        if (attackType.equals(ATTACK_SWORD)) {
            baseDamage = attacker.getAttack() + random.nextInt(11) - 5;
            variance = 0.1;
        } else {
            baseDamage = attacker.getAttack() + random.nextInt(21) - 10;
            variance = 0.3;
        }

        if (defenseType.equals(DEFENSE_WAND)) {
            double luckMultiplier = 1.0 + (attacker.getLuck() / 100.0);
            baseDamage = (int) (baseDamage * luckMultiplier);
        }

        int varianceAmount = (int) (baseDamage * variance);
        baseDamage += random.nextInt(varianceAmount * 2 + 1) - varianceAmount;

        return Math.max(1, baseDamage);
    }

    private int calculateDamageReduction(User defender, String attackType, String defenseType) {
        int baseReduction = defender.getDefense() / 2;

        if (defenseType.equals(DEFENSE_SHIELD)) {
            return baseReduction + random.nextInt(6);
        } else if (defenseType.equals(DEFENSE_WAND)) {
            if (attackType.equals(ATTACK_WAND)) {
                double bonus = 1.0 + (defender.getLuck() / 50.0);
                return (int) (baseReduction * bonus);
            }
            return baseReduction / 2;
        }

        return baseReduction;
    }

    public Battle completeBattle(Battle battle) throws SQLException {
        int attackerCurrentHp = battle.getAttackerHpBefore();
        int defenderCurrentHp = battle.getDefenderHpBefore();

        Integer winnerId;
        int attackerPointsChange;
        int defenderPointsChange;

        if (attackerCurrentHp <= 0 && defenderCurrentHp <= 0) {
            winnerId = null;
            attackerPointsChange = 0;
            defenderPointsChange = 0;
        } else if (attackerCurrentHp <= 0) {
            winnerId = battle.getDefenderId();
            attackerPointsChange = LOSE_ON_ATTACK_POINTS;
            defenderPointsChange = WIN_ON_DEFENSE_POINTS;
        } else if (defenderCurrentHp <= 0) {
            winnerId = battle.getAttackerId();
            attackerPointsChange = WIN_ON_ATTACK_POINTS;
            defenderPointsChange = LOSE_ON_DEFENSE_POINTS;
        } else {
            winnerId = null;
            attackerPointsChange = 0;
            defenderPointsChange = 0;
        }

        battle.setWinnerId(winnerId);
        battle.setAttackerPointsChange(attackerPointsChange);
        battle.setDefenderPointsChange(defenderPointsChange);
        battle.setStatus("COMPLETED");
        battle.setCompletedAt(LocalDateTime.now());

        battleRepository.update(battle);

        Optional<User> attackerOpt = userRepository.findById(battle.getAttackerId());
        Optional<User> defenderOpt = userRepository.findById(battle.getDefenderId());

        if (attackerOpt.isPresent()) {
            User attacker = attackerOpt.get();
            attacker.setCurrentHp(Math.max(0, attackerCurrentHp));
            attacker.setRankPoints(Math.max(0, attacker.getRankPoints() + attackerPointsChange));
            userRepository.update(attacker);
        }

        if (defenderOpt.isPresent()) {
            User defender = defenderOpt.get();
            defender.setCurrentHp(Math.max(0, defenderCurrentHp));
            defender.setRankPoints(Math.max(0, defender.getRankPoints() + defenderPointsChange));
            userRepository.update(defender);
        }

        return battle;
    }

    public Optional<User> findOpponent(int userId) throws SQLException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        
        // Expand search range: start with ±50, expand by 25 each time up to ±200
        for (int range = 50; range <= 200; range += 25) {
            List<User> candidates = userRepository.findSimilarRank(userId, range);
            candidates.removeIf(u -> u.getId() == userId);
            candidates.removeIf(u -> u.getCurrentHp() <= 0);
            
            if (!candidates.isEmpty()) {
                return Optional.of(candidates.get(random.nextInt(candidates.size())));
            }
        }
        
        // If no real opponents found, create a bot
        return Optional.of(createBotOpponent(user));
    }
    
    private User createBotOpponent(User player) {
        User bot = new User();
        String[] botNames = {"Shadow_AI", "Storm_Bot", "Frost_Mage", "Dark_Knight", "Phoenix_AI"};
        bot.setUsername(botNames[random.nextInt(botNames.length)] + "_" + System.currentTimeMillis() % 10000);
        bot.setEmail("bot_" + System.currentTimeMillis() + "@forge.com");
        bot.setPasswordHash("bot_password_hash");
        
        // Bot stats: similar to player (±10%)
        int atkVariance = (int)(player.getAttack() * 0.1);
        int defVariance = (int)(player.getDefense() * 0.1);
        bot.setAttack(Math.max(10, player.getAttack() + random.nextInt(atkVariance * 2 + 1) - atkVariance));
        bot.setDefense(Math.max(10, player.getDefense() + random.nextInt(defVariance * 2 + 1) - defVariance));
        bot.setLuck(player.getLuck());
        
        // Bot level similar to player
        bot.setLevel(player.getLevel());
        
        // Bot HP based on level + defense
        bot.setMaxHp(50 + (player.getLevel() * 10) + bot.getDefense());
        bot.setCurrentHp(bot.getMaxHp());
        
        // Bot rank similar to player
        bot.setRankPoints(player.getRankPoints());
        bot.setRank(player.getPlayerRank());
        
        // Default battle settings
        bot.setDefenseType(DEFENSE_SHIELD);
        bot.setCoins(0);
        bot.setXp(0);
        bot.setCurrentStreak(0);
        bot.setLongestStreak(0);
        
        try {
            int botId = userRepository.create(bot);
            bot.setId(botId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return bot;
    }

    public List<Battle> getUserBattles(int userId) throws SQLException {
        return battleRepository.findByUserId(userId);
    }

    public List<BattleAction> getBattleActions(int battleId) throws SQLException {
        return battleRepository.getBattleActions(battleId);
    }

    public Optional<Battle> getBattleById(int battleId) throws SQLException {
        return battleRepository.findById(battleId);
    }
}
package com.forge.service;

import com.forge.model.*;
import com.forge.repository.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class HarryPotterBattleService {
    private final BattleRepository battleRepository;
    private final UserRepository userRepository;
    private final SpellRepository spellRepository;
    private final WandRepository wandRepository;
    private final Random random;

    public HarryPotterBattleService() {
        this.battleRepository = new BattleRepository();
        this.userRepository = userRepository;
        this.spellRepository = new SpellRepository();
        this.wandRepository = new WandRepository();
        this.random = new Random();
    }

    public List<Spell> getAllSpells() throws SQLException {
        return spellRepository.getAllSpells();
    }

    public List<Spell> getAttackSpells() throws SQLException {
        return spellRepository.getAttackSpells();
    }

    public List<Spell> getDefenseSpells() throws SQLException {
        return spellRepository.getDefenseSpells();
    }

    public Battle startBattle(int player1Id, int player2Id) throws SQLException {
        Optional<User> player1Opt = userRepository.findById(player1Id);
        Optional<User> player2Opt = userRepository.findById(player2Id);

        if (player1Opt.isEmpty() || player2Opt.isEmpty()) {
            throw new IllegalArgumentException("Invalid user IDs");
        }

        User player1 = player1Opt.get();
        User player2 = player2Opt.get();

        Battle battle = new Battle(player1Id, player2Id);
        battle.setAttackerHpBefore(player1.getCurrentHp());
        battle.setDefenderHpBefore(player2.getCurrentHp());

        int battleId = battleRepository.create(battle);
        battle.setId(battleId);

        return battle;
    }

    public BattleAction castSpell(Battle battle, Spell spell, int casterId, int targetId) throws SQLException {
        User caster = userRepository.findById(casterId).orElseThrow();
        User target = userRepository.findById(targetId).orElseThrow();
        
        Wand casterWand = wandRepository.findUserWand(casterId).orElse(null);
        int attackBonus = casterWand != null ? casterWand.getAttackBonus() : 0;
        int defenseBonus = casterWand != null ? casterWand.getDefenseBonus() : 0;
        int luckBonus = casterWand != null ? casterWand.getLuckBonus() : caster.getLuck();

        int currentCasterHp = (casterId == battle.getAttackerId()) ? battle.getAttackerHpBefore() : battle.getDefenderHpBefore();
        int currentTargetHp = (casterId == battle.getAttackerId()) ? battle.getDefenderHpBefore() : battle.getAttackerHpBefore();

        boolean hit = calculateHit(spell, caster, luckBonus);
        int damage = 0;
        int damageReduced = 0;
        String statusEffect = null;

        if (hit && spell.getType() == Spell.SpellType.ATTACK) {
            damage = calculateDamage(spell, caster, target, attackBonus);
            
            int defenseReduction = calculateDefenseReduction(target, defenseBonus);
            damageReduced = Math.min(damage, defenseReduction);
            damage = Math.max(0, damage - damageReduced);
            
            if (spell.getEffect() != null) {
                statusEffect = applyStatusEffect(spell.getEffect(), target);
            }
        } else if (spell.getType() == Spell.SpellType.DEFENSE) {
            damage = applyDefenseSpell(spell, caster, defenseBonus);
            statusEffect = "SHIELD_ACTIVE";
        } else if (spell.getType() == Spell.SpellType.UTILITY) {
            damage = applyUtilitySpell(spell);
            statusEffect = spell.getEffect();
        }

        int roundNumber = battleRepository.getBattleActions(battle.getId()).size() / 2 + 1;
        int turnOrder = (battleRepository.getBattleActions(battle.getId()).size() % 2) + 1;

        BattleAction action = new BattleAction(
            battle.getId(), casterId, targetId, roundNumber,
            spell.getId(), spell.getName(), spell.getType().name(), spell.getEffect(),
            damage, damageReduced, turnOrder, hit
        );
        action.setStatusEffect(statusEffect);
        battleRepository.addAction(action);

        if (casterId == battle.getAttackerId()) {
            battle.setDefenderHpBefore(Math.max(0, currentTargetHp - damage));
        } else {
            battle.setAttackerHpBefore(Math.max(0, currentCasterHp - damage));
        }

        return action;
    }

    private boolean calculateHit(Spell spell, User caster, int luckBonus) {
        int accuracy = spell.getAccuracy();
        int luckModifier = luckBonus / 2;
        int roll = random.nextInt(100) + 1;
        return roll <= (accuracy + luckModifier);
    }

    private int calculateDamage(Spell spell, User attacker, User defender, int attackBonus) {
        int baseDamage = spell.getBaseDamage();
        int attackStat = attacker.getAttack() + attackBonus;
        
        double variance = 0.2;
        int varianceAmount = (int) (baseDamage * variance);
        baseDamage += random.nextInt(varianceAmount * 2 + 1) - varianceAmount;

        double levelMultiplier = 1.0 + (attacker.getLevel() - 1) * 0.1;
        baseDamage = (int) (baseDamage * levelMultiplier);

        baseDamage += attackStat / 2;

        return Math.max(1, baseDamage);
    }

    private int calculateDefenseReduction(User defender, int defenseBonus) {
        int baseDefense = defender.getDefense() + defenseBonus;
        return baseDefense + random.nextInt(11) - 5;
    }

    private String applyStatusEffect(String effect, User target) {
        switch (effect) {
            case "STUN":
                return "STUNNED";
            case "BURN":
                return "BURNING";
            case "WET":
                return "WET";
            case "CONFUSE":
                return "CONFUSED";
            case "DISARM":
                return "DISARMED";
            default:
                return null;
        }
    }

    private int applyDefenseSpell(Spell spell, User caster, int defenseBonus) {
        int shieldValue = spell.getBaseDamage() + (defenseBonus * 2);
        return -shieldValue;
    }

    private int applyUtilitySpell(Spell spell) {
        return 0;
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
            attackerPointsChange = -15;
            defenderPointsChange = +25;
        } else if (defenderCurrentHp <= 0) {
            winnerId = battle.getAttackerId();
            attackerPointsChange = +20;
            defenderPointsChange = -10;
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
        List<User> similarRankUsers = userRepository.findSimilarRank(userId, 50);

        similarRankUsers.removeIf(u -> u.getId() == userId);
        similarRankUsers.removeIf(u -> u.getCurrentHp() <= 0);

        if (similarRankUsers.isEmpty()) {
            List<User> topPlayers = userRepository.getTopPlayers(10);
            topPlayers.removeIf(u -> u.getId() == userId);
            topPlayers.removeIf(u -> u.getCurrentHp() <= 0);
            if (!topPlayers.isEmpty()) {
                return Optional.of(topPlayers.get(random.nextInt(topPlayers.size())));
            }
            return Optional.empty();
        }

        return Optional.of(similarRankUsers.get(random.nextInt(similarRankUsers.size())));
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

    public String getOpponentAnalysis(User opponent) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("⚔️ OPPONENT ANALYSIS ⚔️\n\n");
        
        analysis.append("📊 STATS:\n");
        analysis.append("   Level: ").append(opponent.getLevel()).append("\n");
        analysis.append("   Attack: ").append(opponent.getAttack()).append("\n");
        analysis.append("   Defense: ").append(opponent.getDefense()).append("\n");
        analysis.append("   Luck: ").append(opponent.getLuck()).append("\n\n");
        
        analysis.append("💗 HEALTH: ").append(opponent.getCurrentHp())
               .append("/").append(opponent.getMaxHp()).append("\n\n");
        
        analysis.append("🎯 TIPS:\n");
        if (opponent.getDefense() > 15) {
            analysis.append("   - Enemy has HIGH defense! Use debuffs.\n");
        }
        if (opponent.getLuck() > 10) {
            analysis.append("   - Enemy is LUCKY! Accuracy may fail.\n");
        }
        if (opponent.getLevel() > 15) {
            analysis.append("   - Enemy is STRONG! Watch out!\n");
        }
        
        return analysis.toString();
    }
}
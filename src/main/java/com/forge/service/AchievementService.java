package com.forge.service;

import com.forge.model.Achievement;
import com.forge.model.User;
import com.forge.repository.AchievementRepository;
import com.forge.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    public AchievementService() {
        this.achievementRepository = new AchievementRepository();
        this.userRepository = new UserRepository();
    }

    public List<Achievement> getUnlockedAchievements(int userId) throws SQLException {
        return achievementRepository.findUnlockedByUserId(userId);
    }

    public List<Achievement> getAllAchievements() throws SQLException {
        return achievementRepository.findAll();
    }

    public void checkAndUnlockAchievements(int userId) throws SQLException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;
        
        User user = userOpt.get();

        // Achievement 1: First Quest - Complete 1 quest
        checkAndUnlock(userId, 1, user.getTotalQuestsCompleted() >= 1);

        // Achievement 2: Level 5 - Reach level 5
        checkAndUnlock(userId, 2, user.getLevel() >= 5);

        // Achievement 3: Coin Collector - Earn 100 coins total
        checkAndUnlock(userId, 3, user.getTotalCoinsEarned() >= 100);

        // Achievement 4: Quest Master - Complete 50 quests
        checkAndUnlock(userId, 4, user.getTotalQuestsCompleted() >= 50);

        // Achievement 5: Level 10 - Reach level 10
        checkAndUnlock(userId, 5, user.getLevel() >= 10);

        // Achievement 6: Wealthy - Have 200 coins at once
        checkAndUnlock(userId, 6, user.getCoins() >= 200);
    }

    private void checkAndUnlock(int userId, int achievementId, boolean condition) throws SQLException {
        if (condition && !achievementRepository.hasAchievement(userId, achievementId)) {
            achievementRepository.unlockAchievement(userId, achievementId);
            System.out.println("Achievement unlocked: " + achievementId);
        }
    }
}
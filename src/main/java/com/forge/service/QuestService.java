package com.forge.service;

import com.forge.model.Quest;
import com.forge.model.QuestProgress;
import com.forge.model.User;
import com.forge.model.UserInventory;
import com.forge.repository.QuestRepository;
import com.forge.repository.QuestProgressRepository;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class QuestService {
    private final QuestRepository questRepository;
    private final QuestProgressRepository progressRepository;
    private final UserService userService;
    private final AchievementService achievementService;
    private final InventoryService inventoryService;

    public QuestService() {
        this.questRepository = new QuestRepository();
        this.progressRepository = new QuestProgressRepository();
        this.userService = new UserService();
        this.achievementService = new AchievementService();
        this.inventoryService = new InventoryService();
    }

    public List<Quest> getQuestsByMode(int modeId) throws SQLException {
        return questRepository.findByModeId(modeId);
    }

    public List<Quest> getAvailableQuests(int userId) throws SQLException {
        return questRepository.findByUserId(userId);
    }
    
    public Optional<Quest> getQuestById(int questId) throws SQLException {
        return questRepository.findById(questId);
    }
    
    public int[] getModeProgressStats(int userId, int modeId) throws SQLException {
        int completed = 0;
        int easy = 0, medium = 0, hard = 0;
        int totalXp = 0;
        int totalCoins = 0;
        
        List<QuestProgress> progressList = progressRepository.findByUserId(userId);
        for (QuestProgress progress : progressList) {
            if (progress.getStatus() == QuestProgress.QuestStatus.COMPLETED) {
                Quest quest = questRepository.findById(progress.getQuestId()).orElse(null);
                if (quest != null && quest.getModeId() == modeId) {
                    completed++;
                    totalXp += quest.getXpReward();
                    totalCoins += quest.getCoinReward();
                    
                    if (quest.getDifficulty() != null) {
                        switch (quest.getDifficulty()) {
                            case EASY -> easy++;
                            case MEDIUM -> medium++;
                            case HARD, BOSS -> hard++;
                        }
                    }
                }
            }
        }
        
        return new int[]{completed, easy, medium, hard, totalXp, totalCoins};
    }

    public List<QuestProgress> getUserProgress(int userId) throws SQLException {
        return progressRepository.findByUserId(userId);
    }

    public List<QuestProgress> getActiveQuests(int userId) throws SQLException {
        List<QuestProgress> active = progressRepository.findPendingByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        for (QuestProgress progress : active) {
            if (progress.getExpiresAt() != null && progress.getExpiresAt().isBefore(now)) {
                progressRepository.delete(progress.getId());
            }
        }
        return progressRepository.findPendingByUserId(userId);
    }

    public int startQuest(int userId, int questId) throws SQLException {
        Optional<Quest> questOpt = questRepository.findById(questId);
        if (questOpt.isEmpty()) return -1;
        
        Quest quest = questOpt.get();
        QuestProgress progress = new QuestProgress(userId, questId);
        progress.setStatus(QuestProgress.QuestStatus.IN_PROGRESS);
        
        LocalDateTime expiresAt = LocalDateTime.now().plus(quest.getTimeLimitHours(), ChronoUnit.HOURS);
        progress.setExpiresAt(expiresAt);
        
        return progressRepository.create(progress);
    }

    public String checkGearRequirements(int userId, Quest quest) {
        if (quest.getRequiredSlot() == null && quest.getRequiredItemId() == 0) {
            return null;
        }
        
        try {
            List<UserInventory> inventory = inventoryService.getUserInventory(userId);
            for (UserInventory ui : inventory) {
                if (ui.isEquipped()) {
                    if (quest.getRequiredSlot() != null && ui.getItem().getSlot() == quest.getRequiredSlot()) {
                        if (quest.getRequiredItemId() == 0 || ui.getItemId() == quest.getRequiredItemId()) {
                            return null;
                        }
                    }
                }
            }
            String required = quest.getRequiredSlot() != null ? quest.getRequiredSlot().name() : "specific item";
            return "Requires " + required + " equipped";
        } catch (SQLException e) {
            return "Error checking requirements";
        }
    }

    public void completeQuest(int progressId, int userId) throws SQLException {
        List<QuestProgress> progressList = progressRepository.findByUserId(userId);
        Optional<QuestProgress> progressOpt = progressList.stream()
            .filter(p -> p.getId() == progressId)
            .findFirst();
        
        if (progressOpt.isPresent()) {
            QuestProgress progress = progressOpt.get();
            Optional<Quest> questOpt = questRepository.findById(progress.getQuestId());
            
            if (questOpt.isPresent()) {
                Quest quest = questOpt.get();
                
                if (progress.getExpiresAt() != null && progress.getExpiresAt().isBefore(LocalDateTime.now())) {
                    progress.setStatus(QuestProgress.QuestStatus.FAILED);
                    progressRepository.updateStatus(progressId, QuestProgress.QuestStatus.FAILED);
                    return;
                }
                
                progress.setStatus(QuestProgress.QuestStatus.COMPLETED);
                progressRepository.updateStatus(progressId, QuestProgress.QuestStatus.COMPLETED);
                
                Optional<User> userOpt = userService.getUserById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.addXp(quest.getXpReward());
                    user.setCoins(user.getCoins() + quest.getCoinReward());
                    user.setTotalQuestsCompleted(user.getTotalQuestsCompleted() + 1);
                    user.setTotalCoinsEarned(user.getTotalCoinsEarned() + quest.getCoinReward());
                    userService.updateUser(user);
                    
                    updateStreak(user);
                    achievementService.checkAndUnlockAchievements(userId);
                }
            }
        }
    }

    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActive = user.getLastActiveDate();
        
        if (lastActive == null) {
            user.setCurrentStreak(1);
        } else if (ChronoUnit.DAYS.between(lastActive, today) == 1) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
            if (user.getCurrentStreak() > user.getLongestStreak()) {
                user.setLongestStreak(user.getCurrentStreak());
            }
        } else if (ChronoUnit.DAYS.between(lastActive, today) > 1) {
            user.setCurrentStreak(1);
        }
        user.setLastActiveDate(today);
    }

    public void failQuest(int progressId, int userId) throws SQLException {
        Optional<QuestProgress> progressOpt = progressRepository.findById(progressId);
        
        if (progressOpt.isPresent()) {
            QuestProgress progress = progressOpt.get();
            Optional<Quest> questOpt = questRepository.findById(progress.getQuestId());
            
            if (questOpt.isPresent()) {
                Quest quest = questOpt.get();
                
                if ("lose_coins".equals(quest.getPenaltyType())) {
                    userService.subtractCoins(userId, quest.getPenaltyValue());
                }
            }
            
            progressRepository.delete(progressId);
        }
    }
    
    public void delete(int progressId) throws SQLException {
        progressRepository.delete(progressId);
    }

    public int createCustomQuest(Quest quest) throws SQLException {
        quest.setCustom(true);
        quest.setXpReward(0);
        quest.setCoinReward(0);
        return questRepository.create(quest);
    }

    public int createQuest(Quest quest) throws SQLException {
        return questRepository.create(quest);
    }
}
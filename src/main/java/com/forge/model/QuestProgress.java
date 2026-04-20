package com.forge.model;

import java.time.LocalDateTime;

public class QuestProgress {
    private int id;
    private int userId;
    private int questId;
    private QuestStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;
    private Quest quest;

    public enum QuestStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, EXPIRED
    }

    public QuestProgress() {}

    public QuestProgress(int userId, int questId) {
        this.userId = userId;
        this.questId = questId;
        this.status = QuestStatus.PENDING;
        this.startedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getQuestId() { return questId; }
    public void setQuestId(int questId) { this.questId = questId; }

    public QuestStatus getStatus() { return status; }
    public void setStatus(QuestStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Quest getQuest() { return quest; }
    public void setQuest(Quest quest) { this.quest = quest; }

    public void complete() {
        this.status = QuestStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = QuestStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }
}

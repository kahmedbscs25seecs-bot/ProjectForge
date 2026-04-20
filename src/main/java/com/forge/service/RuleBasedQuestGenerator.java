package com.forge.service;

import com.forge.model.Quest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RuleBasedQuestGenerator {
    private final Random random = new Random();
    
    private static final String[][] STUDY_QUESTS = {
        {"Read for 30 minutes", "Read any book or article for at least 30 minutes", "EASY"},
        {"Complete practice problems", "Solve 5-10 practice problems from your subject", "MEDIUM"},
        {"Watch educational video", "Watch an educational video related to your field", "EASY"},
        {"Take notes", "Write detailed notes from today's study session", "MEDIUM"},
        {"Teach someone", "Explain a concept you learned to someone else", "HARD"},
        {"Review past material", "Review notes from the past week", "EASY"},
        {"Create flashcards", "Create 20 flashcards for key concepts", "MEDIUM"},
        {"Take a practice test", "Complete a timed practice test", "HARD"},
        {"Mind map", "Create a mind map of a chapter", "MEDIUM"},
        {"Study group", "Join or form a study group session", "HARD"}
    };
    
    private static final String[][] GYM_QUESTS = {
        {"Morning workout", "Complete a 45-minute morning workout session", "EASY"},
        {"100 pushups", "Do 100 pushups throughout the day", "MEDIUM"},
        {"No junk food", "Avoid all junk food for one day", "EASY"},
        {"Drink 3L water", "Drink at least 3 liters of water", "EASY"},
        {"Cardio session", "Complete 30 minutes of cardio", "MEDIUM"},
        {"Protein goal", "Hit your daily protein target", "MEDIUM"},
        {"Stretch routine", "Complete a 20-minute stretching session", "EASY"},
        {"Leg day", "Complete a full leg workout", "HARD"},
        {"Meal prep", "Prepare all meals for the next day", "MEDIUM"},
        {"Weigh in", "Track your weight and measurements", "EASY"}
    };
    
    private static final String[][] SOCIAL_QUESTS = {
        {"Talk to stranger", "Have a 5-minute conversation with someone new", "EASY"},
        {"Social event", "Attend a social gathering or event", "HARD"},
        {"Share something", "Share something personal with a friend", "MEDIUM"},
        {"Make a call", "Call a friend or family member", "EASY"},
        {"Join club", "Attend a club or group meeting", "MEDIUM"},
        {"Compliment someone", "Give a genuine compliment to someone", "EASY"},
        {"Network event", "Attend a networking event", "HARD"},
        {"Coffee meetup", "Meet someone for coffee", "MEDIUM"},
        {"Online interaction", "Engage meaningfully on social media", "EASY"},
        {"Present ideas", "Present your ideas to a group", "HARD"}
    };
    
    private static final String[][] CREATIVE_QUESTS = {
        {"Sketch something", "Create a sketch or drawing", "EASY"},
        {"Write a story", "Write a short story (500+ words)", "MEDIUM"},
        {"Play instrument", "Practice your instrument for 30 minutes", "EASY"},
        {"Photo walk", "Go for a photo walk and capture 10 photos", "EASY"},
        {"Compose music", "Create a short piece of music", "HARD"},
        {"Learn technique", "Learn a new creative technique", "MEDIUM"},
        {"Complete project", "Work on a creative project for 1 hour", "MEDIUM"},
        {"Art piece", "Create a finished art piece", "HARD"},
        {"Write poem", "Write an original poem", "EASY"},
        {"Collaborate", "Create something with another person", "HARD"}
    };
    
    private static final String[][] MINDFUL_QUESTS = {
        {"Meditate 10 min", "Meditate for 10 minutes", "EASY"},
        {"Deep breathing", "Practice deep breathing exercises", "EASY"},
        {"Journal", "Write in a journal for 15 minutes", "EASY"},
        {"Gratitude list", "Write 3 things you're grateful for", "EASY"},
        {"Nature walk", "Take a peaceful walk in nature", "EASY"},
        {"Yoga session", "Complete a yoga session", "MEDIUM"},
        {"Digital detox", "No screens for 2 hours", "MEDIUM"},
        {"Sleep early", "Be in bed by 10 PM", "MEDIUM"},
        {"Affirmations", "Practice positive affirmations", "EASY"},
        {"Body scan", "Do a 20-minute body scan meditation", "HARD"}
    };
    
    public List<Quest> generateQuests(String modeName, int userLevel, int count) {
        List<Quest> quests = new ArrayList<>();
        String[][] questPool = getQuestPool(modeName);
        
        List<String[]> availableQuests = new ArrayList<>();
        for (String[] q : questPool) {
            availableQuests.add(q);
        }
        
        for (int i = 0; i < count && !availableQuests.isEmpty(); i++) {
            int index = random.nextInt(availableQuests.size());
            String[] questData = availableQuests.remove(index);
            
            Quest quest = createQuest(questData, userLevel);
            quests.add(quest);
        }
        
        return quests;
    }
    
    private String[][] getQuestPool(String modeName) {
        String name = modeName.toLowerCase();
        if (name.contains("study")) return STUDY_QUESTS;
        if (name.contains("body") || name.contains("gym") || name.contains("builder")) return GYM_QUESTS;
        if (name.contains("social") || name.contains("introvert")) return SOCIAL_QUESTS;
        if (name.contains("creative")) return CREATIVE_QUESTS;
        if (name.contains("mindful") || name.contains("meditation")) return MINDFUL_QUESTS;
        return STUDY_QUESTS;
    }
    
    private Quest createQuest(String[] questData, int userLevel) {
        Quest quest = new Quest();
        quest.setTitle(questData[0]);
        quest.setDescription(questData[1]);
        quest.setDifficulty(Quest.Difficulty.valueOf(questData[2]));
        quest.setTimeLimitHours(questData[2].equals("EASY") ? 12 : questData[2].equals("MEDIUM") ? 24 : 72);
        
        int baseXp = questData[2].equals("EASY") ? 30 : questData[2].equals("MEDIUM") ? 50 : 80;
        quest.setXpReward(baseXp + (userLevel * 2));
        
        int baseCoins = questData[2].equals("EASY") ? 15 : questData[2].equals("MEDIUM") ? 25 : 40;
        quest.setCoinReward(baseCoins + (userLevel));
        
        quest.setPenaltyType("lose_coins");
        quest.setPenaltyValue(quest.getCoinReward() / 2);
        quest.setCustom(true);
        
        return quest;
    }
    
    public List<Quest> generateCustomQuests(int userLevel, int count) {
        return generateQuests("Study", userLevel, count);
    }
}
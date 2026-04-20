package com.forge.service;

import com.forge.model.Quest;
import com.forge.model.Quest.Difficulty;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class AIQuestService {
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final RuleBasedQuestGenerator fallbackGenerator;
    private final HttpClient httpClient;
    
    public AIQuestService() {
        this.fallbackGenerator = new RuleBasedQuestGenerator();
        this.httpClient = HttpClient.newHttpClient();
    }
    
    public List<Quest> generateQuests(String modeName, String modeDescription, int userLevel, int count) {
        return generateQuests(modeName, modeDescription, userLevel, count, 0, 0, 0, 0, 0, 0);
    }
    
    public List<Quest> generateQuests(String modeName, String modeDescription, int userLevel, int count,
                                     int completedQuests, int easyCompleted, int mediumCompleted, int hardCompleted,
                                     int totalXpEarned, int totalCoinsEarned) {
        try {
            String prompt = buildPrompt(modeName, modeDescription, userLevel, count, 
                completedQuests, easyCompleted, mediumCompleted, hardCompleted,
                totalXpEarned, totalCoinsEarned);
            String response = callOllama(prompt);
            return parseResponse(response, userLevel);
        } catch (Exception e) {
            System.out.println("Ollama failed, using rule-based generator: " + e.getMessage());
            return fallbackGenerator.generateQuests(modeName, userLevel, count);
        }
    }
    
    private String buildPrompt(String modeName, String modeDescription, int userLevel, int count, 
                              int completedQuests, int easyCompleted, int mediumCompleted, int hardCompleted,
                              int totalXpEarned, int totalCoinsEarned) {
        
        String experienceLevel = determineExperienceLevel(completedQuests, userLevel);
        
        return String.format("""
            You are a quest generator for a gamified productivity app called FORGE.
            
            PLAYER CONTEXT:
            - Username: Player
            - User Level: %d
            - Total quests completed in this mode: %d
            - Quests by difficulty - Easy: %d, Medium: %d, Hard: %d
            - Total XP earned: %d
            - Total coins earned: %d
            - Experience level: %s
            
            MODE INFO:
            - Mode: "%s"
            - Description: %s
            
            TASK:
            Generate exactly %d quests that are appropriate for this player's experience level.
            
            RULES:
            - If player is BEGINNER (0-5 quests): mostly EASY quests, few MEDIUM
            - If player is INTERMEDIATE (6-15 quests): mix of EASY, MEDIUM, some HARD
            - If player is ADVANCED (16+ quests): mostly HARD, some MEDIUM challenges
            - Quests should COMPLEMENT what they've already done (if they did lots of easy, include harder)
            - Make difficulty based on their progress history
            
            Each quest must be:
            - A specific, actionable task
            - Appropriate for the mode's theme
            - Varied in difficulty (EASY, MEDIUM, HARD) based on player experience
            
            Respond ONLY with a JSON array with exactly %d quest objects:
            [{"title": "...", "description": "...", "difficulty": "EASY|MEDIUM|HARD"}, ...]
            
            Generate exactly %d diverse, appropriate quests:
            """, userLevel, completedQuests, easyCompleted, mediumCompleted, hardCompleted,
            totalXpEarned, totalCoinsEarned, experienceLevel,
            modeName, modeDescription, count, count, count);
    }
    
    private String determineExperienceLevel(int completedQuests, int userLevel) {
        int totalScore = completedQuests + (userLevel * 2);
        if (totalScore <= 5) return "BEGINNER";
        if (totalScore <= 15) return "INTERMEDIATE";
        return "ADVANCED";
    }
    
    private String callOllama(String prompt) throws Exception {
        String requestBody = String.format("""
            {
                "model": "llama3",
                "prompt": "%s",
                "stream": false
            }
            """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(OLLAMA_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("Ollama returned status: " + response.statusCode());
        }
        
        return response.body();
    }
    
    private List<Quest> parseResponse(String response, int userLevel) {
        List<Quest> quests = new ArrayList<>();
        
        try {
            String jsonPart = response;
            if (response.contains("\"response\":")) {
                int start = response.indexOf("\"response\":\"");
                int end = response.lastIndexOf("\"");
                if (start >= 0 && end > start) {
                    jsonPart = response.substring(start + 12, end);
                }
            }
            
            jsonPart = jsonPart.replace("\\n", " ").replace("\\\"", "\"");
            
            String[] questParts = jsonPart.split("\\},\\s*\\{");
            for (String part : questParts) {
                if (part.contains("title") && part.contains("description")) {
                    Quest quest = parseQuestJson("{" + part + "}", userLevel);
                    if (quest != null) {
                        quests.add(quest);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to parse AI response: " + e.getMessage());
        }
        
        if (quests.isEmpty()) {
            return fallbackGenerator.generateQuests("General", userLevel, 4);
        }
        
        return quests;
    }
    
    private Quest parseQuestJson(String json, int userLevel) {
        try {
            Quest quest = new Quest();
            
            String title = extractJsonValue(json, "title");
            String description = extractJsonValue(json, "description");
            String difficulty = extractJsonValue(json, "difficulty");
            
            if (title == null || description == null) return null;
            
            quest.setTitle(title);
            quest.setDescription(description);
            
            try {
                quest.setDifficulty(Difficulty.valueOf(difficulty.toUpperCase()));
            } catch (Exception e) {
                quest.setDifficulty(Difficulty.MEDIUM);
            }
            
            quest.setTimeLimitHours(switch (quest.getDifficulty()) {
                case EASY -> 12;
                case HARD -> 72;
                default -> 24;
            });
            
            int baseXp = switch (quest.getDifficulty()) {
                case EASY -> 30;
                case HARD -> 80;
                default -> 50;
            };
            quest.setXpReward(baseXp + (userLevel * 2));
            
            int baseCoins = switch (quest.getDifficulty()) {
                case EASY -> 15;
                case HARD -> 40;
                default -> 25;
            };
            quest.setCoinReward(baseCoins + userLevel);
            
            quest.setPenaltyType("lose_coins");
            quest.setPenaltyValue(quest.getCoinReward() / 2);
            quest.setCustom(true);
            
            return quest;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\"";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return null;
            
            int colonIndex = json.indexOf(":", keyIndex);
            if (colonIndex == -1) return null;
            
            int valueStart = colonIndex + 1;
            while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\"')) {
                valueStart++;
            }
            
            int valueEnd = valueStart;
            if (json.charAt(valueStart - 1) == '\"') {
                while (valueEnd < json.length() && json.charAt(valueEnd) != '\"') {
                    valueEnd++;
                }
            } else {
                while (valueEnd < json.length() && json.charAt(valueEnd) != ',' && json.charAt(valueEnd) != '}') {
                    valueEnd++;
                }
            }
            
            return json.substring(valueStart, valueEnd).trim();
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isOllamaAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/tags"))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
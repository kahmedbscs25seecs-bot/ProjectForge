package com.forge.controller;

import com.forge.model.Battle;
import com.forge.model.User;
import com.forge.repository.UserRepository;
import com.forge.service.BattleService;
import com.forge.util.DragUtil;
import com.forge.util.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LeaderboardController {
    @FXML
    private Label userRankLabel;
    @FXML
    private VBox leaderboardContainer;
    @FXML
    private VBox battleHistoryContainer;

    private final UserRepository userRepo = new UserRepository();
    private final BattleService battleService = new BattleService();
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        loadLeaderboard();
        loadBattleHistory();
        
        if (leaderboardContainer.getScene() != null) {
            Stage stage = (Stage) leaderboardContainer.getScene().getWindow();
            Parent root = leaderboardContainer.getScene().getRoot();
            DragUtil.makeDraggable(stage, root);
        }
    }

    private void loadLeaderboard() {
        leaderboardContainer.getChildren().clear();
        
        try {
            List<User> topPlayers = userRepo.getTopPlayers(20);
            
            int userRank = 0;
            for (int i = 0; i < topPlayers.size(); i++) {
                User player = topPlayers.get(i);
                if (player.getId() == currentUser.getId()) {
                    userRank = i + 1;
                    break;
                }
            }
            
            userRankLabel.setText("Your Rank: #" + (userRank > 0 ? userRank : "Unranked"));
            
            for (int i = 0; i < topPlayers.size(); i++) {
                User player = topPlayers.get(i);
                
                HBox row = new HBox(20);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 10; -fx-background-radius: 5;");
                
                Label rankLabel = new Label("#" + (i + 1));
                rankLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16; -fx-font-weight: bold;");
                rankLabel.setMinWidth(50);
                
                Label nameLabel = new Label(player.getUsername());
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
                nameLabel.setMinWidth(150);
                
                Label levelLabel = new Label("Lv." + player.getLevel());
                nameLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
                
                Label pointsLabel = new Label(player.getRankPoints() + " pts");
                pointsLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14;");
                
                if (player.getId() == currentUser.getId()) {
                    row.setStyle("-fx-background-color: #2a2a4e; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #FFD700; -fx-border-width: 1;");
                }
                
                row.getChildren().addAll(rankLabel, nameLabel, levelLabel, pointsLabel);
                leaderboardContainer.getChildren().add(row);
            }
            
            if (topPlayers.isEmpty()) {
                Label noData = new Label("No players yet");
                noData.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                leaderboardContainer.getChildren().add(noData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadBattleHistory() {
        battleHistoryContainer.getChildren().clear();
        
        try {
            List<Battle> battles = battleService.getUserBattles(currentUser.getId());
            
            for (Battle battle : battles) {
                if (battle.getStatus().equals("COMPLETED")) {
                    HBox row = new HBox(20);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setStyle("-fx-background-color: #1a1a2e; -fx-padding: 10; -fx-background-radius: 5;");
                    
                    boolean isAttacker = battle.getAttackerId() == currentUser.getId();
                    String opponentId = isAttacker ? String.valueOf(battle.getDefenderId()) : String.valueOf(battle.getAttackerId());
                    
                    Optional<User> opponentOpt = userRepo.findById(Integer.parseInt(opponentId));
                    String opponentName = opponentOpt.map(User::getUsername).orElse("Unknown");
                    
                    Label vsLabel = new Label("vs " + opponentName);
                    vsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
                    
                    String result;
                    String resultColor;
                    if (battle.getWinnerId() == null) {
                        result = "DRAW";
                        resultColor = "#888";
                    } else if (battle.getWinnerId() == currentUser.getId()) {
                        result = "WIN";
                        resultColor = "#4CAF50";
                    } else {
                        result = "LOSS";
                        resultColor = "#F44336";
                    }
                    
                    Label resultLabel = new Label(result);
                    resultLabel.setStyle("-fx-text-fill: " + resultColor + "; -fx-font-size: 14; -fx-font-weight: bold;");
                    
                    int pointsChange = isAttacker ? battle.getAttackerPointsChange() : battle.getDefenderPointsChange();
                    String pointsStr = pointsChange >= 0 ? "+" + pointsChange : String.valueOf(pointsChange);
                    Label pointsLabel = new Label(pointsStr);
                    pointsLabel.setStyle("-fx-text-fill: " + (pointsChange >= 0 ? "#4CAF50" : "#F44336") + "; -fx-font-size: 14;");
                    
                    row.getChildren().addAll(vsLabel, resultLabel, pointsLabel);
                    battleHistoryContainer.getChildren().add(row);
                }
            }
            
            if (battles.isEmpty()) {
                Label noBattles = new Label("No battles yet");
                noBattles.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                battleHistoryContainer.getChildren().add(noBattles);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            MainController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = (Stage) leaderboardContainer.getScene().getWindow();
            stage.setScene(SceneHelper.createStyledScene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
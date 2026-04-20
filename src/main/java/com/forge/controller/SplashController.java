package com.forge.controller;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {
    
    public SplashController() {
    }
    
    public void startAnimation(ProgressBar loadingBar, Label loadingLabel, Stage stage) {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(new Duration(0), e -> loadingBar.setProgress(0)));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), e -> {
            loadingBar.setProgress(0.3);
            loadingLabel.setText("Initializing...");
        }));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2000), e -> {
            loadingBar.setProgress(0.6);
            loadingLabel.setText("Loading resources...");
        }));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(2500), e -> {
            loadingBar.setProgress(0.8);
            loadingLabel.setText("Connecting to database...");
        }));
        timeline.getKeyFrames().add(new KeyFrame(new Duration(3000), e -> {
            loadingBar.setProgress(1.0);
            loadingLabel.setText("Ready!");
        }));
        timeline.setCycleCount(1);
        timeline.play();
        
        Timeline transition = new Timeline();
        transition.getKeyFrames().add(new KeyFrame(new Duration(3500), e -> navigateToLogin(stage)));
        transition.play();
    }
    
    private void navigateToLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            FadeTransition fadeOut = new FadeTransition(new Duration(1000), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setResizable(false);
                stage.setTitle("Forge - Gamified Productivity");
                
                FadeTransition fadeIn = new FadeTransition(new Duration(1000), stage.getScene().getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
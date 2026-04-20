package com.forge;

import com.forge.controller.SplashController;
import com.forge.util.DatabaseUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        DatabaseUtil.initializeDatabase();
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/splash.fxml"));
        Scene scene = new Scene(root);
        
        stage.setTitle("FORGE");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setWidth(1100);
        stage.setHeight(750);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.initStyle(StageStyle.UNDECORATED);
        
        makeDraggable(stage, root);
        
        stage.show();
        
        ProgressBar loadingBar = (ProgressBar) scene.lookup("#loadingBar");
        Label loadingLabel = (Label) scene.lookup("#loadingLabel");
        
        if (loadingBar != null && loadingLabel != null) {
            SplashController splash = new SplashController();
            splash.startAnimation(loadingBar, loadingLabel, stage);
        }
    }
    
    private void makeDraggable(Stage stage, Parent root) {
        final double[] dragOffsetX = {0};
        final double[] dragOffsetY = {0};
        
        root.setOnMousePressed((MouseEvent event) -> {
            dragOffsetX[0] = event.getSceneX();
            dragOffsetY[0] = event.getSceneY();
        });
        
        root.setOnMouseDragged((MouseEvent event) -> {
            stage.setX(event.getScreenX() - dragOffsetX[0]);
            stage.setY(event.getScreenY() - dragOffsetY[0]);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.forge.util;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneHelper {
    private static final String CSS = SceneHelper.class.getResource("/css/styles.css").toExternalForm();
    
    public static Scene createStyledScene(Parent root) {
        Scene scene = new Scene(root);
        scene.getStylesheets().add(CSS);
        return scene;
    }
    
    public static void addStylesheet(Scene scene) {
        if (!scene.getStylesheets().contains(CSS)) {
            scene.getStylesheets().add(CSS);
        }
    }
}
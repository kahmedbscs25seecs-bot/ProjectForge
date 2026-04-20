package com.forge.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class DragUtil {
    public static void makeDraggable(Stage stage, Parent root) {
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
    
    public static void makeDraggableFromNode(Node node, Parent root) {
        Stage stage = (Stage) node.getScene().getWindow();
        makeDraggable(stage, root);
    }
}
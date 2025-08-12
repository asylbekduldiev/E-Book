package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

public class Main extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main-view.fxml"));
        
        // Создаем сцену
        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.WHITE); // Убираем прозрачность
        
        // Подключаем CSS стили
        scene.getStylesheets().add(getClass().getResource("/css/modern.css").toExternalForm());
        
        // Настройка главного окна
        primaryStage.initStyle(StageStyle.UNDECORATED); // Без стандартной рамки
        primaryStage.setTitle("Modern E-Book Reader");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Добавляем возможность перетаскивания окна
        root.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        
        root.setOnMouseDragged((MouseEvent event) -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });
        
        // Красивая анимация появления
        root.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        primaryStage.show();
        fadeIn.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
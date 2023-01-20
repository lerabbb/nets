package nsu.lerabbb.snake;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import nsu.lerabbb.snake.View.GraphicMenuView;
import nsu.lerabbb.snake.View.IMenuView;

public class Main extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        IMenuView menuView = new GraphicMenuView(primaryStage);
        Platform.runLater(menuView::drawMenu);
    }
}

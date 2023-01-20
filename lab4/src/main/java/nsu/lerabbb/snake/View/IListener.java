package nsu.lerabbb.snake.View;

import nsu.lerabbb.snake.Model.Game;

public interface IListener {
    void showView();
    void updateGameView(Game game);
    void updateGameView(String message);
    void drawError(String errorText);
}

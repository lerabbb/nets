package nsu.lerabbb.snake.Model;

import nsu.lerabbb.snake.Net.protobuf.SnakesProto;
import nsu.lerabbb.snake.View.IListener;

public interface IGame {
    void putListener(IListener listener);
    void removeListeners(IListener listener);
    void setGameState(SnakesProto.GameState gameState);
    SnakesProto.GameConfig getProtoGameConfig();
    Snake getSnake(int index);
    void changeSnakeState(int index);
    void updateGameOnClient();
    void updateGameOnServer();
    void changeDir(int playerId, SnakesProto.Direction dir);
    void makeTurn();
    boolean addPlayer(GamePlayer player);
}

package nsu.lerabbb.snake.Net.Nodes;

import nsu.lerabbb.snake.Model.GamePlayer;
import nsu.lerabbb.snake.Model.enums.Direction;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

public interface INode {
    void init();
    void run();
    void end();
    void send(GamePlayer player, SnakesProto.GameMessage msg);
    void receive();
    SnakesProto.GameMessage getSteerMsg(Direction dir);
}

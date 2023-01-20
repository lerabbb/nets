package nsu.lerabbb.snake.Net.ConnectionHandlers;

import nsu.lerabbb.snake.Net.GameMessage;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;

public interface IUnicastHandler {
    void send(SnakesProto.GameMessage msg, InetAddress addr, int port) throws IOException;
    GameMessage receive();
    void close();
}

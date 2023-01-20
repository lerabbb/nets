package nsu.lerabbb.snake.Net;

import lombok.Getter;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.net.InetAddress;

@Getter
public class GameMessage {
    private final InetAddress addr;
    private final int port;
    private final SnakesProto.GameMessage message;

    public GameMessage(InetAddress addr, int port, SnakesProto.GameMessage msg){
        this.addr = addr;
        this.port = port;
        this.message = msg;
    }
}

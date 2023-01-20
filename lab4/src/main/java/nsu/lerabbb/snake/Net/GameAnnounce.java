package nsu.lerabbb.snake.Net;

import lombok.Getter;
import lombok.Setter;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.net.InetAddress;

@Getter
public class GameAnnounce {
    private final InetAddress address;
    private final int port;
    private SnakesProto.GameAnnouncement gameData; //пхд надо развернуть этот список и хранить конфиг каждой игры

    @Setter
    private Long timeConnect;  //время жизни объявления игры в меню

    public GameAnnounce(InetAddress address, int port){
        this.address = address;
        this.port = port;
    }
    public GameAnnounce(InetAddress address, int port, SnakesProto.GameAnnouncement message){
        this.address = address;
        this.port = port;
        this.gameData = message;
    }

    public GameAnnounce(InetAddress address, int port,SnakesProto.GameAnnouncement message, Long time) {
        this.address = address;
        this.port = port;
        this.gameData = message;
        this.timeConnect = time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        GameAnnounce game = (GameAnnounce) obj;
        return address == game.getAddress() && port == game.getPort();
    }

    @Override
    public String toString() {
        return String.format(
                "ip: %s\tport: %s" +
                        "Имя игры: %s\tsize: %sx%s" +
                        "Время хода: %s\tКоличество еды:%s",
                address, port,
                gameData.getGameName(),
                gameData.getConfig().getWidth(),
                gameData.getConfig().getHeight(),
                gameData.getConfig().getStateDelayMs(),
                gameData.getConfig().getFoodStatic()
        );
    }
}

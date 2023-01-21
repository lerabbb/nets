package nsu.lerabbb.snake.Model;

import lombok.Getter;
import lombok.Setter;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Getter
@Setter
public class GamePlayer {
    private String name;
    private int id;
    private String ip;
    private int port;
    private SnakesProto.NodeRole nodeRole;
    private SnakesProto.PlayerType playerType;
    private int score;


    public GamePlayer(String name, int id, SnakesProto.NodeRole role, String ip, int port){
        this.name = name;
        this.id = id;
        this.nodeRole = role;
        this.ip = ip;
        this.port = port;
        this.score = 0;
    }
    public GamePlayer(String name, int id, SnakesProto.NodeRole role, String ip, int port, int score){
        this.name = name;
        this.id = id;
        this.nodeRole = role;
        this.ip = ip;
        this.port = port;
        this.score = score;
    }
    public GamePlayer(SnakesProto.GamePlayer player){
        this.ip = player.getIpAddress();
        this.port = player.getPort();
        this.score = player.getScore();
        this.id = player.getId();
        this.nodeRole = player.getRole();
        this.name = player.getName();
        this.playerType = player.getType();
    }

    public InetAddress getIpAddr() throws UnknownHostException {
        return InetAddress.getByName(ip);
    }

    public SnakesProto.GamePlayer toProto(){
        return SnakesProto.GamePlayer.newBuilder()
                .setIpAddress(ip)
                .setPort(port)
                .setName(name)
                .setRole(nodeRole)
                .setId(id)
                .setScore(score)
                .build();
    }

    public void increaseScore(){
        this.score++;
    }
}

package nsu.lerabbb.snake.Net;

import lombok.Getter;
import nsu.lerabbb.snake.Logger;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GamesListener {
    private static final Long TIMEOUT = 2000L;

    @Getter
    private final List<GameAnnounce> gamesList;
    @Getter
    private boolean isGamesListUpd;

    public GamesListener(){
        this.gamesList = new ArrayList<>();
        this.isGamesListUpd = false;
    }

    public void updateGamesList(int port, InetAddress ip, SnakesProto.GameMessage.AnnouncementMsg message){
        boolean gameExistFlag = false;
        for(GameAnnounce game: gamesList){
            if(game.getAddress() == ip && game.getPort() == port){
                for(SnakesProto.GameAnnouncement announcement: message.getGamesList()){
                    isGamesListUpd = !announcement.getGameName().equals(game.getGameData().getGameName());
                }

                game.setTimeConnect(System.currentTimeMillis());
                gameExistFlag = true;
                break;
            }
        }

        if(!gameExistFlag){
            for(SnakesProto.GameAnnouncement gameData: message.getGamesList()){
                gamesList.add(new GameAnnounce(ip, port, gameData, System.currentTimeMillis()));
                String info = String.format("Game %s [%s:%s] started", gameData.getGameName(), ip, port);
                Logger.getInstance().info(info);
            }
            isGamesListUpd = true;
        }
    }

    public void checkGamesState(){
        for(GameAnnounce game: gamesList){
            long time = System.currentTimeMillis() - game.getTimeConnect();
            if(time > TIMEOUT){
                gamesList.remove(game);
                isGamesListUpd = true;
                String info = String.format("Game %s [%s:%s] ended", game.getGameData().getGameName(), game.getAddress().toString(), game.getPort());
                Logger.getInstance().info(info);
            }
        }
    }
    public List<String> getGamesListAsString(){
        List<String> list = new ArrayList<>();
        for(GameAnnounce announce: gamesList){
            list.add(announce.toString());
        }
        return list;
    }
}

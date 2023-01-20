package nsu.lerabbb.snake.Controller;

import javafx.event.ActionEvent;
import javafx.scene.input.KeyEvent;
import lombok.Setter;
import nsu.lerabbb.snake.Model.Game;
import nsu.lerabbb.snake.Model.enums.Direction;
import nsu.lerabbb.snake.Model.enums.SystemState;
import nsu.lerabbb.snake.Net.Nodes.INode;
import nsu.lerabbb.snake.Net.Nodes.MasterNode;
import nsu.lerabbb.snake.Net.Nodes.NormalNode;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;
import nsu.lerabbb.snake.View.GraphicGameView;
import nsu.lerabbb.snake.View.IListener;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameController implements IController, IGameController{

    @Setter
    private SystemState state;

    private final GraphicGameView gameView;
    private Game game;

    private final String playerName;
    private final SnakesProto.GameConfig config;

    private InetAddress servAddr;
    private int servPort;
    private INode node;
    private String gameName;

    private final ExecutorService threadPool;

    public GameController(IListener gameView, Game game, InetAddress servAddr, int servPort, SnakesProto.GameConfig config, String playerName){
        this.gameView = (GraphicGameView) gameView;
        this.game = game;
        this.servAddr = servAddr;
        this.servPort = servPort;
        this.config = config;
        this.playerName = playerName;

        this.state = SystemState.JOIN_GAME;
        this.threadPool = Executors.newCachedThreadPool();
        ((GraphicGameView) gameView).setController(this);
    }
    public GameController(IListener gameView, SnakesProto.GameConfig config, String playerName, String gameName){
        this.gameView = (GraphicGameView) gameView;
        this.config = config;
        this.state = SystemState.NEW_GAME;
        this.playerName = playerName;
        this.gameName = gameName;

        this.threadPool = Executors.newCachedThreadPool();
        ((GraphicGameView) gameView).setController(this);
    }

    @Override
    public void start() {
        if(state == SystemState.JOIN_GAME){
            game.putListener(gameView);
            node = new NormalNode(gameView, game, servAddr, servPort, config, playerName);
        }
        else if(state == SystemState.NEW_GAME){
            game = new Game(config);
            game.putListener(gameView);
            node = new MasterNode(game, config, playerName, gameName);
        }
        node.init();
        node.run();
    }

    @Override
    public void exitGameAction(ActionEvent actionEvent) {
        state = SystemState.MENU;
        node.end();
        game.removeListeners(gameView);
        threadPool.shutdown();
    }

    @Override
    public void keyPressed(KeyEvent keyEvent){
        switch(keyEvent.getCode()){
            case W, UP -> sendMove(Direction.UP);
            case A, LEFT -> sendMove(Direction.LEFT);
            case S, DOWN -> sendMove(Direction.DOWN);
            case D, RIGHT -> sendMove(Direction.RIGHT);
        }
    }

    private void sendMove(Direction dir){
        SnakesProto.GameMessage steerMsg = node.getSteerMsg(dir);
        threadPool.submit(() -> node.send(null, steerMsg));
    }
}

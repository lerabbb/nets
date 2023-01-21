package nsu.lerabbb.snake.Net.Nodes;

import javafx.application.Platform;
import nsu.lerabbb.snake.Logger;
import nsu.lerabbb.snake.Model.Game;
import nsu.lerabbb.snake.Model.GamePlayer;
import nsu.lerabbb.snake.Model.enums.Direction;
import nsu.lerabbb.snake.Model.enums.SystemState;
import nsu.lerabbb.snake.Net.ConnectionHandlers.UnicastHandler;
import nsu.lerabbb.snake.Net.GameMessage;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;
import nsu.lerabbb.snake.View.IListener;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NormalNode implements INode {
    private static final int TIMEOUT = 2000;

    private final IListener gameView;
    private final Game game;
    private final GamePlayer curPlayer;
    private SystemState state;
    private final SnakesProto.GameConfig gameConfig;

    private final InetAddress serverAddr;
    private final int serverPort;
    private UnicastHandler unicastHandler;

    private final ExecutorService threadPool;

    private Long lastRcvMessageTime;
    private Long lastSentMessageTime;
    private final Map<Long, SnakesProto.GameMessage> msgSentList;

    private long seqNum;

    public NormalNode(IListener gameView, Game game, InetAddress addr, int port, SnakesProto.GameConfig config, String playerName){
        this.gameView = gameView;
        this.game = game;
        this.serverAddr = addr;
        this.serverPort = port;
        this.gameConfig = config;

        this.curPlayer = new GamePlayer(playerName, -1, SnakesProto.NodeRole.NORMAL, "", 0);
        this.threadPool = Executors.newCachedThreadPool();
        this.state = SystemState.JOIN_GAME;
        this.msgSentList = new HashMap<>();
        this.lastRcvMessageTime = 0L;
        this.lastSentMessageTime = 0L;
        this.seqNum = 0;
    }

    @Override
    public void init(){
        try {
            unicastHandler = new UnicastHandler(new DatagramSocket(), TIMEOUT);
        } catch(SocketException e){
            Logger.getInstance().error(e.getMessage());
        }
    }

    @Override
    public void run() {
        threadPool.submit(()->{
           while(state.isJoinGameState()){
                receive();
           }
           unicastHandler.close();
        });
        curPlayer.setPort(unicastHandler.getSocket().getLocalPort());
        if(curPlayer.getNodeRole() == SnakesProto.NodeRole.NORMAL){
            threadPool.submit(() -> send(null, receiveJoinMsg(curPlayer.getName())));
        }
    }

    @Override
    public void end() {
        state = SystemState.MENU;
        if (unicastHandler != null) {
            threadPool.shutdown();
        }
    }

    @Override
    public void receive(){
        if(unicastHandler == null){
            return;
        }
        GameMessage rcvMsg = unicastHandler.receive();
        lastRcvMessageTime = System.currentTimeMillis();
        long seq = rcvMsg.getMessage().getMsgSeq();

        switch (rcvMsg.getMessage().getTypeCase()){
            case ACK -> {
                SnakesProto.GameMessage sntMsg = msgSentList.get(seq);
                if(sntMsg.getTypeCase() == SnakesProto.GameMessage.TypeCase.JOIN){
                    msgSentList.remove(seq);
                    curPlayer.setId(rcvMsg.getMessage().getReceiverId());
                }
                else if(sntMsg.getTypeCase() == SnakesProto.GameMessage.TypeCase.PING){
                    msgSentList.remove(seq);
                }
                else if(sntMsg.getTypeCase() == SnakesProto.GameMessage.TypeCase.STEER){
                    for(Long msgSeg: msgSentList.keySet()){
                        if(msgSentList.get(msgSeg).getTypeCase() == SnakesProto.GameMessage.TypeCase.STEER && msgSeg <= seq){
                            msgSentList.remove(msgSeg);
                        }
                    }
                }
            }
            case PING -> {
                SnakesProto.GameMessage ackMsg = receiveAckMsg(curPlayer.getId(), rcvMsg.getMessage().getMsgSeq());
                send(null, ackMsg);
            }
            case ERROR -> {
                msgSentList.remove(rcvMsg.getMessage().getMsgSeq());
                Platform.runLater(()-> gameView.drawError(rcvMsg.getMessage().getError().getErrorMessage()));
            }
            case STATE -> {
                SnakesProto.GameMessage.StateMsg stateMsg = rcvMsg.getMessage().getState();
                SnakesProto.GameMessage ackMsg = receiveAckMsg(rcvMsg.getMessage().getReceiverId(), rcvMsg.getMessage().getMsgSeq());
                send(null, ackMsg);
                if(game.getGameState() == null){
                    game.setGameState(stateMsg.getState());
                }
                if(stateMsg.getState().getStateOrder() > game.getStateOrder()){
                    game.updateGameOnClient();
                }
            }
            case ROLE_CHANGE -> {
                SnakesProto.GameMessage.RoleChangeMsg roleChangeMsg = rcvMsg.getMessage().getRoleChange();
                curPlayer.setNodeRole(roleChangeMsg.getReceiverRole());
                SnakesProto.GameMessage ackMsg = receiveAckMsg(rcvMsg.getMessage().getReceiverId(), rcvMsg.getMessage().getMsgSeq());
                send(null, ackMsg);
            }
        }
    }

    @Override
    public void send(GamePlayer player, SnakesProto.GameMessage msg) {
        if(unicastHandler==null){
            return;
        }

        try{
            lastSentMessageTime = System.currentTimeMillis();
            switch (msg.getTypeCase()) {
                case ACK -> {
                    unicastHandler.send(msg, serverAddr, serverPort);
                }
                case PING -> {
                    msgSentList.put(msg.getMsgSeq(), msg);
                    unicastHandler.send(msg, serverAddr, serverPort);
                }
                case JOIN -> {
                    msgSentList.put(msg.getMsgSeq(), msg);
                    while (msgSentList.containsKey(msg.getMsgSeq())) {
                        unicastHandler.send(msg, serverAddr, serverPort);
                        Thread.sleep(TIMEOUT);
                    }
                }
                case STEER -> {
                    msgSentList.put(msg.getMsgSeq(), msg);
                    while (msgSentList.containsKey(msg.getMsgSeq())) {
                        unicastHandler.send(msg, serverAddr, serverPort);
                        Thread.sleep(gameConfig.getStateDelayMs());
                    }
                }
            }
        }catch(InterruptedException ignored){
        }
        catch (IOException e){
            Logger.getInstance().error(e.getMessage());
        }
    }

    @Override
    public SnakesProto.GameMessage getSteerMsg(Direction dir) {
        SnakesProto.GameMessage.SteerMsg steerMsg = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(dir.toProto())
                .build();

        seqNum++;
        return SnakesProto.GameMessage.newBuilder()
                .setSteer(steerMsg)
                .setMsgSeq(seqNum-1)
                .setSenderId(curPlayer.getId())
                .build();
    }

    private SnakesProto.GameMessage receiveAckMsg(int id, long seq){
        SnakesProto.GameMessage.AckMsg msg = SnakesProto.GameMessage.AckMsg.newBuilder().build();
        return SnakesProto.GameMessage.newBuilder()
                .setAck(msg)
                .setMsgSeq(seq)
                .setReceiverId(id)
                .setSenderId(curPlayer.getId())
                .build();
    }

    private SnakesProto.GameMessage receiveJoinMsg(String name){
        SnakesProto.GameMessage.JoinMsg msg = SnakesProto.GameMessage.JoinMsg.newBuilder().setPlayerName(name).build();
        seqNum++;
        return SnakesProto.GameMessage.newBuilder()
                .setJoin(msg)
                .setMsgSeq(seqNum-1)
                .build();
    }
}

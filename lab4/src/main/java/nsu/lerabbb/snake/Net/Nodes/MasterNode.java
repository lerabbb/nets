package nsu.lerabbb.snake.Net.Nodes;

import nsu.lerabbb.snake.Logger;
import nsu.lerabbb.snake.Model.Coord;
import nsu.lerabbb.snake.Model.Game;
import nsu.lerabbb.snake.Model.GamePlayer;
import nsu.lerabbb.snake.Model.Snake;
import nsu.lerabbb.snake.Model.enums.Direction;
import nsu.lerabbb.snake.Model.enums.SystemState;
import nsu.lerabbb.snake.Net.ConnectionHandlers.MulticastSender;
import nsu.lerabbb.snake.Net.ConnectionHandlers.UnicastHandler;
import nsu.lerabbb.snake.Net.GameMessage;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MasterNode implements INode {
    private static final String MULTICAST_ADDR = "239.192.0.4";
    private static final int MULTICAST_PORT = 9192;
    private static final int SCHED_THREADS_NUM = 2;
    private static final int SCHED_DELAY = 1;
    private static final int SCHED_PERIOD = 1;
    private static final long NODE_TIMEOUT = 1000;

    private final Game game;
    private final SnakesProto.GameConfig config;
    private final GamePlayer masterPlayer;
    private GamePlayer deputyPlayer;

    private final ScheduledExecutorService schedThreadPool;
    private final ExecutorService threadPool;
    private UnicastHandler unicastHandler;

    private final String gameName;
    private int seqNum;
    private int playerId;
    private boolean failFlag;
    private SystemState state;

    private final Map<Long, SnakesProto.GameMessage> msgSentList;
    private final Map<Integer, GamePlayer> playersMap;
    private final Map<Integer, Long> lastSteerMap;
    private final Map<Integer, Long> lastMsgToPlayer;
    private final Map<Integer, Long> lastMsgFromPlayer;


    public MasterNode(Game game, SnakesProto.GameConfig config, String playerName, String gameName){
        this.game = game;
        this.config = config;
        this.gameName = gameName;

        this.masterPlayer = new GamePlayer(playerName, 0, SnakesProto.NodeRole.MASTER, "127.0.0.1", 0);
        this.game.addPlayer(masterPlayer);

        this.schedThreadPool = Executors.newScheduledThreadPool(SCHED_THREADS_NUM);
        this.threadPool = Executors.newCachedThreadPool();
        this.msgSentList = new HashMap<>();
        this.playersMap = new HashMap<>();
        this.lastSteerMap = new HashMap<>();
        this.lastMsgToPlayer = new HashMap<>();
        this.lastMsgFromPlayer = new HashMap<>();

        this.deputyPlayer = null;
        this.state = SystemState.NEW_GAME;
        this.failFlag = false;
        this.seqNum = 0;
        this.playerId = 0;
    }


    @Override
    public void init() {
        try{
            unicastHandler = new UnicastHandler(new DatagramSocket(), 500);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            masterPlayer.setPort(unicastHandler.getSocket().getLocalPort());
            MulticastSender multicastSender = new MulticastSender(MULTICAST_PORT, MULTICAST_ADDR, unicastHandler.getSocket(), this);
            schedThreadPool.scheduleAtFixedRate(multicastSender::run, SCHED_DELAY, SCHED_PERIOD, TimeUnit.SECONDS);

            threadPool.submit(this::receive);
            threadPool.submit(this::runGame);
            schedThreadPool.scheduleAtFixedRate(this::sendPing, config.getStateDelayMs(), config.getStateDelayMs(), TimeUnit.MILLISECONDS);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void end() {
        state = SystemState.EXIT;
        schedThreadPool.shutdownNow();
        threadPool.shutdown();
    }

    @Override
    public void send(GamePlayer player, SnakesProto.GameMessage msg) {
        if(unicastHandler == null || player == null){
            return;
        }
        if(msg.getTypeCase() != SnakesProto.GameMessage.TypeCase.ANNOUNCEMENT){
            lastMsgToPlayer.put(player.getId(), System.currentTimeMillis());
        }
        try {
            switch (msg.getTypeCase()) {
                case ACK, ERROR -> unicastHandler.send(msg, player.getIpAddr(), player.getPort());
                case ROLE_CHANGE -> sendMsg(msg, player.getIpAddr(), player.getPort());
                case STEER -> sendMsg(msg, masterPlayer.getIpAddr(), masterPlayer.getPort());
                case PING -> {
                    msgSentList.put(msg.getMsgSeq(), msg);
                    unicastHandler.send(msg, player.getIpAddr(), player.getPort());
                }
                case STATE -> {
                    if(masterPlayer.getId() != player.getId()){
                        sendMsg(msg, player.getIpAddr(), player.getPort());
                    }
                }
                default -> Logger.getInstance().error("Unknown message type: " + msg.getTypeCase());
            }
        } catch (InterruptedException ignored){
        } catch (IOException e){
            Logger.getInstance().error(e.getMessage());
        }
    }

    @Override
    public void receive() {
        while(state.isNewGameState()){
            if(unicastHandler == null){
                continue;
            }

            GameMessage msg = unicastHandler.receive();
            if(msg == null){
                continue;
            }

            long rcvSeqNum = msg.getMessage().getMsgSeq();
            if(msg.getMessage().getTypeCase() != SnakesProto.GameMessage.TypeCase.JOIN) {
                lastMsgFromPlayer.put(msg.getMessage().getSenderId(), System.currentTimeMillis());
            }

            switch (msg.getMessage().getTypeCase()){
                case ACK -> {
                    SnakesProto.GameMessage rcvMsg = msgSentList.get(rcvSeqNum);
                    switch (rcvMsg.getTypeCase()){
                        case PING, STATE, STEER, ROLE_CHANGE -> {
                            msgSentList.remove(rcvSeqNum);
                        }
                        default -> Logger.getInstance().error("Unknown message type: " + msg.getMessage().getTypeCase());
                    }
                }
                case STEER -> {
                    GamePlayer player = getPlayer(msg);
                    Logger.getInstance().info("Player " + player.getName() + " sent steer message");
                    handleSteer(msg.getMessage());
                    SnakesProto.GameMessage ackMsg = createAckMsg(player.getId(), msg.getMessage().getMsgSeq());
                    send(player, ackMsg);
                }
                case JOIN -> {
                    Logger.getInstance().info("New player connected");
                    handleJoin(msg.getMessage(), msg.getAddr().toString(), msg.getPort());
                }
                case PING -> {
                    GamePlayer player = getPlayer(msg);
                    Logger.getInstance().info("Player " + player.getName() + " pinged");
                    SnakesProto.GameMessage ackMsg = createAckMsg(player.getId(), msg.getMessage().getMsgSeq());
                    send(player, ackMsg);
                }
                case ROLE_CHANGE -> {

                }
                default -> Logger.getInstance().error("Unknown message type: " + msg.getMessage().getTypeCase());
            }
        }
    }

    private void runGame(){
        try{
            while (state == SystemState.NEW_GAME) {
                game.makeTurn();
                checkConnection();
                threadPool.submit(() -> {
                    for (GamePlayer player : playersMap.values()) {
                        send(player, createGameStateMsg(player.getId()));
                    }
                });
                Thread.sleep(config.getStateDelayMs());
            }
        }catch (InterruptedException ignored){
        }
    }
    private void sendPing(){
        for(int id: lastMsgToPlayer.keySet()){
            if(id == masterPlayer.getId()){
                continue;
            }
            long time = System.currentTimeMillis() - lastMsgToPlayer.get(id);
            if(time > config.getStateDelayMs()){
                send(playersMap.get(id), createPingMsg(id));
            }
        }
    }
    private void handleSteer(SnakesProto.GameMessage message){
        int senderId = message.getSenderId();
        if(playersMap.get(senderId).getNodeRole() == SnakesProto.NodeRole.VIEWER){
            return;
        }
        if(lastSteerMap.get(senderId) == null  || lastSteerMap.get(senderId) < message.getMsgSeq()){
            game.changeDir(senderId, message.getSteer().getDirection());
            lastSteerMap.put(senderId, message.getMsgSeq());
        }
    }
    private void handleJoin(SnakesProto.GameMessage message, String ip, int port){
        GamePlayer player = createPlayer(message.getJoin().getPlayerName(), ip, port);

        if(failFlag){
            send(player, createErrorMsg(message.getMsgSeq(), "Ошибка: нет доступных мест для нового игрока. Попробуйте позже"));
            Logger.getInstance().error("No place for new player " + player.getName());
            return;
        }

        send(player, createAckMsg(player.getId(), message.getMsgSeq()));
        game.updateGameOnServer();
        if(deputyPlayer == null){
            threadPool.submit(() ->{
                send(player, createRoleChangeMsg(player.getId(), SnakesProto.NodeRole.DEPUTY));
            });
            deputyPlayer = player;
        }
    }

    public SnakesProto.GameMessage createAnnouncementMsg(){
        seqNum++;

        List<SnakesProto.GamePlayer> playersList = new ArrayList<>();
        for(GamePlayer player: game.getPlayersList()){
            playersList.add(player.toProto());
        }
        SnakesProto.GamePlayers gamePlayers = SnakesProto.GamePlayers.newBuilder()
                .addAllPlayers(playersList)
                .build();

        SnakesProto.GameAnnouncement announcement = SnakesProto.GameAnnouncement.newBuilder()
                .setConfig(config)
                .setPlayers(gamePlayers)
                .setGameName(gameName)
                .build();
        SnakesProto.GameMessage.AnnouncementMsg msg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder()
                .addGames(announcement)
                .build();

        return SnakesProto.GameMessage.newBuilder()
                .setAnnouncement(msg)
                .build();
    }
    private SnakesProto.GameMessage createPingMsg(int id){
        SnakesProto.GameMessage.PingMsg msg = SnakesProto.GameMessage.PingMsg.newBuilder().build();
        seqNum++;
        return SnakesProto.GameMessage.newBuilder()
                .setReceiverId(id)
                .setSenderId(masterPlayer.getId())
                .setMsgSeq(seqNum-1)
                .setPing(msg)
                .build();
    }
    private SnakesProto.GameMessage createAckMsg(int receiverId, long seq){
        return SnakesProto.GameMessage.newBuilder()
                .setAck(
                        SnakesProto.GameMessage.AckMsg.newBuilder().build()
                )
                .setSenderId(masterPlayer.getId())
                .setReceiverId(receiverId)
                .setMsgSeq(seq)
                .build();
    }
    private SnakesProto.GameMessage createErrorMsg(long seq, String text){
        SnakesProto.GameMessage.ErrorMsg msg = SnakesProto.GameMessage.ErrorMsg.newBuilder()
                .setErrorMessage(text)
                .build();
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(seq)
                .setSenderId(masterPlayer.getId())
                .setError(msg)
                .build();
    }
    private SnakesProto.GameMessage createRoleChangeMsg(int receiverId, SnakesProto.NodeRole role){
        SnakesProto.GameMessage.RoleChangeMsg msg = SnakesProto.GameMessage.RoleChangeMsg.newBuilder()
                .setReceiverRole(role)
                .setSenderRole(SnakesProto.NodeRole.MASTER)
                .build();
        seqNum++;
        return SnakesProto.GameMessage.newBuilder()
                .setReceiverId(receiverId)
                .setSenderId(masterPlayer.getId())
                .setMsgSeq(seqNum-1)
                .setRoleChange(msg)
                .build();
    }
    private SnakesProto.GameMessage createGameStateMsg(int id){
        List<SnakesProto.GameState.Snake> protoSnakesList = new ArrayList<>();
        List<SnakesProto.GameState.Coord> protoFoodList = new ArrayList<>();
        SnakesProto.GamePlayers.Builder gamePlayersBuilder = SnakesProto.GamePlayers.newBuilder();

        for(Snake snake: game.getSnakesList()){
            protoSnakesList.add(snake.toProto());
        }
        for(Coord food: game.getFoodList()){
            protoFoodList.add(food.toProto());
        }
        for(GamePlayer player: playersMap.values()){
            gamePlayersBuilder.addPlayers(player.toProto());
            if(game.getSnakesList().get(player.getId()) == null
                    || player.getId() == masterPlayer.getId()
                    || player.getNodeRole() == SnakesProto.NodeRole.VIEWER)
            {
                continue;
            }
            player.setNodeRole(SnakesProto.NodeRole.VIEWER);
            threadPool.submit(()-> {
                send(player, createRoleChangeMsg(player.getId(), SnakesProto.NodeRole.VIEWER));
            });
        }

        SnakesProto.GamePlayers gamePlayers = gamePlayersBuilder.build();
        SnakesProto.GameState gameState = SnakesProto.GameState.newBuilder()
                .setPlayers(gamePlayers)
                .setStateOrder(game.getStateOrder())
                .addAllFoods(protoFoodList)
                .addAllSnakes(protoSnakesList)
                .build();

        SnakesProto.GameMessage.StateMsg stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder()
                .setState(gameState)
                .build();

        seqNum++;
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(seqNum-1)
                .setSenderId(masterPlayer.getId())
                .setReceiverId(id)
                .setState(stateMsg)
                .build();
    }

    @Override
    public SnakesProto.GameMessage getSteerMsg(Direction dir) {
        SnakesProto.GameMessage.SteerMsg msg = SnakesProto.GameMessage.SteerMsg.newBuilder()
                .setDirection(dir.toProto())
                .build();
        seqNum++;
        return SnakesProto.GameMessage.newBuilder()
                .setSteer(msg)
                .setSenderId(masterPlayer.getId())
                .setMsgSeq(seqNum-1)
                .build();
    }


    private void checkConnection(){
        for(int id: lastMsgFromPlayer.keySet()){
            if(id == masterPlayer.getId()){
                continue;
            }
            long time = System.currentTimeMillis() - lastMsgFromPlayer.get(id);
            if(time <= NODE_TIMEOUT){
                continue;
            }
            Logger.getInstance().info("Player "+ playersMap.get(id).getName() + " disconnected by server");
            playersMap.remove(id);
            if(game.getSnake(id) != null){
                game.changeSnakeState(id);
            }
            lastMsgFromPlayer.remove(id);
            lastMsgToPlayer.remove(id);
            lastSteerMap.remove(id);

            for(long msg: msgSentList.keySet()){
                if(msgSentList.get(msg).getReceiverId() == id){
                    msgSentList.remove(msg);
                }
            }
            if(playersMap.get(id) != null && playersMap.get(id).getNodeRole() == SnakesProto.NodeRole.DEPUTY){
                findNewDeputy();
            }
        }
    }

    private void sendMsg(SnakesProto.GameMessage msg, InetAddress ip, int port) throws IOException, InterruptedException {
        msgSentList.put(msg.getMsgSeq(), msg);
        while (msgSentList.containsKey(msg.getMsgSeq())) {
            unicastHandler.send(msg, ip, port);
            Thread.sleep(config.getStateDelayMs());
        }
    }

    private void findNewDeputy(){
        for(GamePlayer player: playersMap.values()){
            if(player.getId() != masterPlayer.getId()){
                threadPool.submit(() -> send(player, createRoleChangeMsg(player.getId(), SnakesProto.NodeRole.DEPUTY)));
                deputyPlayer = player;
                Logger.getInstance().info("New deputy: " + player.getName());
                break;
            }
        }
    }

    private GamePlayer getPlayer(GameMessage message){
        SnakesProto.GameMessage gameMessage = message.getMessage();
        return playersMap.get(gameMessage.getSenderId());
    }

    private GamePlayer createPlayer(String name, String ip, int port){
        GamePlayer player = new GamePlayer(name, playerId, SnakesProto.NodeRole.NORMAL, ip, port);
        playerId++;
        failFlag = game.addPlayer(player);
        return player;
    }
}

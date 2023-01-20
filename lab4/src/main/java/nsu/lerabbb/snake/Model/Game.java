package nsu.lerabbb.snake.Model;

import javafx.application.Platform;
import lombok.Getter;
import nsu.lerabbb.snake.Model.enums.CellType;
import nsu.lerabbb.snake.Model.enums.Direction;
import nsu.lerabbb.snake.Model.enums.SnakeState;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;
import nsu.lerabbb.snake.View.IListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class Game implements IGame{
    private final Field field;
    private final List<Snake> snakesList;
    private final List<GamePlayer> playersList;
    private final List<Coord> foodList;
    private final CopyOnWriteArrayList<IListener> listenersList;

    private final int stateDelayMs;
    private int foodNum;
    private int stateOrder;

    private SnakesProto.GameState gameState;

    public Game(SnakesProto.GameConfig config){
        this.field = new Field(config.getWidth(), config.getHeight());
        this.snakesList = new ArrayList<>();
        this.playersList = new ArrayList<>();
        this.foodList = new ArrayList<>();
        this.foodNum = config.getFoodStatic();
        this.stateDelayMs = config.getStateDelayMs();

        this.listenersList = new CopyOnWriteArrayList<>();
        this.stateOrder = 0;
    }

    @Override
    public void setGameState(SnakesProto.GameState gameState){
        this.gameState = gameState;
        this.foodNum = gameState.getFoodsCount() + gameState.getPlayers().getPlayersCount();
    }
    @Override
    public SnakesProto.GameConfig getProtoGameConfig(){
        return SnakesProto.GameConfig.newBuilder()
                .setHeight(field.getFieldHeight())
                .setWidth(field.getFieldWidth())
                .setFoodStatic(foodNum)
                .setStateDelayMs(stateDelayMs)
                .build();
    }
    @Override
    public Snake getSnake(int index){
        return snakesList.get(index);
    }
    @Override
    public void changeSnakeState(int index){
        Snake snake = snakesList.get(index);
        snake.setState(SnakeState.ZOMBIE);
        snakesList.set(index, snake);
    }

    //actions on client
    @Override
    public void updateGameOnClient(){
        field.clear();
        stateOrder = gameState.getStateOrder();

        snakesList.clear();
        if(!gameState.getSnakesList().isEmpty()){
            for(SnakesProto.GameState.Snake snake: gameState.getSnakesList()){
                Snake mySnake = new Snake(snake.getPoints(0), snake.getHeadDirection(), snake.getPlayerId());
                mySnake.fromProto(snake);
                snakesList.add(mySnake.getPlayerId(), mySnake);
            }
        }

        playersList.clear();
        for(SnakesProto.GamePlayer player: gameState.getPlayers().getPlayersList()){
            playersList.add(player.getId(), new GamePlayer(player));
        }

        foodList.clear();
        for(SnakesProto.GameState.Coord food: gameState.getFoodsList()){
            foodList.add(new Coord(food));
        }

        updateSnakes();
        updateFood();
        pingListeners();
    }
    private void updateSnakes(){
        for(Snake snake: snakesList){
            if(snake.isAlive()){
                for(Coord point: snake.getPoints()){
                    field.setSnakePart(point);
                }
            }
            else{
                snakesList.remove(snake);
            }
        }
    }
    private void updateFood(){
        for(Coord food: foodList){
            field.setFood(food);
        }
    }
    private void pingListeners(){
        for(IListener listener: listenersList){
            if(listener == null){
                continue;
            }

            StringBuilder message = new StringBuilder();
            for(GamePlayer player: playersList){
                if(snakesList.get(player.getId()) != null){
                    message.append(String.format("[Player%s %s] failed\n", player.getId(), player.getName()));
                }
                else{
                    message.append(String.format("[Player%s %s] score: %s\n", player.getId(), player.getName(), player.getScore()));
                }
            }

            Platform.runLater(() -> {
                listener.updateGameView(this);
                listener.updateGameView(message.toString());
            });
        }
    }


    //actions on server
    @Override
    public void updateGameOnServer(){
        updateSnakes();
        updateFood();
        pingListeners();
    }

    @Override
    public void changeDir(int playerId, SnakesProto.Direction dir){
        Snake snake = snakesList.get(playerId);
        snake.setNewDirection(Direction.fromProto(dir));
        snakesList.set(playerId, snake);
    }

    @Override
    public void makeTurn(){
        spawnFood();

        if(snakesList.isEmpty()){
            return;
        }
        for(Snake snake: snakesList){
            moveSnake(snake);
        }
        checkCrash();

        updateGameOnServer();
        stateOrder++;
    }

    @Override
    public boolean addPlayer(GamePlayer player){
        Snake snake = spawnSnake(player.getId());
        if(snake == null){
            return false;
        }
        snakesList.add(player.getId(), snake);
        playersList.add(player.getId(), player);
        return true;
    }

    private void moveSnake(Snake snake){
        Direction newDir = snake.getNewDirection();
        if (newDir == snake.getOppositeDir()) {
            return;
        }
        snake.move(newDir);
        Coord head = snake.getHead();

        if(field.isFoodHere(head)){
            return;
        }
        Coord tail = snake.getTail();
        field.clearCell(tail);
        snake.removeTail();

        field.setSnakePart(head);
    }

    private void checkCrash(){
        for(Snake snake: snakesList){
            CellType cell = field.getCellType(snake.getHead());
            if(field.isFoodHere(snake.getHead())){
                playersList.get(snake.getPlayerId()).increaseScore();
                foodList.removeIf(food -> food.equals(snake.getHead()));
                spawnFood();
            }
            if(snake.doesCrashExist(snake.getHead())){
                killSnake(snake);
            }
            for(Snake alienSnake: snakesList){
                if (snake.getPlayerId() == alienSnake.getPlayerId()) {
                    continue;
                }
                if(isHeadCrash(snake, alienSnake)){
                    playersList.get(snake.getPlayerId()).increaseScore();
                    playersList.get(alienSnake.getPlayerId()).increaseScore();
                    killSnake(snake);
                    killSnake(alienSnake);
                    continue;
                }
                if(alienSnake.doesCrashExist(snake.getHead())){
                    playersList.get(alienSnake.getPlayerId()).increaseScore();
                    killSnake(snake);
                }
            }
        }
    }

    private boolean isHeadCrash(Snake snake1, Snake snake2){
        return snake1.getHead().equals(snake2.getHead());
    }

    private void killSnake(Snake snake){
        snake.setState(SnakeState.ZOMBIE);
        for(Coord point: snake.getPoints()){
            double p = Math.random();
            if(p >= 0.5){
                field.setFood(point);
                foodList.add(point);
            }else{
                field.clearCell(point);
            }
        }
    }
    private Snake spawnSnake(int playerId){
        Coord spawnCoord = field.getFreeSquareCenter();
        if(spawnCoord.getX() == -1 && spawnCoord.getY() == -1){
            return null;
        }
        Direction startDirection = getRandomDirection();
        Snake snake = new Snake(spawnCoord, startDirection, field.getFieldWidth(), field.getFieldHeight(), playerId);
        for(Coord point: snake.getPoints()){
            field.setSnakePart(point);
        }
        return snake;
    }

    private void spawnFood(){
        int maxFoodNum = foodNum + aliveSnakesNum();
        while(foodList.size() != maxFoodNum){
            Coord spawnCoord = getRandomCoord();
            while(!field.isEmptyCell(spawnCoord)){
                spawnCoord = getRandomCoord();
            }
            field.setFood(spawnCoord);
            foodList.add(spawnCoord);
        }
    }

    @Override
    public void putListener(IListener listener){
        if(listenersList.contains(listener)){
            throw new IllegalArgumentException("Listener already exists");
        }
        listenersList.add(listener);
    }
    @Override
    public void removeListeners(IListener listener){
        if(!listenersList.contains(listener)){
            throw new IllegalArgumentException("Impossible to remove listener. It doesn't exist");
        }
        listenersList.remove(listener);
    }

    private Direction getRandomDirection(){
        int min = Direction.UP.getVal();
        int max = Direction.RIGHT.getVal();
        int dir = (int)(Math.random()*((max-min)+1))+min;
        return Direction.getDirByValue(dir);
    }
    private Coord getRandomCoord(){
        int x = (int)(Math.random() * field.getFieldWidth());
        int y = (int)(Math.random() * field.getFieldHeight());
        return new Coord(x,y);
    }
    private int aliveSnakesNum(){
        int count = 0;
        for(Snake snake: snakesList){
            if(snake.isAlive()){
                count++;
            }
        }
        return count;
    }
}
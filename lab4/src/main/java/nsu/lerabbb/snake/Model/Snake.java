package nsu.lerabbb.snake.Model;

import lombok.Getter;
import lombok.Setter;
import nsu.lerabbb.snake.Model.enums.Direction;
import nsu.lerabbb.snake.Model.enums.SnakeState;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public class Snake {

    @Setter
    private SnakeState state;
    private int playerId;
    private LinkedList<Coord> points;
    private Direction headDirection;
    @Setter
    private Direction newDirection;
    private int fieldWidth;
    private int fieldHeight;

    public Snake(Coord headCoord, Direction headDirection){
        this.playerId = 0;
        this.state = SnakeState.ALIVE;
        this.headDirection = headDirection;
        this.newDirection = headDirection;

        Coord tailCoord = new Coord(headCoord.getX() - headDirection.getX(), headCoord.getY() - headDirection.getY());
        this.points = new LinkedList<>();
        this.points.add(headCoord);
        this.points.add(tailCoord);
    }

    public Snake(Coord headCoord, Direction headDirection, int playerId){
        this.playerId = playerId;
        this.state = SnakeState.ALIVE;
        this.headDirection = headDirection;

        Coord tailCoord = new Coord(headCoord.getX() - headDirection.getX(), headCoord.getY() - headDirection.getY());
        this.points = new LinkedList<>();
        this.points.add(headCoord);
        this.points.add(tailCoord);
    }

    public Snake(SnakesProto.GameState.Coord headCoord, SnakesProto.Direction dir, int playerId){
        this.playerId = playerId;
        this.state = SnakeState.ALIVE;
        this.headDirection = Direction.fromProto(dir);

        Coord tailCoord = new Coord(headCoord.getX() - headDirection.getX(), headCoord.getY() - headDirection.getY());
        this.points = new LinkedList<>();
        this.points.add(new Coord(headCoord));
        this.points.add(tailCoord);
    }


    public Snake(Coord headCoord, Direction headDirection, int fieldWidth, int fieldHeight, int playerId){
        this.playerId = playerId;
        this.state = SnakeState.ALIVE;
        this.headDirection = headDirection;

        Coord tailCoord = new Coord(headCoord.getX() - headDirection.getX(), headCoord.getY() - headDirection.getY());
        this.points = new LinkedList<>();
        this.points.add(headCoord);
        this.points.add(tailCoord);
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
    }

    public boolean isAlive(){
        return state == SnakeState.ALIVE;
    }
    public boolean isZombie(){
        return state == SnakeState.ZOMBIE;
    }
    public Coord getHead(){
        return points.getFirst();
    }
    public Coord getTail(){
        return points.getLast();
    }
    public Direction getOppositeDir(){ return headDirection.getOppositeDir(); }
    public void removeTail(){
        points.removeLast();
    }

    public void move(Direction dir){
        headDirection = dir;

        Coord newHead = new Coord(points.getFirst().getX() + headDirection.getX(),
                                    points.getFirst().getY() + headDirection.getY());
        points.addFirst(newHead);

        for(Coord point: points){
            point.setX(mod(point.getX(), fieldWidth));
            point.setY(mod(point.getY(), fieldHeight));
        }
    }
    public boolean doesCrashExist(Coord coord){
       for(int i=1; i<points.size(); i++){
           if(points.get(i).equals(coord)){
               return true;
           }
       }
       return false;
    }

    public SnakesProto.GameState.Snake toProto(){
        List<SnakesProto.GameState.Coord> protoPoints = new ArrayList<>();
        for(Coord point: points){
            protoPoints.add(point.toProto());
        }
        return SnakesProto.GameState.Snake.newBuilder()
                .setState(state.toProto())
                .setPlayerId(playerId)
                .setHeadDirection(headDirection.toProto())
                .addAllPoints(protoPoints)
                .build();
    }
    public void fromProto(SnakesProto.GameState.Snake protoSnake){
        state = state.fromProto(protoSnake.getState());
        playerId = protoSnake.getPlayerId();
        headDirection = Direction.fromProto(protoSnake.getHeadDirection());
        points.clear();
        List<SnakesProto.GameState.Coord> protoPoints = protoSnake.getPointsList();
        for(SnakesProto.GameState.Coord protoPoint: protoPoints){
            points.addLast(new Coord(protoPoint));
        }
    }

    private int mod(int x, int n){
        int r = x % n;
        if (r < 0){
            r += n;
        }
        return r;
    }
}







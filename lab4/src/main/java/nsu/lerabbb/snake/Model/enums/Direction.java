package nsu.lerabbb.snake.Model.enums;

import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

public enum Direction{
    UP, DOWN, LEFT, RIGHT;

    public int getY(){
        return switch(this){
            case UP -> -1;
            case DOWN -> 1;
            default -> 0;
        };
    }
    public int getX(){
        return switch(this){
            case LEFT -> -1;
            case RIGHT -> 1;
            default ->0;
        };
    }
    public int getVal(){
        return switch(this){
            case UP -> 0;
            case DOWN -> 1;
            case LEFT -> 2;
            default -> 3;
        };
    }
    public static Direction getDirByValue(int dir){
        return switch(dir){
            case 0 -> Direction.UP;
            case 1 -> Direction.DOWN;
            case 2 -> Direction.LEFT;
            default -> Direction.RIGHT;
        };
    }
    public Direction getOppositeDir(){
        return switch(this){
            case RIGHT -> LEFT;
            case LEFT -> RIGHT;
            case UP -> DOWN;
            default-> UP;
        };
    }
    public SnakesProto.Direction toProto(){
        return switch (this){
            case RIGHT -> SnakesProto.Direction.RIGHT;
            case LEFT -> SnakesProto.Direction.LEFT;
            case UP -> SnakesProto.Direction.UP;
            case DOWN -> SnakesProto.Direction.DOWN;
        };
    }
    public static Direction fromProto(SnakesProto.Direction dir){
        if(dir == SnakesProto.Direction.RIGHT){
            return RIGHT;
        }
        if(dir == SnakesProto.Direction.LEFT){
            return LEFT;
        }
        if(dir == SnakesProto.Direction.UP) {
            return UP;
        }
        return DOWN;
    }
}

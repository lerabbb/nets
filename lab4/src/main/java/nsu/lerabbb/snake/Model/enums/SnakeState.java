package nsu.lerabbb.snake.Model.enums;

import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

public enum SnakeState{
    ALIVE, ZOMBIE;

    public SnakesProto.GameState.Snake.SnakeState toProto(){
        return switch(this){
            case ALIVE -> SnakesProto.GameState.Snake.SnakeState.ALIVE;
            case ZOMBIE -> SnakesProto.GameState.Snake.SnakeState.ZOMBIE;
        };
    }
    public SnakeState fromProto(SnakesProto.GameState.Snake.SnakeState state){
        if(state == SnakesProto.GameState.Snake.SnakeState.ALIVE){
            return ALIVE;
        }
        return ZOMBIE;
    }
}

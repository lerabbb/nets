package nsu.lerabbb.snake.Model.enums;

public enum SystemState {
    MENU,
    NEW_GAME,
    JOIN_GAME,
    EXIT;

    public boolean isMenuState(){
        return this == MENU;
    }
    public boolean isNewGameState(){
        return this == NEW_GAME;
    }
    public boolean isJoinGameState(){
        return this == JOIN_GAME;
    }
    public boolean isExitState(){
        return this == EXIT;
    }
}

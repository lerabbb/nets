package nsu.lerabbb.snake.Model;

import lombok.Getter;
import lombok.Setter;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;

@Getter
@Setter
public class Coord {
    private int y;
    private int x;

    public Coord(int x, int y){
        this.x = x;
        this.y = y;
    }
    public Coord(SnakesProto.GameState.Coord coord){
        this.x = coord.getX();
        this.y = coord.getY();
    }

    public SnakesProto.GameState.Coord toProto(){
        return SnakesProto.GameState.Coord.newBuilder()
                .setX(x)
                .setY(y)
                .build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Coord coord = (Coord) obj;
        return x == coord.getX() && y == coord.getY();
    }
}

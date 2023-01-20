package nsu.lerabbb.snake.Model;

import lombok.Getter;
import nsu.lerabbb.snake.Model.enums.CellType;


public class Field {
    private static final int SQUARE_SIZE = 5;
    private static final int SQUARE_CENTER = (SQUARE_SIZE - 1) / 2;

    @Getter
    private final int fieldWidth;
    @Getter
    private final int fieldHeight;
    @Getter
    private final CellType[][] field;

    public Field(int width, int height){
        this.fieldWidth=width;
        this.fieldHeight= height;

        this.field = new CellType[fieldHeight][fieldWidth];
        this.clear();
    }

    public void setFood(Coord coord){
        field[coord.getY()][coord.getX()] = CellType.FOOD_CELL;
    }
    public void setSnakePart(Coord coord){
        field[coord.getY()][coord.getX()] = CellType.SNAKE_CELL;
    }
    public void clearCell(Coord coord){
        field[coord.getY()][coord.getX()] = CellType.EMPTY_CELL;
    }

    public boolean isSnakeHere(Coord coord){
        return field[coord.getY()][coord.getX()] == CellType.SNAKE_CELL;
    }
    public boolean isFoodHere(Coord coord){
        return field[coord.getY()][coord.getX()] == CellType.FOOD_CELL;
    }
    public boolean isEmptyCell(Coord coord){
        return field[coord.getY()][coord.getX()] == CellType.EMPTY_CELL;
    }

    public void clear(){
        for(int i = 0; i < fieldHeight; i++){
            for(int j=0; j < fieldWidth; j++){
                this.field[i][j] = CellType.EMPTY_CELL;
            }
        }
    }
    public CellType getCellType(Coord coord){
        return field[coord.getY()][coord.getX()];
    }
    public Coord getFreeSquareCenter(){
        Coord squareStart = new Coord(0,0);
        Coord squareEnd = new Coord(0,0);

        for(int y=0; y<fieldHeight-SQUARE_SIZE; y++){
            for(int x=0; x<fieldWidth-SQUARE_SIZE; x++) {
                squareStart.setX(x);
                squareStart.setY(y);
                squareEnd.setX(x + SQUARE_SIZE);
                squareEnd.setY(y + SQUARE_SIZE);

                if (isSquareEmpty(squareStart, squareEnd)) {
                    return new Coord(squareStart.getX() + SQUARE_CENTER, squareStart.getY() + SQUARE_CENTER);
                }
            }
        }
        return new Coord(-1, -1);
    }

    private boolean isSquareEmpty(Coord start, Coord end){
        for(int y = start.getY(); y<end.getY(); y++){
            for(int x = start.getX(); x<end.getX(); x++){
                if(!isEmptyCell(new Coord(x,y))){
                    return false;
                }
            }
        }
        return true;
    }
}

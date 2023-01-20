package nsu.lerabbb.snake.View;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import lombok.Setter;
import nsu.lerabbb.snake.Controller.ErrorController;
import nsu.lerabbb.snake.Controller.IGameController;
import nsu.lerabbb.snake.Logger;
import nsu.lerabbb.snake.Model.Game;
import nsu.lerabbb.snake.Model.enums.CellType;
import nsu.lerabbb.snake.Net.GameAnnounce;

import java.io.IOException;

public class GraphicGameView implements IListener {
    private static final double CELL_SIZE = 20;

    private final double fieldWidth;
    private final double fieldHeight;

    private final Stage stage;

    private GridPane fieldPane;
    private SplitPane splitPane;
    private AnchorPane fieldRootPane;
    private AnchorPane infoRootPane;
    private VBox infoVbox;
    private Label scoresLabel;

    @Setter
    private IGameController controller;

    public GraphicGameView(Stage stage, int height, int width){
        this.stage = stage;
        this.fieldHeight = height;
        this.fieldWidth = width;
        this.controller = null;

        initGraphicNodes();
    }

    public GraphicGameView(Stage stage, GameAnnounce announce){
        this.stage = stage;
        this.fieldHeight = announce.getGameData().getConfig().getHeight();
        this.fieldWidth = announce.getGameData().getConfig().getWidth();
        this.controller = null;

        initGraphicNodes();
    }

    private void initGraphicNodes(){
        fieldRootPane = new AnchorPane();
        fieldRootPane.setStyle("-fx-background-color: #70ba3c;");
        fieldRootPane.setPrefHeight(fieldHeight * CELL_SIZE + 80);
        fieldRootPane.setPrefWidth(fieldWidth * CELL_SIZE + 80);

        Button exitGameBtn = new Button("Выйти из игры");
        exitGameBtn.setOnAction(controller::exitGameAction);
        Label scoresTitleLabel = new Label("Игроки:");
        scoresLabel = new Label();
        infoVbox = new VBox(exitGameBtn, scoresTitleLabel, scoresTitleLabel);
        infoVbox.setAlignment(Pos.CENTER);
        infoVbox.setSpacing(20);

        infoRootPane = new AnchorPane(infoVbox);
        infoRootPane.setStyle("-fx-background-color: #70ba3c;");
        infoRootPane.setPrefHeight(fieldHeight * CELL_SIZE + 80);

        splitPane = new SplitPane(fieldRootPane, infoRootPane);

        Scene scene = new Scene(splitPane);
        scene.setOnKeyPressed(controller::keyPressed);
        stage.setScene(scene);
    }

    @Override
    public void showView(){
        stage.show();
    }
    @Override
    public void updateGameView(Game game) {
        drawField(game.getField().getField());
    }
    @Override
    public void updateGameView(String message) {
        scoresLabel.setText(message);
    }

    @Override
    public void drawError(String errorText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/error.fxml"));
            Parent root = loader.load();
            ErrorController controller = loader.getController();
            controller.setErrorText(errorText);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            Logger.getInstance().error(e.getMessage());
        }
    }

    private void drawField(CellType[][] field) {
        fieldRootPane.getChildren().remove(fieldPane);

        fieldPane = new GridPane();
        fieldPane.setStyle("-fx-background-color: white;\n-fx-grid-lines-visible: false;");

        for(int i = 0; i<fieldHeight; i++) {
            for (int j = 0; j < fieldWidth; j++) {
                CellType cellType = field[i][j];
                fieldPane.add(getRectangle(cellType), j, i);
            }
        }
    }

    private Rectangle getRectangle(CellType cellType){
        Paint paint = null;
        paint = switch(cellType){
            case FOOD_CELL -> Color.RED;
            case SNAKE_CELL -> Color.GREEN;
            case EMPTY_CELL -> Color.WHITE;
        };
        Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE, paint);
        rect.setStyle("-fx-width: 1;\n-fx-stroke: lightgray;");
        return rect;
    }
}

package nsu.lerabbb.snake.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.Setter;
import nsu.lerabbb.snake.Logger;
import nsu.lerabbb.snake.Model.Game;
import nsu.lerabbb.snake.Model.enums.SystemState;
import nsu.lerabbb.snake.Net.ConnectionHandlers.MulticastReceiver;
import nsu.lerabbb.snake.Net.GameAnnounce;
import nsu.lerabbb.snake.Net.GamesListener;
import nsu.lerabbb.snake.Net.protobuf.SnakesProto;
import nsu.lerabbb.snake.View.GraphicGameView;
import nsu.lerabbb.snake.View.GraphicMenuView;
import nsu.lerabbb.snake.View.IListener;
import nsu.lerabbb.snake.View.IMenuView;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuController implements IController, Initializable {

    private static final String MULTICAST_ADDR = "239.192.0.4";
    private static final int MULTICAST_PORT = 9192;
    private static final int MIN_CELL_NUM = 10;
    private static final int MAX_CELL_NUM = 100;
    private static final int MIN_FOOD_NUM = 0;
    private static final int MAX_FOOD_NUM = 100;
    private static final int MIN_STEP_DELAY = 100;
    private static final int MAX_STEP_DELAY = 3000;

    @FXML private TextField gameNameTextField;
    @FXML private TextField nameTextField;
    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;
    @FXML private TextField foodNumTextField;
    @FXML private TextField stepDelayMsTextField;

    @Getter
    @FXML private ListView<String> gamesListView;

    @Setter
    private IMenuView menuView;

    private int width;
    private int height;
    private int foodNum;
    private int stepDelayMs;
    private String playerName;
    private String gameName;

    private SystemState state;
    private GamesListener gamesListener;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        width = 40;
        height = 30;
        foodNum = 1;
        stepDelayMs = 1000;
        state = SystemState.MENU;
        gamesListener = new GamesListener();
        gamesListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> gamesListviewHandler());
    }

    public void startGameAction(ActionEvent actionEvent) {
        if(!validateInput()){
            return;
        }
        state = SystemState.NEW_GAME;
        threadPool.shutdown();

        SnakesProto.GameConfig config = SnakesProto.GameConfig.newBuilder()
                .setHeight(height)
                .setWidth(width)
                .setFoodStatic(foodNum)
                .setStateDelayMs(stepDelayMs)
                .build();

        IListener gameView = new GraphicGameView(((GraphicMenuView) menuView).getStage(), height, width);
        GameController controller = new GameController(gameView, config, playerName, gameName);
        Logger.getInstance().info("Creating new game");
        controller.start();
        Platform.runLater(gameView::showView);
    }

    @Override
    public void start(){
        try {
            MulticastReceiver multicastReceiver = new MulticastReceiver(MULTICAST_PORT, MULTICAST_ADDR, gamesListener);
            threadPool.submit(() -> {
                while(state.isMenuState()){
                    multicastReceiver.run();
                    if(gamesListener.isGamesListUpd()){
                        Platform.runLater(() -> menuView.drawGamesList(gamesListener.getGamesListAsString()));
                    }
                }
                multicastReceiver.close();
            });
        } catch(UnknownHostException e){
            Logger.getInstance().error("Couldn't get multicast group", e);
            threadPool.shutdown();
        } catch(IOException e){
            Logger.getInstance().error("Fail while creating multicast listener", e);
            threadPool.shutdown();
        }
    }

    public void gamesListviewHandler(){
        if((playerName = nameTextField.getText()) == null){
            Platform.runLater(() -> ((GraphicMenuView)menuView).drawInvalidTextField(nameTextField));
            return;
        }

        int index = gamesListView.getSelectionModel().getSelectedIndex();
        state = SystemState.JOIN_GAME;
        startGame(gamesListener.getGamesList().get(index));
    }


    public void startGame(GameAnnounce gameAnnounce){
        IListener gameView = new GraphicGameView(((GraphicMenuView)menuView).getStage(), height, width);
        Game game = new Game(gameAnnounce.getGameData().getConfig());

        GameController controller = new GameController(
                gameView,
                game,
                gameAnnounce.getAddress(),
                gameAnnounce.getPort(),
                gameAnnounce.getGameData().getConfig(),
                playerName
        );
        controller.start();
        Platform.runLater(gameView::showView);
    }

    private boolean validateInput(){
        boolean flag = true;

        if((playerName = nameTextField.getText()) == null){
            Platform.runLater(() -> ((GraphicMenuView)menuView).drawInvalidTextField(nameTextField));
            flag = false;
        }
        if((gameName = gameNameTextField.getText()) == null){
            Platform.runLater(() -> ((GraphicMenuView)menuView).drawInvalidTextField(gameNameTextField));
            flag = false;
        }
        if(notInteger(widthTextField.getText())){
            Platform.runLater(() -> ((GraphicMenuView)menuView).drawInvalidTextField(widthTextField));
            flag = false;
        }
        if(notInteger(widthTextField.getText())){
            Platform.runLater(() -> ((GraphicMenuView)menuView).drawInvalidTextField(widthTextField));
            flag = false;
        }
        if(notInteger(widthTextField.getText())){
            Platform.runLater(() -> ((GraphicMenuView)menuView).drawInvalidTextField(widthTextField));
            flag = false;
        }
        if(notInteger(widthTextField.getText())){
            Platform.runLater(() -> ((GraphicMenuView)menuView).drawInvalidTextField(widthTextField));
            flag = false;
        }

        if(flag){
            width = Integer.parseInt(widthTextField.getText());
            height = Integer.parseInt(heightTextField.getText());
            foodNum = Integer.parseInt(foodNumTextField.getText());
            stepDelayMs = Integer.parseInt(stepDelayMsTextField.getText());
            if(checkSide(width) || checkSide(height) || !checkFoodNum(foodNum) || !checkStepDelay(stepDelayMs)){
                flag = false;
            }
        }

        return flag;
    }

    private boolean notInteger(String text){
        Pattern pattern = Pattern.compile("^\\d+$");
        Matcher matcher = pattern.matcher(text);
        return !matcher.find();
    }
    private boolean checkSide(int side){
        return side < MIN_CELL_NUM || side > MAX_CELL_NUM;
    }
    private boolean checkFoodNum(int foodNum){
        return foodNum >= MIN_FOOD_NUM && foodNum <= MAX_FOOD_NUM;
    }
    private boolean checkStepDelay(int stepDelayMs){
        return stepDelayMs >= MIN_STEP_DELAY && stepDelayMs <= MAX_STEP_DELAY;
    }
}

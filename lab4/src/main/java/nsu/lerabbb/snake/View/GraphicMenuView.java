package nsu.lerabbb.snake.View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;
import nsu.lerabbb.snake.Controller.IController;
import nsu.lerabbb.snake.Controller.MenuController;
import nsu.lerabbb.snake.Logger;

import java.io.IOException;
import java.util.List;

public class GraphicMenuView implements IMenuView {
    private ListView<String> gamesListView;

    @Getter
    private final Stage stage;

    public GraphicMenuView(Stage stage){
        this.stage = stage;
    }

    @Override
    public void drawMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            Parent root = loader.load();
            IController controller = loader.getController();
            setElements(controller);
            ((MenuController)controller).setMenuView(this);
            controller.start();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            Logger.getInstance().error(e.getMessage());
        }
    }

    @Override
    public void drawGamesList(List<String> gamesInfoList){
        gamesListView.getItems().removeAll();
        if(gamesInfoList.isEmpty()){
            gamesListView.getItems().add("Игры не найдены");
            return;
        }
        gamesListView.getItems().addAll(gamesInfoList);
    }

    public void drawInvalidTextField(TextField textField){
        textField.setStyle("-fx-text-box-border: red ; -fx-focus-color: red ;");
    }

    private void setElements(IController controller){
        this.gamesListView = ((MenuController)controller).getGamesListView();
    }
}

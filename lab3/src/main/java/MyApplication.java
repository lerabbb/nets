import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MyApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Controller controller = new Controller();
        View view = new View(controller, primaryStage);
        controller.setView(view);
        Platform.runLater(view::drawStartScene);
    }

    public static void main(String[] args) {
        MyApplication.launch(args);
    }
}

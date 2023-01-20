package nsu.lerabbb.snake.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ErrorController {
    @FXML private Label errorTextLabel;

    public void setErrorText(String errorText){
        errorTextLabel.setText(errorText);
    }
}

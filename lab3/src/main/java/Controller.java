import geocode.Place;
import javafx.application.Platform;
import javafx.event.ActionEvent;

import java.io.IOException;

public class Controller {
    private View view;

    private Searcher searcher;
    private Place chosenPlace;

    private int radius;
    private boolean searchBtnFlag;

    public Controller() throws IOException {
        this.searcher = new Searcher();
        this.searchBtnFlag = true;
    }
    public Controller(View view) throws IOException {
        this.view = view;
        this.searcher = new Searcher();
    }
    public void setView(View view) {
        this.view = view;
    }

    public void searchBtnAction(ActionEvent actionEvent){
        if(!searchBtnFlag){
            Platform.runLater(() -> view.drawWarningLabel());
            return;
        }
        searchBtnFlag = false;

        //send a GET request to api
        String placeName = view.getPlaceName();
        searcher.makeGhRequest(placeName).thenAccept(foundPlaces -> {
            Platform.runLater(() -> view.drawPlacesResultsScene(foundPlaces));
        });

        Platform.runLater(() -> view.drawWaitScene());
    }

    public void placesListViewHandler(){
        //extract data of chosen place
        chosenPlace = view.getChosenPlace();
        //user is asked to enter a search radius of attractions in modal window
        Platform.runLater(()-> view.showModalWindow());
    }

    public void inputRadiusAction(ActionEvent actionEvent){
        String inputRadius = view.getInputRadius();

        if (!Helper.validateRadius(inputRadius)) {
             view.drawInvalidInputRadius();
             return;
        }
        radius = Integer.parseInt(inputRadius);

        //switch scene to description
        searcher.makeOwRequest(chosenPlace).thenAccept(weather -> {
            Platform.runLater(() -> view.drawWeatherDesc(weather.toString()));
        });

        searcher.makeOtmRequest(chosenPlace, radius).thenAccept(attractions ->{
            Platform.runLater(() -> view.drawAttractDesc(attractions));
        });

        Platform.runLater(()->{
            view.drawPlaceDescScene(chosenPlace.toString());
        });
    }



}

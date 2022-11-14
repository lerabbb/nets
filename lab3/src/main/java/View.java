import attraction.Attraction;
import geocode.FoundPlaces;
import geocode.Place;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class View {
    private Controller controller;

    private Stage stage;
    private Stage modalStage;
    private Scene searchScene;
    private AnchorPane pane;

    private VBox vbox;
    private HBox searchHbox;
    private TextField searchBar;
    private Button searchBtn;
    private ImageView waitImgView;
    private TextField inputRadiusField;
    private VBox descVbox;

    private ListView<Place> placesListView;

    private String appCss;
    private String btnCss;
    private String modalWindowCss;

    public View(Controller controller, Stage stage){
        this.controller = controller;
        this.stage = stage;
        this.appCss = this.getClass().getResource("app.css").toExternalForm();
        this.btnCss = this.getClass().getResource("button.css").toExternalForm();
        this.modalWindowCss = this.getClass().getResource("modalWindow.css").toExternalForm();

        this.searchBar = new TextField();
        this.searchBtn = new Button("Search");
        this.searchHbox = new HBox(searchBar, searchBtn);
        this.vbox = new VBox();

        Image image = new Image(getClass().getResourceAsStream("load.jpg"));
        this.waitImgView = new ImageView(image);
        this.waitImgView.setFitHeight(Constants.IMG_HEIGHT);
        this.waitImgView.setFitWidth(Constants.IMG_WIDTH);

        this.descVbox = new VBox();

        this.placesListView = new ListView<>();
        this.placesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Place>() {
            @Override
            public void changed(ObservableValue<? extends Place> observable, Place oldValue, Place newValue) {
                controller.placesListViewHandler();
            }
        });
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
    public String getPlaceName(){
        String res = searchBar.getText().replace(" ", "+");
        return res.toLowerCase(Locale.ROOT);
    }
    public Place getChosenPlace(){
        return placesListView.getSelectionModel().getSelectedItem();
    }
    public String getInputRadius(){
        return inputRadiusField.getText();
    }
    public void drawStartScene(){
        searchBar.setPromptText("Enter a place name");
        searchBar.setCursor(Cursor.TEXT);
        searchBtn.getStylesheets().add(btnCss);
        searchBtn.setOnAction(controller::searchBtnAction);
        searchBtn.setCursor(Cursor.HAND);
        searchHbox.setSpacing(Constants.SPACING);
        vbox.getChildren().add(searchHbox);
        vbox.setMaxHeight(Constants.VBOX_MAX_HEIGHT);
        vbox.setMaxWidth(Constants.VBOX_MAX_WIDTH);
        vbox.setSpacing(Constants.SPACING);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setLayoutX(Constants.SEARCH_VBOX_X);
        vbox.setLayoutY(Constants.SEARCH_VBOX_Y);

        pane = new AnchorPane();
        pane.getChildren().add(vbox);
        searchScene = new Scene(pane);
        searchScene.getStylesheets().add(appCss);

        stage.setTitle("lab3. Places");
        stage.setResizable(false);
        stage.setScene(searchScene);
        stage.show();
    }
    public void drawWaitScene(){
        vbox.getChildren().add(waitImgView);
    }
    public void drawPlacesResultsScene(FoundPlaces foundPlaces){
        vbox.getChildren().remove(waitImgView);
        Label resultLabel = new Label("Results");
        placesListView.getItems().removeAll();
        placesListView.getItems().addAll(foundPlaces.getPlacesList());
        vbox.getChildren().addAll(resultLabel, placesListView);
    }
    public void drawWarningLabel(){
        Label warningLabel = new Label("You have already made request");
        vbox.getChildren().add(warningLabel);
    }

    public void showModalWindow(){
        Label hintLabel = new Label("Enter an attractions' search radius:");
        inputRadiusField = new TextField();
        Button inputRadiusBtn = new Button("Search");
        inputRadiusBtn.getStylesheets().add(btnCss);
        inputRadiusBtn.setCursor(Cursor.HAND);
        inputRadiusBtn.setOnAction(controller::inputRadiusAction);
        Label warningLabel = new Label("Radius can be only integer");
        VBox vBox = new VBox(hintLabel, inputRadiusField, inputRadiusBtn, warningLabel);
        vBox.setSpacing(Constants.SPACING);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setLayoutX(Constants.MODAL_VBOX_X);
        vBox.setLayoutY(Constants.MODAL_VBOX_Y);
        vBox.setMaxHeight(Constants.MODAL_VBOX_HEIGHT);
        vBox.setMaxWidth(Constants.MODAL_VBOX_WIDTH);

        AnchorPane modalPane = new AnchorPane(vBox);
        Scene modalScene = new Scene(modalPane);
        modalScene.getStylesheets().add(modalWindowCss);

        modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setScene(modalScene);
        modalStage.showAndWait();
    }

    public void drawInvalidInputRadius(){
        inputRadiusField.setStyle("-fx-text-box-border: red ; -fx-focus-color: red ;");
    }

    public void drawPlaceDescScene(String headerText){
        modalStage.close();

        Label headerLabel = new Label(headerText);
        descVbox.getChildren().addAll(headerLabel, waitImgView);
        descVbox.setPrefWidth(Constants.DESC_VBOX_WIDTH);
        descVbox.setPrefHeight(Constants.DESC_VBOX_HEIGHT);
        descVbox.setLayoutX(Constants.DESC_VBOX_X);
        descVbox.setSpacing(Constants.SPACING);
        descVbox.setAlignment(Pos.TOP_CENTER);

        AnchorPane descPane = new AnchorPane(descVbox);
        Scene descScene = new Scene(descPane);
        descScene.getStylesheets().add(appCss);
        stage.setScene(descScene);
        stage.show();
    }
    public void drawWeatherDesc(String desc){
        descVbox.getChildren().remove(waitImgView);
        Label weatherHeaderLabel = new Label("Weather");
        Label weatherDescLabel = new Label(desc);
        descVbox.getChildren().addAll(weatherHeaderLabel, weatherDescLabel);
    }
    public void drawAttractDesc(Attraction[] attractions) {
        List<Attraction> attrList = Arrays.asList(attractions);
        Helper.removeEmptyItems(attrList);

        descVbox.getChildren().remove(waitImgView);
        Label attrHeaderLabel = new Label("Attractions");

        ListView<Attraction> attractListView = new ListView<>();
        attractListView.setPrefHeight(Constants.LIST_VIEW_HEIGHT);
        attractListView.getItems().addAll(attrList);
        descVbox.getChildren().addAll(attrHeaderLabel, attractListView);
    }
}

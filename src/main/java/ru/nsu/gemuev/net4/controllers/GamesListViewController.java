package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import lombok.NonNull;
import ru.nsu.gemuev.net4.controllers.uievents.ListOfAnnGamesChangedEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGameViewEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.model.AnnouncementGame;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GamesListViewController implements Initializable {

    private final EventBus eventBus;
    @FXML
    private TextField playerNameField;
    @FXML
    private ListView<AnnouncementGame> gamesList;

    @Inject
    public GamesListViewController(@NonNull EventBus eventBus){
        this.eventBus = eventBus;
    }


    @Subscribe
    public void listOfGamesChanged(ListOfAnnGamesChangedEvent e){
        Platform.runLater(() -> {
            ObservableList<AnnouncementGame> items = FXCollections
                    .observableArrayList(List.copyOf(e.getGames()));
            gamesList.setItems(items);
        });
    }

    @FXML
    protected void onBackPressed(){
        eventBus.post(new ShowMainViewEvent());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MultipleSelectionModel<AnnouncementGame> selectionModel = gamesList.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.selectedItemProperty().addListener((__, ___, selectedPlace) -> {
            if (selectedPlace != null) {
                System.out.println(selectedPlace);
                //selectionModel.select(-1);
            }
        });
    }

    @FXML
    public void onJoinClicked() {
        Model model = DIContainer.getInjector().getInstance(Model.class);
        model.joinGame_(playerNameField.getText());
    }
}

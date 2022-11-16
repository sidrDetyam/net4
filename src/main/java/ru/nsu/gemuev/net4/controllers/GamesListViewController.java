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
import lombok.NonNull;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.controllers.uievents.ListOfAnnGamesChangedEvent;
import ru.nsu.gemuev.net4.controllers.uievents.ShowGamesList;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GamesListViewController implements Initializable {

    private final EventBus eventBus;
    @FXML
    private ListView<SnakesProto.GameAnnouncement> gamesList;

    @Inject
    public GamesListViewController(@NonNull EventBus eventBus){
        this.eventBus = eventBus;
    }


    @Subscribe
    public void listOfGamesChanged(ListOfAnnGamesChangedEvent e){
        Platform.runLater(() -> {
            ObservableList<SnakesProto.GameAnnouncement> items = FXCollections
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
        MultipleSelectionModel<SnakesProto.GameAnnouncement> selectionModel = gamesList.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.selectedItemProperty().addListener((__, ___, selectedPlace) -> {
            if (selectedPlace != null) {
                System.out.println(selectedPlace);
                //selectionModel.select(-1);
            }
        });
    }
}

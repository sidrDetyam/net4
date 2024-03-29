package ru.nsu.gemuev.net4.controllers;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.Main;
import ru.nsu.gemuev.net4.controllers.uievents.*;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.io.IOException;

@Log4j2
@SuppressWarnings("unused")
public class SceneManager {
    @Setter
    private Stage stage;
    private final Scene mainScene;
    private final Scene configScene;
    private final Scene gameScene;
    private final Scene gamesListScene;
    private final Scene settingsScene;

    private static Parent loadFXML(@NonNull String name) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(name));
        fxmlLoader.setControllerFactory(type -> DIContainer.getInjector().getInstance(type));
        return fxmlLoader.load();
    }

    public SceneManager() {
        try {
            gameScene = new Scene(loadFXML("game_view.fxml"), 709, 550);
            mainScene = new Scene(loadFXML("main_view.fxml"), 600, 400);
            configScene = new Scene(loadFXML("config_view.fxml"), 600, 400);
            gamesListScene = new Scene(loadFXML("game_list_view.fxml"), 600, 400);
            settingsScene = new Scene(loadFXML("settings_view.fxml"), 600, 400);
        } catch (Throwable e) {
            log.error("Ui init fail " + e);
            throw new RuntimeException("Ui init fail"  + e);
        }
    }

    @Subscribe
    public void showConfigView(ShowConfigViewEvent e){
        Platform.runLater(() -> stage.setScene(configScene));
    }

    @Subscribe
    public void showMainView(ShowMainViewEvent e){
        Platform.runLater(() -> stage.setScene(mainScene));
    }

    @Subscribe
    public void showGameView(ShowGameViewEvent e){
        Platform.runLater(() -> stage.setScene(gameScene));
    }

    @Subscribe
    public void showGamesList(ShowGamesList e){
        Platform.runLater(() -> stage.setScene(gamesListScene));
    }

    @Subscribe
    public void showSettings(ShowSettingsView e){
        Platform.runLater(() -> stage.setScene(settingsScene));
    }
}

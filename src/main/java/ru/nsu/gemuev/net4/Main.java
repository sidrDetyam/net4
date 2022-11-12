package ru.nsu.gemuev.net4;

import javafx.application.Application;
import javafx.stage.Stage;
import ru.nsu.gemuev.net4.controllers.SceneManager;
import ru.nsu.gemuev.net4.controllers.events.ShowMainViewEvent;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Snakes");

        SceneManager sceneSwitcher = DIContainer.getInjector().getInstance(SceneManager.class);
        sceneSwitcher.setStage(stage);
        sceneSwitcher.showMainView(new ShowMainViewEvent());
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
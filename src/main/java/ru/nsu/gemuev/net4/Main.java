package ru.nsu.gemuev.net4;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.controllers.SceneManager;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.util.DIContainer;

public class Main extends Application {

    @Override
    public void start(Stage stage){
        stage.setTitle("Snakes");

        var injector = DIContainer.getInjector();

        SceneManager sceneManager = injector.getInstance(SceneManager.class);
        sceneManager.setStage(stage);
        sceneManager.showMainView(new ShowMainViewEvent());
        stage.setResizable(false);
        stage.show();
    }

    @SneakyThrows
    public static void main(String[] args) {
        launch();
    }
}

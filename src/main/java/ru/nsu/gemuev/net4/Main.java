package ru.nsu.gemuev.net4;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.controllers.SceneManager;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.model.Model;
import ru.nsu.gemuev.net4.util.DIContainer;

import java.io.IOException;

public class Main extends Application {

    private SceneManager sceneManager;
    private Model model;

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Snakes");

        var injector = DIContainer.getInjector();

        sceneManager = injector.getInstance(SceneManager.class);
        sceneManager.setStage(stage);
        sceneManager.showMainView(new ShowMainViewEvent());

        model = injector.getInstance(Model.class);

        stage.setResizable(false);
        stage.show();
    }

    @SneakyThrows
    public static void main(String[] args) {
        launch();
    }
}

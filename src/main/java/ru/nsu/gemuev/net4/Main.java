package ru.nsu.gemuev.net4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.controllers.SceneManager;
import ru.nsu.gemuev.net4.controllers.uievents.ShowMainViewEvent;
import ru.nsu.gemuev.net4.model.Model;
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
        Model model = injector.getInstance(Model.class);
        stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, __ -> {
            model.exit();
            Platform.exit();
        });
        stage.show();
    }

    @SneakyThrows
    public static void main(String[] args) {
        launch();
    }
}

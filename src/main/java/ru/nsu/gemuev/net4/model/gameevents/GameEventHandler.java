package ru.nsu.gemuev.net4.model.gameevents;

import com.google.inject.Inject;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.net.MulticastReceiver;

@Log4j2
public class GameEventHandler{

    public GameEventHandler(){

    }

    public void handle(SnakesProto.GameMessage gameMessage){
        System.out.println("not impl!");
    }
}

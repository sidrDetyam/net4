package ru.nsu.gemuev.net4.model.gameevents;

import com.google.inject.Inject;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import ru.nsu.gemuev.net4.SnakesProto;
import ru.nsu.gemuev.net4.net.MulticastReceiver;

import java.net.InetAddress;

@Log4j2
public class GameEventHandler{

    public GameEventHandler(){

    }

    public void handle(@NonNull SnakesProto.GameMessage gameMessage, @NonNull InetAddress inetAddress, int port){
        System.out.println("not impl! " + inetAddress + " " + port);
    }
}

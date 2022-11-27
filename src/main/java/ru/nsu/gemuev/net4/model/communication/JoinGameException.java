package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;

public class JoinGameException extends Exception{
    public JoinGameException(@NonNull String cause){
        super(cause);
    }
}

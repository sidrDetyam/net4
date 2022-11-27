package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;
import ru.nsu.gemuev.net4.model.ports.Message;

public interface MessageHandler {
    void handle(@NonNull Message message);
}

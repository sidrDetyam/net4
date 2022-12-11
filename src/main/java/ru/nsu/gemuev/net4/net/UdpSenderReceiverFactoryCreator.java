package ru.nsu.gemuev.net4.net;

import lombok.SneakyThrows;
import ru.nsu.gemuev.net4.model.ports.SenderReceiverFactory;
import ru.nsu.gemuev.net4.model.ports.SenderReceiverFactoryCreator;

public class UdpSenderReceiverFactoryCreator implements SenderReceiverFactoryCreator {

    @Override
    @SneakyThrows
    public SenderReceiverFactory newFactory() {
        return new UdpSenderReceiverFactory();
    }
}

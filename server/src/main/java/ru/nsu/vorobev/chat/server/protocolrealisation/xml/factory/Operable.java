package ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;

public interface Operable {
    void doOperation(IContext context, TCPConnection connection);
}

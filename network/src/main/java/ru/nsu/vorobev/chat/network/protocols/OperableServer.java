package ru.nsu.vorobev.chat.network.protocols;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;

public interface OperableServer {
    void  doOperation(IContextServer contextServer, TCPConnection connection);
}

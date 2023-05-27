package ru.nsu.vorobev.chat.network.protocols;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;

public interface IContextServer {
    void onMessageReact(TCPConnection connection,String msg, int ID);
    void onNamesReact(TCPConnection connection, int ID);
    void onDisconnectReact(TCPConnection connection, int ID);
}

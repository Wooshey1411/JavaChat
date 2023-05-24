package ru.nsu.vorobev.chat.network.connection;

import java.io.IOException;

public interface TCPConnectionListener {
    void onConnectionReady(TCPConnection tcpConnection);
    void onReceiveData(TCPConnection tcpConnection, byte[] obj);
    void onDisconnect(TCPConnection tcpConnection);
    void onException(TCPConnection tcpConnection, Exception ex);
    void onRegistration(TCPConnection tcpConnection) throws IOException, ClassNotFoundException;
}

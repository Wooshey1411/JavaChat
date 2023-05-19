package ru.nsu.vorobev.chat.network.connection;

import java.io.IOException;

public interface TCPConnectionListener {
    void onConnectionReady(TCPConnectionSerializable tcpConnectionSerializable);
    void onReceiveData(TCPConnectionSerializable tcpConnectionSerializable, Object obj);
    void onDisconnect(TCPConnectionSerializable tcpConnectionSerializable);
    void onException(TCPConnectionSerializable tcpConnectionSerializable, Exception ex);
    void onRegistration(TCPConnectionSerializable tcpConnectionSerializable) throws IOException, ClassNotFoundException;
}

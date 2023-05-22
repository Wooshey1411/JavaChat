package ru.nsu.vorobev.chat.network.connection;

import java.io.IOException;

public interface TCPConnection {
    void disconnect();
    void sendData(Object obj);
    Object receiveData() throws IOException, ClassNotFoundException;

}

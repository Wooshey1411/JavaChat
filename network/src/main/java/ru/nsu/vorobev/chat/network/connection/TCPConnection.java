package ru.nsu.vorobev.chat.network.connection;

import java.io.IOException;

public interface TCPConnection {
    void disconnect();
    void sendData(byte[] data);
    byte[] receiveData() throws IOException, ClassNotFoundException;

}

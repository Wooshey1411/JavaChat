package ru.nsu.vorobev.chat.client.model.protocolrealisation;

import java.io.IOException;

public interface Connection {
    void connect() throws IOException;

    void disconnect();

    void sendMsg(String msg);

    void usersListRequest();
}

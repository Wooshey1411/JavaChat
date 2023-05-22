package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.server.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.server.protocolrealisation.XMLProtocol;


public class ChatServer {

    private static final int port = 8377;
    public static final int maxHistoryLen = 5;
    public static void main(String[] args) {

        // Connection connection = new SerializableProtocol

        Connection connection = new XMLProtocol(port);

        connection.start();
    }

}

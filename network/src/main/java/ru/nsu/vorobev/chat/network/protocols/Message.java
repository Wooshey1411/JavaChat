package ru.nsu.vorobev.chat.network.protocols;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;

import java.io.Serial;
import java.io.Serializable;

public class Message implements Serializable,OperableServer {
    @Serial
    private static final long serialVersionUID = 5;
    public Message(String message, int ID, String name){
        this.name = name;
        this.message = message;
        this.ID = ID;
    }
    private final String message;
    private final int ID;
    private final String name;
    public int getID() {
        return ID;
    }

    @Override
    public void doOperation(IContextServer contextServer, TCPConnection connection) {
        contextServer.onMessageReact(connection,message,ID);
    }
}

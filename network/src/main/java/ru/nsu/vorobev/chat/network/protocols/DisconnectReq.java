package ru.nsu.vorobev.chat.network.protocols;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;

import java.io.Serial;
import java.io.Serializable;

public class DisconnectReq implements Serializable,OperableServer {
    @Serial
    private static final long serialVersionUID = 5;

    public DisconnectReq(int ID){
        this.ID = ID;
    }
    private final int ID;
    public int getID(){
        return ID;
    }

    @Override
    public void doOperation(IContextServer contextServer, TCPConnection connection) {
        contextServer.onDisconnectReact(connection,ID);
    }
}

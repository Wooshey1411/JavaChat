package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class DisconnectReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 5;

    public DisconnectReq(int ID){
        this.ID = ID;
    }
    private final int ID;
    public int getID(){
        return ID;
    }
}

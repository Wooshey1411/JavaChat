package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class RegistrationReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 2;
    public RegistrationReq(int ID, String msg){
        this.ID = ID;
        this.msg = msg;
    }

    private final String msg;
    private final int ID;

    public int getID() {
        return ID;
    }

    public String getMsg() {
        return msg;
    }
}

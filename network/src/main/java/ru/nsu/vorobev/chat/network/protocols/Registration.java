package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class Registration implements Serializable {
    @Serial
    private static final long serialVersionUID = 2;
    public Registration(boolean isSuccessful, int ID, String msg){
        this.isSuccessful = isSuccessful;
        this.ID = ID;
        this.msg = msg;
    }

    private String msg;
    private final int ID;
    private boolean isSuccessful;

    public int getID() {
        return ID;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

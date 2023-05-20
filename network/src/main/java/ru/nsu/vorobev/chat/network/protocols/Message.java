package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 5;
    public Message(String message, boolean isSuccessful, int ID){
        this.message = message;
        this.isSuccessful = isSuccessful;
        this.ID = ID;
    }
    private String message;
    private boolean isSuccessful;
    private int ID;
    public String getMessage() {
        return message;
    }

    public int getID() {
        return ID;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}

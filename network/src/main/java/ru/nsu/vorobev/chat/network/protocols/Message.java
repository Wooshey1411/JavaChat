package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 5;
    public Message(String message, int ID, String name){
        this.name = name;
        this.message = message;
        this.ID = ID;
    }
    private String message;
    private int ID;
    private String name;
    public String getMessage() {
        return message;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }
}

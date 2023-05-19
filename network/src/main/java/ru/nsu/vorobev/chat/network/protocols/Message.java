package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 5;
    public Message(String message){
        this.message = message;
    }
    String message;

    public String getMessage() {
        return message;
    }
}

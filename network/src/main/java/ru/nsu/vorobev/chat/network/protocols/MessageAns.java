package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class MessageAns implements Serializable {

    @Serial
    private static final long serialVersionUID = 5;
    public MessageAns(boolean isSuccessful, String reason){
        this.reason = reason;
        this.isSuccessful = isSuccessful;
    }
    private boolean isSuccessful;
    private String reason;

    public String getReason() {
        return reason;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }
}

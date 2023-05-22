package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class Disconnect implements Serializable {
    @Serial
    private static final long serialVersionUID = 5;

    public Disconnect(boolean isSuccessful, String reason){
        this.isSuccessful = isSuccessful;
        this.reason = reason;
    }
    private final boolean isSuccessful;
    private final String reason;

    public boolean getSuccessful() {
        return isSuccessful;
    }

    public String getReason() {
        return reason;
    }
}

package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class RegistrationAns implements Serializable {
    @Serial
    private static final long serialVersionUID = 2;

    public RegistrationAns(boolean isSuccessful, String message, int ID){
        this.isSuccessful = isSuccessful;
        this.message = message;
        this.ID = ID;
    }

    private final boolean isSuccessful;
    private final String message;
    private final int ID;

    public String getMessage() {
        return message;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public int getID() {
        return ID;
    }
}

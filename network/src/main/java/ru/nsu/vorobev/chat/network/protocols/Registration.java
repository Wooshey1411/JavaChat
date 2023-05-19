package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class Registration implements Serializable {
    @Serial
    private static final long serialVersionUID = 2;
    public Registration(){}

    public String msg;
    public int ID;
    public boolean isSuccessful;
}

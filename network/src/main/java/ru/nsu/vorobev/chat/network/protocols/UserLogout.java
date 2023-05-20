package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class UserLogout implements Serializable {
    @Serial
    private static final long serialVersionUID = 2;

    public UserLogout(String name){
        this.name = name;
    }
    private final String name;

    public String getName() {
        return name;
    }
}

package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class UserLogin implements Serializable {
    @Serial
    private static final long serialVersionUID = 2;

    public UserLogin(String name){
        this.name = name;
    }
    private final String name;

    public String getName() {
        return name;
    }
}

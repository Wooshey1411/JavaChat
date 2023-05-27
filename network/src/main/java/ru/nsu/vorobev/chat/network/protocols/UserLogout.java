package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class UserLogout implements Serializable,Operable {
    @Serial
    private static final long serialVersionUID = 2;

    public UserLogout(String name){
        this.name = name;
    }
    private final String name;

    @Override
    public void doOperation(IContext context) {
        context.onLogoutReact(name);
    }
}

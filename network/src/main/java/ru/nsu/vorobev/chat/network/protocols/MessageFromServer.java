package ru.nsu.vorobev.chat.network.protocols;

import java.io.Serial;
import java.io.Serializable;

public class MessageFromServer implements Serializable,Operable {

    @Serial
    private static final long serialVersionUID = 5;

    public MessageFromServer(String message,String name,int ID){
        this.name = name;
        this.message = message;
    }
    private final String message;
    private final String name;
    public String getMessage() {
        return message;
    }
    public String getName() {
        return name;
    }

    @Override
    public void doOperation(IContext context) {
        context.onMessageReact(message,name);
    }
}

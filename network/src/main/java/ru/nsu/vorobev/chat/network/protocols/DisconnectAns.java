package ru.nsu.vorobev.chat.network.protocols;


import java.io.Serial;
import java.io.Serializable;

public class DisconnectAns implements Serializable,Operable {
    @Serial
    private static final long serialVersionUID = 5;

    public DisconnectAns(String reason, boolean isSuccessful){
        this.reason = reason;
        this.isSuccessful = isSuccessful;
    }
    private final String reason;
    private final boolean isSuccessful;
    @Override
    public void doOperation(IContext context) {
        context.onDisconnectReact(isSuccessful,reason);
    }
}

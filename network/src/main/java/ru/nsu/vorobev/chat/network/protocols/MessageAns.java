package ru.nsu.vorobev.chat.network.protocols;
import java.io.Serial;
import java.io.Serializable;

public class MessageAns implements Serializable,Operable {

    @Serial
    private static final long serialVersionUID = 5;
    public MessageAns(boolean isSuccessful, String reason){
        this.reason = reason;
        this.isSuccessful = isSuccessful;
    }
    private final boolean isSuccessful;
    private final String reason;

    @Override
    public void doOperation(IContext context) {
        context.onMessageAnsReact(isSuccessful,reason);
    }
}

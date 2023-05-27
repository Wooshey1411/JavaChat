package ru.nsu.vorobev.chat.server.protocolrealisation;

public class MessageType {
    private final String msg;
    private final String sender;

    public MessageType(String msg, String sender){
        this.msg = msg;
        this.sender = sender;
    }

    public String getMsg() {
        return msg;
    }

    public String getSender() {
        return sender;
    }
}

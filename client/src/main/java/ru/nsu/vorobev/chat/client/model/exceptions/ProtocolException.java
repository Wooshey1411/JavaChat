package ru.nsu.vorobev.chat.client.model.exceptions;

public class ProtocolException extends RuntimeException{
    public ProtocolException(String msg){
        super(msg);
    }

    public ProtocolException(String msg, Throwable cause){
        super(msg,cause);
    }
}

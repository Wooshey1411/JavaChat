package ru.nsu.vorobev.chat.client.model.exceptions;

public class SocketException extends RuntimeException{
    public SocketException(String msg, Throwable cause){
        super(msg,cause);
    }
    public SocketException(String msg){
        super(msg);
    }
}

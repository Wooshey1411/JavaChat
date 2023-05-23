package ru.nsu.vorobev.chat.server.configparser;

public class BadConfigException extends RuntimeException{
    public BadConfigException(String msg, Throwable cause){
        super(msg,cause);
    }
    public BadConfigException(String msg){
        super(msg);
    }
}

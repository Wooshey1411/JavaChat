package ru.nsu.vorobev.chat.client.model.configparser;

public class NoConfigFileException extends RuntimeException{
    public NoConfigFileException(String msg, Throwable cause){
        super(msg,cause);
    }
}

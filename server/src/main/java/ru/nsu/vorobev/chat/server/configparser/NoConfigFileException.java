package ru.nsu.vorobev.chat.server.configparser;

public class NoConfigFileException extends RuntimeException{
    public NoConfigFileException(String msg, Throwable cause){
        super(msg,cause);
    }
}

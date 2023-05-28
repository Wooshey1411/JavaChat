package ru.nsu.vorobev.chat.server.protocolrealisation.xml.factory;

public class NoFactoryConfigException extends RuntimeException{
    public NoFactoryConfigException(String msg){
        super(msg);
    }

    public NoFactoryConfigException(String msg, Throwable cause){
        super(msg,cause);
    }
}

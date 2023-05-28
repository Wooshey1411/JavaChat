package ru.nsu.vorobev.chat.client.model.protocolrealisation.xml.factory;

public class NoFactoryConfigException extends RuntimeException{
    public NoFactoryConfigException(String msg){
        super(msg);
    }

    public NoFactoryConfigException(String msg, Throwable cause){
        super(msg,cause);
    }
}

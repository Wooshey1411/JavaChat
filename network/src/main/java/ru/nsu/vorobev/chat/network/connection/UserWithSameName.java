package ru.nsu.vorobev.chat.network.connection;

public class UserWithSameName extends RuntimeException{
    public UserWithSameName(String msg){
        super(msg);
    }
}

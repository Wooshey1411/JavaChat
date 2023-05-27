package ru.nsu.vorobev.chat.server.protocolrealisation;

public class User {
    private final String nickname;
    private final int ID;

    public User(String nickname, int ID){
        this.nickname = nickname;
        this.ID = ID;
    }
    public String getNickname() {
        return nickname;
    }
    public int getID() {
        return ID;
    }
}

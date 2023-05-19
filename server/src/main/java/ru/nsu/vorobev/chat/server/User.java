package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.network.connection.TCPConnectionSerializable;

public class User {
    private final TCPConnectionSerializable connection;
    private final String nickname;
    private final int ID;

    public User(TCPConnectionSerializable connection, String nickname, int ID){
        this.connection = connection;
        this.nickname = nickname;
        this.ID = ID;
    }
    public TCPConnectionSerializable getConnection() {
        return connection;
    }

    public String getNickname() {
        return nickname;
    }

    public int getID() {
        return ID;
    }
}

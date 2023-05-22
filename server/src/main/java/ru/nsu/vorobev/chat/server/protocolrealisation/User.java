package ru.nsu.vorobev.chat.server.protocolrealisation;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;

public class User {
    private final TCPConnection connection;
    private final String nickname;
    private final int ID;

    public User(TCPConnection connection, String nickname, int ID){
        this.connection = connection;
        this.nickname = nickname;
        this.ID = ID;
    }
    public TCPConnection getConnection() {
        return connection;
    }

    public String getNickname() {
        return nickname;
    }

    public int getID() {
        return ID;
    }
}

package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.server.configparser.BadConfigException;
import ru.nsu.vorobev.chat.server.configparser.NoConfigFileException;

public class Main {

    public static void main(String[] args) {
        try{
            ChatServer server = new ChatServer();
            server.run();
        } catch (BadConfigException | NoConfigFileException ex){
            System.out.println(ex.getMessage());
        }
    }


}

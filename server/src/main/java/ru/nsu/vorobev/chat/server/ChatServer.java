package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.server.configparser.BadConfigException;
import ru.nsu.vorobev.chat.server.configparser.ConfigParser;
import ru.nsu.vorobev.chat.server.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.server.protocolrealisation.SerializableProtocol;
import ru.nsu.vorobev.chat.server.protocolrealisation.User;
import ru.nsu.vorobev.chat.server.protocolrealisation.XMLProtocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatServer {

    public static final int maxHistoryLen = 5;
    private Connection connection;
    private HashMap<User,Connection> users = new HashMap<>();
    private List<String> messages = new ArrayList<>();

    public ChatServer(){
        try(ConfigParser parser = new ConfigParser()){
            int port = parser.getIntByName("port");
            if(port < 0 || port > 65535){
                throw new BadConfigException("port must be 0-65535");
            }
            String protocol = parser.getStrByName("protocol");
            switch (protocol){
                case "XML" -> connection = new XMLProtocol(port);
                case "Serializable" -> connection = new SerializableProtocol(port,this);
                default -> throw new BadConfigException("Such protocol doesn't exist!");
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void run(){
        connection.start();
    }

    public List<String> getMessages() {
        return messages;
    }

    public HashMap<User, Connection> getUsers() {
        return users;
    }
}

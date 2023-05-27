package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.server.configparser.BadConfigException;
import ru.nsu.vorobev.chat.server.configparser.ConfigParser;
import ru.nsu.vorobev.chat.server.protocolrealisation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatServer {

    public static final int maxHistoryLen = 5;
    private Connection connection;
    private final HashMap<TCPConnection,User> users = new HashMap<>();
    private final List<MessageType> messages = new ArrayList<>();

    public ChatServer(){
        try(ConfigParser parser = new ConfigParser()){
            int port = parser.getIntByName("port");
            if(port < 0 || port > 65535){
                throw new BadConfigException("port must be 0-65535");
            }
            String protocol = parser.getStrByName("protocol");
            switch (protocol){
                case "XML" -> connection = new XMLProtocol(port,this);
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

    public List<MessageType> getMessages() {
        return messages;
    }

    public HashMap<TCPConnection, User> getUsers() {
        return users;
    }
}

package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.server.configparser.BadConfigException;
import ru.nsu.vorobev.chat.server.configparser.ConfigParser;
import ru.nsu.vorobev.chat.server.protocolrealisation.Connection;
//import ru.nsu.vorobev.chat.server.protocolrealisation.SerializableProtocol;
import ru.nsu.vorobev.chat.server.protocolrealisation.SerializableProtocol;
import ru.nsu.vorobev.chat.server.protocolrealisation.XMLProtocol;


public class ChatServer {

    public static final int maxHistoryLen = 5;
    private Connection connection;

    public ChatServer(){
        try(ConfigParser parser = new ConfigParser()){
            int port = parser.getIntByName("port");
            if(port < 0 || port > 65535){
                throw new BadConfigException("port must be 0-65535");
            }
            String protocol = parser.getStrByName("protocol");
            switch (protocol){
                case "XML" -> connection = new XMLProtocol(port);
                case "Serializable" -> connection = new SerializableProtocol(port);
                default -> throw new BadConfigException("Such protocol doesn't exist!");
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
    public void run(){
        connection.start();
    }
}

package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.server.configparser.BadConfigException;
import ru.nsu.vorobev.chat.server.configparser.ConfigParser;
import ru.nsu.vorobev.chat.server.configparser.NoConfigFileException;
import ru.nsu.vorobev.chat.server.protocolrealisation.Connection;
import ru.nsu.vorobev.chat.server.protocolrealisation.SerializableProtocol;
import ru.nsu.vorobev.chat.server.protocolrealisation.XMLProtocol;


public class ChatServer {

   // private static final int port = 8377;
    public static final int maxHistoryLen = 5;
    public static void main(String[] args) {

        Connection connection;
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
        } catch (BadConfigException | NoConfigFileException ex){
            System.out.println("Config error: " + ex);
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        connection.start();
    }

}

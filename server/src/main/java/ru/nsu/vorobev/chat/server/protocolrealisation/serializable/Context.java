package ru.nsu.vorobev.chat.server.protocolrealisation.serializable;

import ru.nsu.vorobev.chat.network.connection.TCPConnection;
import ru.nsu.vorobev.chat.network.protocols.*;
import ru.nsu.vorobev.chat.server.ChatServer;
import ru.nsu.vorobev.chat.server.protocolrealisation.Log;
import ru.nsu.vorobev.chat.server.protocolrealisation.MessageType;
import ru.nsu.vorobev.chat.server.protocolrealisation.User;

public class Context implements IContextServer {

    private final ChatServer server;

    public Context(ChatServer server){
        this.server = server;
    }

    @Override
    public void onMessageReact(TCPConnection connection, String msg, int ID) {
        MessageFromServer BCMessage = null;
        User sender = null;
        for (User user : server.getUsers().values()){
            if(user.getID() == ID){
                sender = user;
                BCMessage = new MessageFromServer(msg,user.getNickname(),ID);
            }
        }
        if(sender == null){
            connection.sendData(Utils.convertObjectToByte(new MessageAns(false, "Bad ID")));
            Log.log(Log.getTime() + ":Client try to send message with nonexistent ID. TCPConnection:" + connection,Log.TypeOfLoggers.INFO);
            return;
        }
        if(msg == null){
            connection.sendData(Utils.convertObjectToByte(new MessageAns(false, "Null string")));
            Log.log(Log.getTime() + ":Client try to send message with NULL string. ID=" + sender.getID(),Log.TypeOfLoggers.INFO);
            return;
        }
        connection.sendData(Utils.convertObjectToByte(new MessageAns(true,null)));

        if(server.getMessages().size() == ChatServer.maxHistoryLen){
            server.getMessages().remove(0);
        }
        server.getMessages().add(new MessageType(BCMessage.getMessage(),BCMessage.getName()));
        for (TCPConnection connections : server.getUsers().keySet()) {
            connections.sendData(Utils.convertObjectToByte(BCMessage));
        }
        Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send message \"" + BCMessage.getMessage() + "\"",Log.TypeOfLoggers.INFO);
    }

    @Override
    public void onNamesReact(TCPConnection connection, int ID) {
        User sender = null;
        for (User user : server.getUsers().values()){
            if(user.getID() == ID){
                sender = user;
            }
        }
        if(sender == null){
            connection.sendData(Utils.convertObjectToByte(new NamesAns(false, "Bad ID")));
            Log.log(Log.getTime() + ":Client send request of names with nonexistent ID. TCPConnection:" + connection,Log.TypeOfLoggers.INFO);
            return;
        }
        NamesAns ans = new NamesAns(true,null);
        for (User user : server.getUsers().values()){
            ans.addName(user.getNickname());
        }
        connection.sendData(Utils.convertObjectToByte(ans));
        Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send request of names successfully",Log.TypeOfLoggers.INFO);
    }

    @Override
    public void onDisconnectReact(TCPConnection connection, int ID) {
        int id = -1;
        for (User user : server.getUsers().values()){
            if(user.getID() == ID){
                id = user.getID();
                break;
            }
        }
        if(id == -1){
            connection.sendData(Utils.convertObjectToByte(new DisconnectAns("Wrong session ID",false)));
            Log.log(Log.getTime() + ":Client " + connection + " try to disconnect with nonexistent ID",Log.TypeOfLoggers.WARNING);
            return;
        }
        connection.sendData(Utils.convertObjectToByte(new DisconnectAns(null,true)));
        Log.log(Log.getTime() + ":Client with ID=" + id + " successfully send disconnect request",Log.TypeOfLoggers.INFO);
    }
}

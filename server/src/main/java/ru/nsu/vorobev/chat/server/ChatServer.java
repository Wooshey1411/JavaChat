package ru.nsu.vorobev.chat.server;

import ru.nsu.vorobev.chat.network.connection.*;
import ru.nsu.vorobev.chat.network.protocols.*;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ChatServer implements TCPConnectionListener {

    private static final int port = 8377;
    private static int ID = 0;

    private final List<User> users = new ArrayList<>();

    private final List<Message> messagesHistory = new ArrayList<>();

    public static void main(String[] args) {
        new ChatServer();
    }

    private ChatServer() {
        System.out.println("Server running...");
        Log.enableLogger();
        Log.init();
        Log.log(Log.getTime() + ":Server start working",Log.TypeOfLoggers.INFO);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new TCPConnectionSerializable(this, serverSocket.accept());
                } catch (IOException ex) {
                    Log.log(Log.getTime() + ":TCPConnection exception:" + ex,Log.TypeOfLoggers.ERROR);
                }
                catch (UserWithSameName ignored){}

            }
        } catch (IOException ex) {
            Log.log(Log.getTime() + ":Server socket error! Server shutdown... exception:" + ex, Log.TypeOfLoggers.ERROR);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnectionSerializable tcpConnectionSerializable) {

        String name = null;
        int ID = -1;
        for (User user : users){
            if(user.getConnection() == tcpConnectionSerializable){
                name = user.getNickname();
                ID = user.getID();
                break;
            }
        }
        Log.log(Log.getTime() + ":Client connected. Nickname:" +name + " ID=" + ID, Log.TypeOfLoggers.INFO);
        broadCastMessage(new UserLogin(name));
        for (Message msg : messagesHistory){
            tcpConnectionSerializable.sendData(msg);
        }
    }

    @Override
    public synchronized void onReceiveData(TCPConnectionSerializable tcpConnectionSerializable, Object obj) {
        if(obj instanceof Message){
            Message BCMessage = null;
            User sender = null;
            for (User user : users){
                if(user.getID() == ((Message) obj).getID()){
                    sender = user;
                    BCMessage = new Message(((Message) obj).getMessage(),-1,user.getNickname());
                }
            }
            if(sender == null){
                tcpConnectionSerializable.sendData(new MessageAns(false, "Bad ID"));
                Log.log(Log.getTime() + ":Client try to send message with nonexistent ID. TCPConnection:" + tcpConnectionSerializable,Log.TypeOfLoggers.INFO);
                return;
            }
            if(((Message) obj).getMessage() == null){
                tcpConnectionSerializable.sendData(new MessageAns(false, "Null string"));
                Log.log(Log.getTime() + ":Client try to send message with NULL string. ID=" + sender.getID(),Log.TypeOfLoggers.INFO);
                return;
            }
            tcpConnectionSerializable.sendData(new MessageAns(true,null));
            int maxHistoryLen = 5;
            if(messagesHistory.size() == maxHistoryLen){
                messagesHistory.remove(0);
            }
            messagesHistory.add(BCMessage);
            broadCastMessage(BCMessage);
            Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send message \"" + BCMessage.getMessage() + "\"",Log.TypeOfLoggers.INFO);
            return;
        }
        if(obj instanceof NamesReq){
            User sender = null;
            for (User user : users){
                if(user.getID() == ((NamesReq) obj).getID()){
                    sender = user;
                }
            }
            if(sender == null){
                tcpConnectionSerializable.sendData(new NamesAns(false, "Bad ID"));
                Log.log(Log.getTime() + ":Client send request of names with nonexistent ID. TCPConnection:" + tcpConnectionSerializable,Log.TypeOfLoggers.INFO);
                return;
            }
            NamesAns ans = new NamesAns(true,null);
            for (User user : users){
                ans.addName(user.getNickname());
            }
            tcpConnectionSerializable.sendData(ans);
            Log.log(Log.getTime() + ":Client " + sender.getNickname() + " with ID=" + sender.getID() + " send request of names successfully",Log.TypeOfLoggers.INFO);
        }


    }

    @Override
    public synchronized void onDisconnect(TCPConnectionSerializable tcpConnectionSerializable) {
        String name = null;
        for(User user : users){
            if (tcpConnectionSerializable == user.getConnection()){
                name = user.getNickname();
                users.remove(user);
                break;
            }
        }
        Log.log(Log.getTime() + ":Client disconnected. Nickname:" +name,Log.TypeOfLoggers.INFO);
        broadCastMessage(new UserLogout(name));
    }

    @Override
    public synchronized void onException(TCPConnectionSerializable tcpConnectionSerializable, Exception ex) {
        Log.log(Log.getTime() + ":" + ex, Log.TypeOfLoggers.WARNING);
    }

    @Override
    public void onRegistration(TCPConnectionSerializable tcpConnectionSerializable) throws IOException, ClassNotFoundException {
        Registration registrationReceive;
        registrationReceive = (Registration) tcpConnectionSerializable.getIn().readObject();

        //System.out.println(registrationReceive.msg);
        Registration registrationAns = new Registration(true,ID++,null);
        for (User user : users){
            if(Objects.equals(user.getNickname(), registrationReceive.getMsg())){
                registrationAns.setSuccessful(false);
                registrationAns.setMsg("Exist user with same name");
                break;
            }
        }
        tcpConnectionSerializable.getOut().writeObject(registrationAns);
        tcpConnectionSerializable.getOut().flush();
        if(!registrationAns.isSuccessful()){
            Log.log(Log.getTime() + ":Client try to connect with exist nickname connection was closed. Nickname:" + registrationReceive.getMsg(),Log.TypeOfLoggers.INFO);
            throw new UserWithSameName("Exist user with same name");
        }
        users.add(new User(tcpConnectionSerializable, registrationReceive.getMsg(),registrationAns.getID()));
    }

    private void broadCastMessage(Object object) {
      //  System.out.println("Broadcast: " + object.toString());
        for (User user : users) {
            user.getConnection().sendData(object);
        }
    }
}

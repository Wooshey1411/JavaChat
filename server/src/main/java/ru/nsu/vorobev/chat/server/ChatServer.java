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

    public static void main(String[] args) {
        new ChatServer();
    }

    private ChatServer() {
        System.out.println("Server running...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    new TCPConnectionSerializable(this, serverSocket.accept());
                } catch (IOException ex) {
                    System.out.println("TCPConnection exception: " + ex);
                }
                catch (UserWithSameName ignored){}

            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnectionSerializable tcpConnectionSerializable) {
        System.out.println("Client connected: " + tcpConnectionSerializable);
        String name = null;
        for (User user : users){
            if(user.getConnection() == tcpConnectionSerializable){
                name = user.getNickname();
                break;
            }
        }

        broadCastMessage(new UserLogin(name));
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
                return;
            }
            if(((Message) obj).getMessage() == null){
                tcpConnectionSerializable.sendData(new MessageAns(false, "Null string"));
                return;
            }
            tcpConnectionSerializable.sendData(new MessageAns(true,null));
            broadCastMessage(BCMessage);
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
                return;
            }
            NamesAns ans = new NamesAns(true,null);
            for (User user : users){
                System.out.println(user.getNickname());
                ans.addName(user.getNickname());
            }
            tcpConnectionSerializable.sendData(ans);
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
        System.out.println("Client disconnected: " + tcpConnectionSerializable);
        broadCastMessage(new UserLogout(name));
    }

    @Override
    public synchronized void onException(TCPConnectionSerializable tcpConnectionSerializable, Exception ex) {
        System.out.println("TCPConnection exception: " + ex);
    }

    @Override
    public void onRegistration(TCPConnectionSerializable tcpConnectionSerializable) throws IOException, ClassNotFoundException {
        Registration registrationReceive;
        registrationReceive = (Registration) tcpConnectionSerializable.getIn().readObject();

        //System.out.println(registrationReceive.msg);
        Registration registrationAns = new Registration();
        registrationAns.ID = ID++;
        registrationAns.isSuccessful = true;
        for (User user : users){
            if(Objects.equals(user.getNickname(), registrationReceive.msg)){
                registrationAns.isSuccessful = false;
                registrationAns.msg = "Exist user with same name";
                break;
            }
        }
        tcpConnectionSerializable.getOut().writeObject(registrationAns);
        tcpConnectionSerializable.getOut().flush();
        if(!registrationAns.isSuccessful){
            throw new UserWithSameName("Exist user with same name");
        }
        users.add(new User(tcpConnectionSerializable, registrationReceive.msg,registrationAns.ID));
    }

    private void broadCastMessage(Object object) {
        System.out.println("Broadcast: " + object.toString());
        for (User user : users) {
            user.getConnection().sendData(object);
        }
    }
}
